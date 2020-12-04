package foodfactory;

import java.time.Duration;

public class ProductImpl implements Product {

	private final double productSize;
	private final Duration cookTime;
	private final String productName;

	ProductImpl(double size, int cookTime, String name) {
		productSize = size;
		this.cookTime = Duration.ofSeconds(cookTime);
		productName = name;
	}

	@Override
	public double size() {
		return productSize;
	}

	@Override
	public Duration cookTime() {
		return cookTime;
	}

	@Override
	public String toString() {
		return String.format("%s(%.0f, %d)", productName, productSize, cookTime.getSeconds());
	}
}
