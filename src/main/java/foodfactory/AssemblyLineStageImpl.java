package foodfactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AssemblyLineStageImpl implements AssemblyLineStage {

	private BlockingQueue<Product> assemblyLine;
	private BlockingQueue<Product> cookedAssemblyLine;
	private String assemblyLineName;
	
	public AssemblyLineStageImpl(List<Product> products, String name) {
		assemblyLine = new LinkedBlockingQueue<Product>();
		cookedAssemblyLine = new LinkedBlockingQueue<Product>();
		assemblyLine.addAll(products);
		assemblyLineName = name;
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
	
	public Product takeFinished() {
		return cookedAssemblyLine.poll();
	}
	
	public String getAssemblyLineName() {
		return assemblyLineName;
	}
}
