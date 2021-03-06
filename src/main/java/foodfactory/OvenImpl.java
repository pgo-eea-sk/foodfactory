package foodfactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OvenImpl implements Oven {

	private double currentSize;
	private double initialSize;
	private long turnedOn = -1; // -1 turned off, positive value - turned for amount of seconds, -2 turned on
								// until turn off
	private String ovenName;
	private List<Product> productsInOven;

	OvenImpl(double initialSize, String name) {
		currentSize = initialSize;
		this.initialSize = initialSize;
		ovenName = name;
		productsInOven = new ArrayList<Product>();
	}

	public double size() {
		return currentSize;
	}

	public synchronized void put(Product product) throws CapacityExceededException {
		if (product.size() > currentSize) {
			throw new CapacityExceededException(
					ovenName + " - Maximum oven capacity exceeded with " + product.toString() + "!");
		} else {
			productsInOven.add(product);
			currentSize -= product.size();
			if (product.cookTime().getSeconds() > turnedOn) {
				turnedOn = product.cookTime().getSeconds();
			}
		}
		if (turnedOn == -2) {
			turnOn();
		}

	}

	public synchronized void take(Product product) {
		productsInOven.remove(product);
		currentSize += product.size();
		if (turnedOn == 0) {
			turnOff();
		}
	}

	public void turnOn() {
		turnedOn = -2;
		Utils.log(String.format("%s - turned on.", ovenName));
	}

	public void turnOn(Duration duration) {
		turnedOn = duration.getSeconds();
		Utils.log(String.format("%s turned on for %d seconds.", ovenName, turnedOn));
		timer();
	}

	public void turnOff() {
		turnedOn = -1;
		Utils.log(String.format("%s - turned off.", ovenName));
	}

	private void timer() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			while (turnedOn > 0) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					Utils.log(String.format("%s - timer interrupted!", ovenName));
				}
				turnedOn--;
			}
			turnOff();
		});
	}

	@Override
	public String toString() {
		return String.format("%s(%.0f)", ovenName, initialSize);
	}

	@Override
	public synchronized List<Product> getProductsInOven() {
		return productsInOven;
	}
}
