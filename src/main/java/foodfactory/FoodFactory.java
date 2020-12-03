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

public class FoodFactory {

	public static List<Oven> ovens;
	public static List<Store> stores;
	private static int linesCount;

	public static final int DEFAULT_OVEN_COUNT = 5;
	public static final int DEFAULT_STORE_COUNT = 4;

	public static void main(String[] args) {
//		parseArgs(args);
		readConfig();
		List<Future<Integer>> futuresList = new ArrayList<Future<Integer>>();

		ExecutorService executor = Executors.newFixedThreadPool(linesCount);
		for(int i = 0; i < linesCount; i++) {
			System.out.println("Putting products to cook on assembly line: " + String.valueOf(i+1));
			futuresList.add(executor.submit(new AssemblyLineTask(generateProductList())));
		}
		while (true) {
			Boolean allTasksFinished = true;
			for (Future<Integer> future : futuresList) {
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
		System.out.println("FoodFactory executor shutdown!");
		executor.shutdown();
	}
	
	private static void parseArgs(String[] args) {
		// TODO
		boolean isSwitch = false, isOven = false, isStore = false;

		for (String arg : args) {
			if (arg.equals("-o")) {
				isSwitch = true;
				isOven = true;
			} else if (arg.equals("-s")) {
				isSwitch = true;
				isStore = true;
			} else if (isOven) {
				isOven = false;
				int ovenCount;
				try {
					ovenCount = Integer.decode(arg);
				} catch (NumberFormatException e) {
					System.out.println("WARN: " + arg + " is not a valid number of ovens, using default value: "
							+ DEFAULT_OVEN_COUNT + ".");
					ovenCount = DEFAULT_OVEN_COUNT;
				}
				ovens = new ArrayList<Oven>();
			} else if (isStore) {
				isStore = false;
				int storeCount;
				try {
					storeCount = Integer.decode(arg);
				} catch (NumberFormatException e) {
					System.out.println("WARN: " + arg + " is not a valid number of stores, using default value: "
							+ DEFAULT_STORE_COUNT + ".");
					storeCount = DEFAULT_STORE_COUNT;
				}
				stores = new ArrayList<Store>();
			}
		}
	}

	private static void readConfig() {
		try (InputStream input = FoodFactory.class.getClassLoader().getResourceAsStream("config.properties")) {
			Properties prop = new Properties();

			if (input == null) {
				System.out.println("Sorry, unable to find config.properties");
				return;
			}

			prop.load(input);
			linesCount = Integer.valueOf(prop.getProperty("assemblyLines", "3"));
			System.out.println("Configured lines count: " + linesCount);
			String propOvens = prop.getProperty("ovens");
			System.out.println("Configured ovens sizes: " + propOvens);
			List<Integer> ovensList = Arrays.asList(propOvens.split(",", -1)).stream().mapToInt(Integer::parseInt)
					.boxed().collect(Collectors.toList());
			ovens = ovensList.stream().map(i -> new OvenImpl(i)).collect(Collectors.toList());

			String propStores = prop.getProperty("stores");
			System.out.println("Configured stores sizes: " + propStores);
			List<Integer> storesList = Arrays.asList(propStores.split(",", -1)).stream().mapToInt(Integer::parseInt)
					.boxed().collect(Collectors.toList());
			stores = storesList.stream().map(i -> new StoreImpl(i)).collect(Collectors.toList());

		} catch (IOException ex) {
			System.out.println("Error readin config.properties");
			System.exit(0);
		}
	}
	
	private static int generateNumber() {
		Random r = new Random();
        return r.nextInt(2) + 1;
	}
	
	private static Product generateProduct() {
		return new ProductImpl(generateNumber(), generateNumber());
	}
	
	private static List<Product> generateProductList() {
		//TODO
		int productListSize = generateNumber();
//		int productListSize = 3;
		List<Product> generatedProductList = new ArrayList<Product>();
		for(int i = 0; i < productListSize; i++) {
			Product p = generateProduct();
			System.out.printf("\tPlacing Product(%f, %d)[%s] on assembly line.\n", p.size(), p.cookTime().getSeconds(), p.toString());
			generatedProductList.add(p);
		}
		return generatedProductList;
	}
}
