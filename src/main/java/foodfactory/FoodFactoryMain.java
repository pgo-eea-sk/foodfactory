package foodfactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 
 * Main class, which spawns process for every assembly line and every store and
 * fills assembly lines with test data. MAX_NUMBER_FROM_GENERATOR is maximum
 * cout of products on one line, maximum size of the product and maximum cooking
 * time in seconds.
 * 
 * @author Peter Golian
 */
public class FoodFactoryMain {

	public static final String ASSEMBLY_LINE_NAME = "Assembly line ";
	public static final String OVEN_NAME = "Oven ";
	public static final String STORE_NAME = "Store ";
	public static final String PRODUCT_NAME = "Product ";
	private static final int MAX_NUMBER_FROM_GENERATOR = 10;

	public static List<Oven> ovens;
	public static List<Store> stores;
	public static int productCounter = 1;
	public static boolean loggingEnabled = false;
	private static int linesCount;

	public static void main(String[] args) {
		if (args.length == 1 && args[0].equals("-log")) {
			loggingEnabled = true;
		}
		// reads confog file
		readConfig();

		boolean okFlag = false;
		for (Oven oven : ovens) {
			if (MAX_NUMBER_FROM_GENERATOR <= oven.size()) {
				okFlag = true;
			}
		}
		if (!okFlag) {
			Utils.specialLog(String.format(
					"Maximum product size is set to %d. System will probably generate bigger products than is the biggest oven size! Please add bigger oven to config file! Shutting down!",
					MAX_NUMBER_FROM_GENERATOR));
			System.exit(0);
		}
		okFlag = false;
		for (Store store : stores) {
			if (MAX_NUMBER_FROM_GENERATOR <= store.size()) {
				okFlag = true;
			}
		}
		if (!okFlag) {
			Utils.specialLog(String.format(
					"Maximum product size is set to %d. System will probably generate bigger products than is the biggest store size! Please add bigger store to config file! Shutting down!",
					MAX_NUMBER_FROM_GENERATOR));
			System.exit(0);
		}

		ExecutorService alExecutor = Executors.newFixedThreadPool(linesCount);
		ExecutorService storeExecutor = Executors.newFixedThreadPool(stores.size());

		// create queues for stores and spawn processes for them
		Map<Store, BlockingQueue<ProductFromLine>> storeQueues = new HashMap<Store, BlockingQueue<ProductFromLine>>();
		for (Store store : stores) {
			BlockingQueue<ProductFromLine> storeQueue = new LinkedBlockingQueue<ProductFromLine>();
			storeQueues.put(store, storeQueue);
			storeExecutor.submit(new StoreTask(storeQueue, store));
		}
		List<Future<String>> finishedTasks = new ArrayList<Future<String>>();
		// spawns processes for assembly lines
		List<AssemblyLineStage> assemblyLines = new ArrayList<AssemblyLineStage>();
		for (int i = 0; i < linesCount; i++) {
			Utils.log("Putting products to cook on assembly line: " + String.valueOf(i + 1));
			AssemblyLineStage als = new AssemblyLineStageImpl(generateProductList(),
					ASSEMBLY_LINE_NAME + String.valueOf(i + 1));
			assemblyLines.add(als);
			finishedTasks.add(alExecutor.submit(new AssemblyLineTask(als, storeQueues)));
		}
		ExecutorService outputExecutor = null;
		;
		if (!loggingEnabled) {
			outputExecutor = Executors.newSingleThreadExecutor();
			outputExecutor.submit(() -> {
				while (true) {
					Utils.niceOutput(assemblyLines);
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException ie) {
						break;
					}
				}
			});
		}
		// test if all assembly lines were finished
		for (Future<String> finishedTask : finishedTasks) {
			try {
				finishedTask.get();
			} catch (InterruptedException | ExecutionException e) {
				Utils.log("ERROR waiting until tasks end!");
			}
		}

		// turns off ovens
		for (Oven oven : ovens) {
			oven.turnOff();
		}
		if (!loggingEnabled) {
			outputExecutor.shutdownNow();
		}
		alExecutor.shutdown();

		// sending signal to store queues, they can stop taking from queues and shutdown
		// their executors
		for (Map.Entry<Store, BlockingQueue<ProductFromLine>> storeQueue : storeQueues.entrySet()) {
			storeQueue.getValue().add(new ProductFromLine(null, null));
		}

		// shutdown store executors
		storeExecutor.shutdown();
		Utils.log("FoodFactory executor shutdown!");
	}

	private static void readConfig() {
		try (InputStream input = FoodFactoryMain.class.getClassLoader().getResourceAsStream("config.properties")) {
			Properties prop = new Properties();

			if (input == null) {
				Utils.log("Sorry, unable to find config.properties");
				return;
			}

			prop.load(input);
			linesCount = Integer.valueOf(prop.getProperty("assemblyLines", "3"));
			Utils.log("Configured lines count: " + linesCount);
			String propOvens = prop.getProperty("ovens");
			Utils.log("Configured ovens sizes: " + propOvens);
			List<Integer> ovensList = Arrays.asList(propOvens.split(",", -1)).stream().mapToInt(i -> {
				String s = i.trim();
				return Integer.valueOf(s);
			}).boxed().collect(Collectors.toList());
			ovens = new ArrayList<Oven>();
			for (int i = 0; i < ovensList.size(); i++) {
				ovens.add(new OvenImpl(ovensList.get(i), OVEN_NAME + String.valueOf(i + 1)));
			}

			String propStores = prop.getProperty("stores");
			Utils.log("Configured stores sizes: " + propStores);
			List<Integer> storesList = Arrays.asList(propStores.split(",", -1)).stream().mapToInt(i -> {
				String s = i.trim();
				return Integer.valueOf(s);
			}).boxed().collect(Collectors.toList());
			stores = new ArrayList<Store>();
			for (int i = 0; i < storesList.size(); i++) {
				stores.add(new StoreImpl(storesList.get(i), STORE_NAME + String.valueOf(i + 1)));
			}

		} catch (IOException ex) {
			Utils.log("Error readin config.properties");
			System.exit(0);
		}
	}

	private static int generateNumber() {
		Random r = new Random();
		return r.nextInt(MAX_NUMBER_FROM_GENERATOR) + 1;
	}

	private static Product generateProduct() {
		return new ProductImpl(generateNumber(), generateNumber(), PRODUCT_NAME + String.valueOf(productCounter++));
	}

	private static List<Product> generateProductList() {
		int productListSize = generateNumber();
		List<Product> generatedProductList = new ArrayList<Product>();
		for (int i = 0; i < productListSize; i++) {
			Product p = generateProduct();
			Utils.log(String.format("\tPlacing %s(%.0f, %d) on assembly line.", p.toString(), p.size(),
					p.cookTime().getSeconds()));
			generatedProductList.add(p);
		}
		return generatedProductList;
	}
}
