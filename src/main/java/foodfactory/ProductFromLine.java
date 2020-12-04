package foodfactory;

/**
 * 
 * @author Peter Golian
 * Helper class to store information which product originates from which line
 *
 */
public class ProductFromLine {
	private AssemblyLineStage assemblyLine;
	private Product product;

	ProductFromLine(AssemblyLineStage assemblyLine, Product product) {
		this.assemblyLine = assemblyLine;
		this.product = product;
	}

	public AssemblyLineStage getAssemblyLine() {
		return assemblyLine;
	}

	public void setAssemblyLine(AssemblyLineStage assemblyLine) {
		this.assemblyLine = assemblyLine;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}

