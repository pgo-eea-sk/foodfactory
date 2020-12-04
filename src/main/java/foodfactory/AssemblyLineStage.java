package foodfactory;

import java.util.List;

/**
 * This represents an assembly line stage of the factory. Implementations of
 * this class should be thread-safe
 */
public interface AssemblyLineStage {

	/**
	 * Put the specified product to the assembly line to continue in the next stage.
	 * @param ​product
	 */
	void putAfter(Product product);

	/**
	 * @return Takes the next product available from the assembly line.
	 */
	Product take();

	/**
	 * @return product available from the assembly line.
	 */
	 Product getHeadProduct();
	 
	/**
	 * @return Are there any uncooked products on the assembly line?
	 */
	boolean isLineEmpty();

	/**
	 * @return remaining number of uncooked products on assembly line
	 */
	int remainig();

	/**
	 * @return initial number of products on assembly line
	 */
	int inputQueueSize();

	/**
	 * @return number of cooked products on assembly line
	 */
	int outputQueueSize();

	/**
	 * 
	 * @return List of products in input queue
	 */
	List<Product> getInputLineProducts();

	/**
	 * 
	 * @return List of products in output queue
	 */
	List<Product> getOutputLineProducts();
}