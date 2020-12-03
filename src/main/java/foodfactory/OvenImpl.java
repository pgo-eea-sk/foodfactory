package foodfactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OvenImpl implements Oven {

	private double currentSize;
	private long turnedOn = -1; // -1 turned off, positive value - turned for amount of seconds, -2 turned on
								// until turn off

	OvenImpl(double initialSize) {
		currentSize = initialSize;
	}

	public double size() {
		return currentSize;
	}

	public synchronized void put(Product product) throws CapacityExceededException {
		if (product.size() > currentSize) {
			throw new CapacityExceededException("Maximum oven capacity exceeded with Product[" + product.toString() + "]!");
		} else {
			currentSize -= product.size();
			if (product.cookTime().getSeconds() > turnedOn) {
				turnedOn = product.cookTime().getSeconds();
				System.out.printf("Oven (%s) turned on time extended to %d seconds.\n", this.toString(), turnedOn);
			}
		}
		if (turnedOn == -2) {
//			turnOn(product.cookTime());
			turnOn();
		}

	}

	public synchronized void take(Product product) {
		currentSize += product.size();
		if (turnedOn == 0) {
			turnOff();
		}
	}

	public void turnOn() {
		turnedOn = -2;
		System.out.printf("Oven (%s) turned on.\n", this.toString());
	}

	public void turnOn(Duration duration) {
		turnedOn = duration.getSeconds();
		System.out.printf("Oven (%s) turned on for %d seconds.\n", this.toString(), turnedOn);
		timer();
	}

	public void turnOff() {
		turnedOn = -1;
		System.out.printf("Oven (%s) turned off.\n", this.toString());
	}

	private void timer() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			while (turnedOn > 0) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					System.out.printf("Oven (%s) timer interrupted!\n", Thread.currentThread().getName());
				}
				turnedOn--;
			}
			turnOff();
		});
	}

	private void productTimer(Product p) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			long timeToCook = p.cookTime().getSeconds();
			while (timeToCook > 0) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					System.out.printf("Oven (%s) timer interrupted!\n", Thread.currentThread().getName());
				}
				timeToCook--;
			}

		});
	}

}
