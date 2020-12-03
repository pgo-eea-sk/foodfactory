package foodfactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StoreImpl implements Store {
	
	BlockingQueue<Product> store;
	double storeSize;
	
	StoreImpl(double initialSize) {
		store = new LinkedBlockingQueue<Product>();
		storeSize = initialSize;
	}

	public void put(Product product) throws CapacityExceededException {
		if (product.size() > storeSize) {
			throw new CapacityExceededException("Store: " + this.toString() + " is full.");
		} else {
			try {
				store.put(product);
				storeSize -= product.size();
			} catch (InterruptedException e) {
				
			}
		}

	}

	public Product take() {
		Product p = null;
		p = (Product) store.poll();
		storeSize += p.size();
		return p;
	}

	public void take(Product product) {
		store.remove(product);
		storeSize += product.size();
	}
	
	public double size() {
		return storeSize;
	}

}
