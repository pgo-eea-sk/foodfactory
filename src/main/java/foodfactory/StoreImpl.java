package foodfactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StoreImpl implements Store {

	BlockingQueue<Product> store;
	double storeSize;
	double initialSize;
	String storeName;

	StoreImpl(double initialSize, String name) {
		store = new LinkedBlockingQueue<Product>();
		storeSize = initialSize;
		this.initialSize = initialSize;
		storeName = name;
	}

	public void put(Product product) throws CapacityExceededException {
		if (product.size() > storeSize) {
			throw new CapacityExceededException(storeName + " is full.");
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

	@Override
	public String toString() {
		return String.format("%s(%.0f)", storeName, initialSize);
	}

	@Override
	public synchronized List<Product> getStoreProducts() {
		Iterator<Product> i = store.iterator();
		List<Product> productsInStore = new ArrayList<Product>();
		while (i.hasNext()) {
			productsInStore.add(i.next());
		}
		return productsInStore;
	}

}
