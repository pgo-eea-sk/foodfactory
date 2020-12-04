package foodfactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {

	private static final String TIME_FORMAT = "HH:mm:ss:SSS";

	public static void specialLog(String message) {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
		System.out.println(sdf.format(new Date()) + " - " + Thread.currentThread().getName() + " - " + message);
	}

	public static void log(String message) {
		if (FoodFactoryMain.loggingEnabled) {
			SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
			System.out.println(sdf.format(new Date()) + " - " + Thread.currentThread().getName() + " - " + message);
		}
	}

	public static void niceOutput(List<AssemblyLineStage> lines) {
		clearConsole();
		for (AssemblyLineStage als : lines) {
			System.out.println(als.toString() + ": " + createProductList(als.getInputLineProducts()));
		}
		for (Oven oven : FoodFactoryMain.ovens) {
			System.out.println(oven.toString() + ": " + createProductList(oven.getProductsInOven()));
		}
		for (Store store : FoodFactoryMain.stores) {
			System.out.println(store.toString() + ": " + createProductList(store.getStoreProducts()));
		}
		for (AssemblyLineStage als : lines) {
			System.out.println(als.toString() + ": " + createProductList(als.getOutputLineProducts()));
		}
	}

	public final static void clearConsole() {
		try {
			if (System.getProperty("os.name").contains("Windows"))
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			else
				Runtime.getRuntime().exec("clear");
		} catch (IOException | InterruptedException ex) {
		}
	}

	private static synchronized String createProductList(List<Product> productList) {
		boolean first = true;
		StringBuilder products = new StringBuilder();
		for (Product p : productList) {
			if (first) {
				products.append(p.toString());
				first = false;
			} else {
				products.append(", ").append(p.toString());
			}
		}
		return products.toString();
	}
}
