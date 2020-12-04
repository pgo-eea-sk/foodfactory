package foodfactory;

import java.time.LocalTime;

/**
 * 
 * 
 * Helper class which stores information about which product is cooking in which
 * oven
 * 
 * @author Peter Golian
 *
 */
public class ProductInOven {
	private Oven oven;
	private Product product;
	private LocalTime startCooking;

	ProductInOven(Oven oven, Product product, LocalTime start) {
		this.oven = oven;
		this.product = product;
		this.startCooking = start;
	}

	public Oven getOven() {
		return oven;
	}

	public void setOven(Oven oven) {
		this.oven = oven;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public LocalTime getStartCooking() {
		return startCooking;
	}

	public void setStartCooking(LocalTime startCooking) {
		this.startCooking = startCooking;
	}

}
