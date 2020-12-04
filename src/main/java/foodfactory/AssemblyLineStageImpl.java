package foodfactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AssemblyLineStageImpl implements AssemblyLineStage {

	private BlockingQueue<Product> assemblyLine;
	private BlockingQueue<Product> cookedAssemblyLine;
	private String assemblyLineName;
	private int productCount;

	public AssemblyLineStageImpl(List<Product> products, String name) {
		assemblyLine = new LinkedBlockingQueue<Product>();
		assemblyLine.addAll(products);
		cookedAssemblyLine = new LinkedBlockingQueue<Product>();
		assemblyLineName = name;
		productCount = products.size();
	}

	@Override
	public void putAfter(Product product) {
		cookedAssemblyLine.add(product);

	}

	@Override
	public Product take() {
		return assemblyLine.poll();
	}

	public double getHeadProductSize() {
		return assemblyLine.peek().size();
	}

	public boolean isLineEmpty() {
		return assemblyLine.isEmpty();
	}

	@Override
	public String toString() {
		return assemblyLineName;
	}

	public int remainig() {
		return assemblyLine.size();
	}

	public int inputQueueSize() {
		return productCount;
	}

	public int outputQueueSize() {
		return cookedAssemblyLine.size();
	}

	public synchronized List<Product> getInputLineProducts() {
		Iterator<Product> i = assemblyLine.iterator();
		List<Product> productsOnInput = new ArrayList<Product>();
		while (i.hasNext()) {
			productsOnInput.add(i.next());
		}
		return productsOnInput;
	}

	public synchronized List<Product> getOutputLineProducts() {
		Iterator<Product> i = cookedAssemblyLine.iterator();
		List<Product> productsOnOutput = new ArrayList<Product>();
		while (i.hasNext()) {
			productsOnOutput.add(i.next());
		}
		return productsOnOutput;
	}
}
