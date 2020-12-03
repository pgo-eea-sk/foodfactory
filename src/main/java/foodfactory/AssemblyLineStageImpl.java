package foodfactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AssemblyLineStageImpl implements AssemblyLineStage {

	private BlockingQueue<Product> assemblyLine;
	private BlockingQueue<Product> cookedAssemblyLine;
	
	public AssemblyLineStageImpl(List<Product> products) {
		assemblyLine = new LinkedBlockingQueue<Product>();
		cookedAssemblyLine = new LinkedBlockingQueue<Product>();
		assemblyLine.addAll(products);
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
}
