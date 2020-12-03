package foodfactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FoodFactoryMain {

	public static final String ASSEMBLY_LINE_NAME = "Assembly line ";
	public static final String OVEN_NAME = "Oven ";
	public static final String STORE_NAME = "Store ";
	public static final String PRODUCT_NAME = "Product ";

	public static List<Oven> ovens;
	public static List<Store> stores;
	private static int linesCount;
	private static int productCounter = 1;

	public static void main(String[] args) {
		readConfig();
		List<Future<AssemblyLineResults>> futuresList = new ArrayList<Future<AssemblyLineResults>>();

		ExecutorService executor = Executors.newFixedThreadPool(linesCount);
		for (int i = 0; i < linesCount; i++) {
			Utils.log("Putting products to cook on assembly line: " + String.valueOf(i + 1));
			futuresList.add(executor
					.submit(new AssemblyLineTask(generateProductList(), ASSEMBLY_LINE_NAME + String.valueOf(i + 1))));
		}
		while (true) {
			Boolean allTasksFinished = true;
			for (Future<AssemblyLineResults> future : futuresList) {
				if (!future.isDone()) {
					allTasksFinished = false;
				}
			}
			if (allTasksFinished)
				break;
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				Utils.log("FoodFactoryMain: InterruptedException");
				e.printStackTrace();
			}
		}
//		for (Future<AssemblyLineResults> future : futuresList) {
//			AssemblyLineResults alr;
//			try {
//				alr = future.get();
//				for (Product p : alr.getLineProducts()) {
//					System.out.printf("%s - finished Product(%f, %d)[%s]\n", alr.getAssemblyLineName(), p.size(), p.cookTime(), p.toString());
//				}
//			} catch (InterruptedException | ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		Utils.log("FoodFactory executor shutdown!");
		executor.shutdown();
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
			List<Integer> ovensList = Arrays.asList(propOvens.split(",", -1)).stream().mapToInt(Integer::parseInt)
					.boxed().collect(Collectors.toList());
			ovens = new ArrayList<Oven>();
			for (int i = 0; i < ovensList.size(); i++) {
				ovens.add(new OvenImpl(ovensList.get(i), OVEN_NAME + String.valueOf(i + 1)));
			}

			String propStores = prop.getProperty("stores");
			Utils.log("Configured stores sizes: " + propStores);
			List<Integer> storesList = Arrays.asList(propStores.split(",", -1)).stream().mapToInt(Integer::parseInt)
					.boxed().collect(Collectors.toList());
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
		return r.nextInt(2) + 1;
	}

	private static Product generateProduct() {
		return new ProductImpl(generateNumber(), generateNumber(), PRODUCT_NAME + String.valueOf(productCounter++));
	}

	private static List<Product> generateProductList() {
		// TODO
		int productListSize = generateNumber();
		List<Product> generatedProductList = new ArrayList<Product>();
		for (int i = 0; i < productListSize; i++) {
			Product p = generateProduct();
			Utils.log(String.format("\tPlacing %s(%.0f, %d) on assembly line.", p.getProductName(), p.size(), p.cookTime().getSeconds()));
			generatedProductList.add(p);
		}
		return generatedProductList;
	}
}
