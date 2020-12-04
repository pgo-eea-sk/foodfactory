package foodfactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Peter Golian Task which implements assembly line. It tries to put
 *         product in the oven and if there is no free space in the ovens it
 *         tries tu put the product in the store, if there is no free store, it
 *         halts until some oven or store is free (it prevents situation, that
 *         some big product is waiting in store and line is halted for it)
 */
public class AssemblyLineTask implements Callable<String> {

	private List<Product> productsOnLine;
	private String assemblyLineName;
	private Map<Store, BlockingQueue<ProductFromLine>> storeQueues;

	AssemblyLineTask(List<Product> productsToCook, String name,
			Map<Store, BlockingQueue<ProductFromLine>> storeQueues) {
		productsOnLine = new ArrayList<Product>();
		productsOnLine.addAll(productsToCook);
		assemblyLineName = name;
		this.storeQueues = storeQueues;
	}

	@Override
	public String call() {
		AssemblyLineStage assemblyLine = new AssemblyLineStageImpl(productsOnLine, assemblyLineName);
		ExecutorService ovenExecutor = Executors.newFixedThreadPool(FoodFactoryMain.productCounter);
		Product p = null;
		BlockingQueue<ProductInOven> productInOven = new LinkedBlockingDeque<ProductInOven>();
		while (true) {
			if (p == null) {
				// taking product from assembly line
				p = assemblyLine.take();
				if (p == null) {
					break;
				}
				Utils.log(String.format("%s taken out", p.toString()));

			}
			
			// searching for free oven
			for (Oven oven : FoodFactoryMain.ovens) {
				try {
					Utils.log(String.format("%s - %s(%.0f, %d), %s, Oven size before: %.0f, Remainig products %d",
							assemblyLineName, p.toString(), p.size(), p.cookTime().getSeconds(), oven.toString(),
							oven.size(), assemblyLine.remainig()));
					oven.put(p);
					productInOven.add(new ProductInOven(oven, p, LocalTime.now()));
					p = null;
					break;
				} catch (CapacityExceededException e) {
					Utils.log(e.getMessage());
					continue;
				}
			}
			
			// no oven is free seaechng for free store
			if (p != null) {
				while (true) {
					for (Map.Entry<Store, BlockingQueue<ProductFromLine>> storeQueue : storeQueues.entrySet()) {
						try {
							Utils.log(String.format("%s - %s(%.0f, %d), Store: %s, Store size before: %.0f",
									assemblyLineName, p.toString(), p.size(), p.cookTime().getSeconds(),
									storeQueue.getKey().toString(), storeQueue.getKey().size()));
							storeQueue.getKey().put(p);
							storeQueue.getValue().add(new ProductFromLine(assemblyLine, p));
							p = null;
							break;

						} catch (CapacityExceededException e) {
							continue;
						}
					}
					if (p == null) {
						break;
					}
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException ie) {
						Utils.log(String.format("%s blocking task timer interrupted!", assemblyLine.toString()));
					}

				}
			} else {
				// free oven found starting cooking process
				Utils.log(assemblyLineName + " - Spawning new CookTask from AssemblyLineTask! - "
						+ productInOven.peek().getProduct().toString());
				ProductInOven pio = productInOven.poll();
				if (pio != null) {
					ovenExecutor.submit(new CookTask(pio, assemblyLine));
				}
			}
		}
		
		// waiting for all products on assembly line to cook
		while (assemblyLine.inputQueueSize() != assemblyLine.outputQueueSize()) {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException ie) {
				Utils.log(String.format("%s blocking task timer interrupted!", assemblyLine.toString()));
			}
		}
		
		//shutdown cooking executor
		ovenExecutor.shutdown();
		Utils.log(assemblyLineName + " - AssemblyLineTask executor shutdown executed!");
		return assemblyLineName;
	}

}
