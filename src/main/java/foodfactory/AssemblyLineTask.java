package foodfactory;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Task which implements assembly line. It tries to put product in the oven and
 * if there is no free space in the ovens it tries tu put the product in the
 * store, if there is no free store, it halts until some oven or store is free
 * (it prevents situation, that some big product is waiting in store and line is
 * halted for it)
 * 
 * @author Peter Golian
 */
public class AssemblyLineTask implements Callable<String> {

	private AssemblyLineStage assemblyLine;
	private Map<Store, BlockingQueue<ProductFromLine>> storeQueues;

	AssemblyLineTask(AssemblyLineStage assemblyLine, Map<Store, BlockingQueue<ProductFromLine>> storeQueues) {
		this.assemblyLine = assemblyLine;
		this.storeQueues = storeQueues;
	}

	@Override
	public String call() {
		ExecutorService ovenExecutor = Executors.newFixedThreadPool(FoodFactoryMain.productCounter);
		Product p = null;
		BlockingQueue<ProductInOven> productInOven = new LinkedBlockingDeque<ProductInOven>();
		while (true) {
			if (p == null) {
				// taking product from assembly line
				p = assemblyLine.getHeadProduct();
				if (p == null) {
					break;
				}
				Utils.log(String.format("%s taken out", p.toString()));

			}

			// searching for free oven
			for (Oven oven : FoodFactoryMain.ovens) {
				try {
					Utils.log(String.format("%s - %s, %s, Oven size before: %.0f, Remainig products %d",
							assemblyLine.toString(), p.toString(), oven.toString(), oven.size(),
							assemblyLine.remainig()));
					oven.put(p);
					assemblyLine.take();
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
							Utils.log(String.format("%s - %s, Store: %s, Store size before: %.0f",
									assemblyLine.toString(), p.toString(), storeQueue.getKey().toString(),
									storeQueue.getKey().size()));
							storeQueue.getKey().put(p);
							storeQueue.getValue().add(new ProductFromLine(assemblyLine, p));
							assemblyLine.take();
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
				Utils.log(assemblyLine.toString() + " - Spawning new CookTask from AssemblyLineTask! - "
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

		// shutdown cooking executor
		ovenExecutor.shutdown();
		Utils.log(assemblyLine.toString() + " - AssemblyLineTask executor shutdown executed!");
		return assemblyLine.toString();
	}

}
