package foodfactory;

/** This represents an assembly line stage of the factory. Implementations of this class should be thread-safe
*/
public interface AssemblyLineStage {

	/**
	 * Put the specified product to the assembly line to continue in the next stage.
	 * ​@param ​product
	 */
	void putAfter(Product product);

	/**
	 * Takes the next product available from the assembly line.
	 * ​@return
	 */ 
	Product take();
	
	/**
	 * Returns the size of next product available from the assembly line.
	 * ​@return
	 */ 
	double getHeadProductSize();
	
	/**
	 * Are there any uncooked products on the assembly line?
	 * ​@return
	 */ 
	public boolean isLineEmpty();
}