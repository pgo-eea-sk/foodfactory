package foodfactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class StoreTask implements Callable<AssemblyLineStage> {

	BlockingQueue<ProductInStore> pisq;
	AssemblyLineStage assemblyLine;

	StoreTask(BlockingQueue<ProductInStore> productsInStore, AssemblyLineStage als) {
		pisq = productsInStore;
		assemblyLine = als;
	}

	@Override
	public AssemblyLineStage call() {
		BlockingQueue<ProductInOven> productInOven = new LinkedBlockingDeque<ProductInOven>();
		ExecutorService executor = Executors.newCachedThreadPool();
		List<Future<AssemblyLineStage>> futuresList = new ArrayList<Future<AssemblyLineStage>>();
		ProductInStore pis = null;
		Product p = null;
		while (true) {
			if (pis == null) {
				pis = pisq.poll();
				if (pis == null) {
					break;
				} else {
					p = pis.getProduct();
				}
			}
			for (int i = 0; i < FoodFactoryMain.ovens.size(); i++) {
				try {
					Utils.log(String.format("%s(%.0f, %d) from store, %s, Oven size before: %.0f", p.getProductName(), p.size(),
							p.cookTime().getSeconds(), FoodFactoryMain.ovens.get(i).getOvenName(),
							FoodFactoryMain.ovens.get(i).size()));
					FoodFactoryMain.ovens.get(i).put(p);
					pis.getStore().take(p);
					pis = null;
					productInOven.add(new ProductInOven(FoodFactoryMain.ovens.get(i), p, LocalTime.now()));
					break;
				} catch (CapacityExceededException e) {
					Utils.log(e.getMessage());
					if(i == FoodFactoryMain.ovens.size() -1) {
						i = -1;
					}
					continue;
				}
			}
			if(pis != null) {
				continue;
			}
			try {
				Utils.log(String.format("Spawning new CookTask from StoreTask!" + productInOven.peek().getProduct()));
				futuresList.add(executor.submit(new CookTask(productInOven.take(), assemblyLine)));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Boolean allTasksFinished = true;
		while (true) {
			for (Future<AssemblyLineStage> future : futuresList) {
				if (!future.isDone()) {
					allTasksFinished = false;
				}
			}
			if(allTasksFinished) break;
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Utils.log(String.format("StoreTask executor shutdown!"));
		executor.shutdown();
		return assemblyLine;
	}

}
