package foodfactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class AssemblyLineTask implements Callable<AssemblyLineResults> {

	private List<Product> productsOnLine;
	private String assemblyLineName;
	private AssemblyLineResults assemblyLineResults;

	AssemblyLineTask(List<Product> productsToCook, String name) {
		productsOnLine = new ArrayList<Product>();
		productsOnLine.addAll(productsToCook);
		assemblyLineName = name;
		assemblyLineResults = new AssemblyLineResults(name);
	}

	@Override
	public AssemblyLineResults call() {
		AssemblyLineStage assemblyLine = new AssemblyLineStageImpl(productsOnLine, assemblyLineName);
		ExecutorService executor = Executors.newFixedThreadPool(productsOnLine.size());
//		ExecutorService executor = Executors.newCachedThreadPool();
		Product p = null;
		BlockingQueue<ProductInOven> productInOven = new LinkedBlockingDeque<ProductInOven>();
		BlockingQueue<ProductInStore> productInStore = new LinkedBlockingDeque<ProductInStore>();
		List<Future<AssemblyLineStage>> futuresList = new ArrayList<Future<AssemblyLineStage>>();
		while (true) {
			if (p == null) {
				p = assemblyLine.take();
				if (p == null)
					break;
			}
			for (Oven oven : FoodFactoryMain.ovens) {
				try {
					Utils.log(String.format("%s - %s(%.0f, %d), %s, Oven size before: %.0f", assemblyLineName, p.getProductName(), p.size(), p.cookTime().getSeconds(), oven.getOvenName(), oven.size()));
					oven.put(p);
					productInOven.add(new ProductInOven(oven, p, LocalTime.now()));
					p = null;
					break;
				} catch (CapacityExceededException e) {
					Utils.log(e.getMessage());
					continue;
				}
			}
			if (p != null) {
				StoreTask st = null;
				Future<AssemblyLineStage> result = null;
				while (true) {
					if (p == null) {
						p = assemblyLine.take();
						if (p == null)
							break;
					}
					for (int i = 0; i < FoodFactoryMain.stores.size(); i++) {
						try {
							Utils.log(String.format("%s - %s(%.0f, %d), Store: %s, Store size before: %.0f", assemblyLineName, p.getProductName(), p.size(), p.cookTime().getSeconds(), FoodFactoryMain.stores.get(i).toString(), FoodFactoryMain.stores.get(i).size()));
							FoodFactoryMain.stores.get(i).put(p);
							productInStore.add(new ProductInStore(FoodFactoryMain.stores.get(i), p));
							p = null;
							break;

						} catch (CapacityExceededException e) {
							if (i == (FoodFactoryMain.stores.size() - 1)) {
								i = 0;
							}
							continue;
						}
					}
					if(p!=null) {
						continue;
					}
					if (st == null) {
						st = new StoreTask(productInStore, assemblyLine);
					}
					if (result != null && result.isDone()) {
						break;
					}
					Utils.log(assemblyLineName + " - Spawning new StoreTask from AssemblyLineTask! " + productInStore.peek().getProduct().getProductName());
					result = executor.submit(st);
					futuresList.add(result);
				}
			}
			try {
				Utils.log(assemblyLineName + " - Spawning new CookTask from AssemblyLineTask! - " + productInOven.peek().getProduct().getProductName());
				futuresList.add(executor.submit(new CookTask(productInOven.take(), assemblyLine)));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		while (true) {
			Boolean allTasksFinished = true;
			for (Future<AssemblyLineStage> future : futuresList) {
				if (!future.isDone()) {
					allTasksFinished = false;
				}
			}
			if(allTasksFinished) break;
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				Utils.log("ERROR");
				e.printStackTrace();
			}
		}
		for (Future<AssemblyLineStage> future : futuresList) {
			try {
				assemblyLineResults.getLineProducts().add(future.get().takeFinished());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		executor.shutdown();
		Utils.log(assemblyLineName + " - AssemblyLineTask executor shutdown executed!");
		return assemblyLineResults;
	}

}
