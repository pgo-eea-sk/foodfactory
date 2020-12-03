package foodfactory;

import java.time.Duration;

public class ProductImpl implements Product {
	
	private final double productSize;
	private final Duration cookTime;

	ProductImpl(double size, int cookTime) {
		productSize = size;
		this.cookTime = Duration.ofSeconds(cookTime);
	}
	@Override
	public double size() {
		return productSize;
	}

	@Override
	public Duration cookTime() {
		return cookTime;
	}

}
