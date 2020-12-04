package foodfactory;

import java.time.LocalTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 
 * Task which reads store queue tekes product from it and tries to put it in the
 * first free oven.
 * 
 * @author Peter Golian
 *
 */
public class StoreTask implements Runnable {

	BlockingQueue<ProductFromLine> pflq;
	Store store;

	StoreTask(BlockingQueue<ProductFromLine> productsFromLine, Store store) {
		pflq = productsFromLine;
		this.store = store;
	}

	@Override
	public void run() {
		Utils.log(String.format("StoreTask for %s started", store.toString()));
		BlockingQueue<ProductInOven> productInOven = new LinkedBlockingDeque<ProductInOven>();
		ExecutorService ovenExecutor = Executors.newFixedThreadPool(FoodFactoryMain.productCounter);
		ProductFromLine pfl = null;
		AssemblyLineStage assemblyLine = null;
		while (true) {
			if (pfl == null) {
				try {
					// taking products from store
					pfl = pflq.take();
				} catch (InterruptedException e) {
					Utils.log(String.format("%s BlockingQueue take() interrupted!", store.toString()));
				}
				// test for signal to shutdown
				if (pfl != null && pfl.getAssemblyLine() == null && pfl.getProduct() == null) {
					break;
				}

			}
			// looping through ovens to find first free with enough free capacity to cook
			// product
			for (int i = 0; i < FoodFactoryMain.ovens.size(); i++) {
				try {
					Utils.log(String.format("%s from store, %s, Oven size before: %.0f", pfl.getProduct().toString(),
							FoodFactoryMain.ovens.get(i).toString(), FoodFactoryMain.ovens.get(i).size()));
					FoodFactoryMain.ovens.get(i).put(pfl.getProduct());
					productInOven
							.add(new ProductInOven(FoodFactoryMain.ovens.get(i), pfl.getProduct(), LocalTime.now()));
					assemblyLine = pfl.getAssemblyLine();
					store.take(pfl.getProduct());
					pfl = null;
					break;
				} catch (CapacityExceededException e) {
					Utils.log(e.getMessage());
					if (i == FoodFactoryMain.ovens.size() - 1) {
						i = -1;
					}
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException ie) {
						Utils.log(String.format("%s blocking task timer interrupted!", store.toString()));
					}

					continue;
				}
			}
//			if (pfl != null) {
//				continue;
//			}

			// Free oven found, spawning cooking task
			Utils.log(String
					.format("Spawning new CookTask from StoreTask! " + productInOven.peek().getProduct().toString()));
			ProductInOven pio = productInOven.poll();
			if (pio != null) {
				ovenExecutor.submit(new CookTask(pio, assemblyLine));
			}
		}

		// shutdown executor after all products are cooked
		ovenExecutor.shutdown();
		Utils.log(String.format("%s - StoreTask executor shutdown!", store.toString()));
	}

}
