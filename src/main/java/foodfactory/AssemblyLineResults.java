package foodfactory;

import java.util.ArrayList;
import java.util.List;

public class AssemblyLineResults {
	private String assemblyLineName;
	private List<Product> lineProducts;
	
	public AssemblyLineResults(String name) {
		assemblyLineName = name;
		lineProducts = new ArrayList<Product>();
	}

	public String getAssemblyLineName() {
		return assemblyLineName;
	}

	public void setAssemblyLineName(String assemblyLineName) {
		this.assemblyLineName = assemblyLineName;
	}

	public List<Product> getLineProducts() {
		return lineProducts;
	}

	public void setLineProducts(List<Product> lineProducts) {
		this.lineProducts = lineProducts;
	}
}
