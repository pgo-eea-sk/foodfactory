package foodfactory;

import java.util.List;

/**
* The store where to put the products if the oven is not available. This class is thread safe.
*/
public interface Store {

	/**
	* Put a product in this store, if there is no space left in the store, it will block
	* until enough space frees up. This operation will put the products in FIFO order
	* ​@param ​product ​The Product to put in this Store
	*/
	void put(Product product) throws CapacityExceededException;
	
	/**
	 * Take the next element that has to be processed respecting FIFO
	 * ​@return
	 */
	Product take();

	/**
	 * Take the specified Product from the Store
	 * ​@param ​product
	 */
	void take(Product product);
	
	/**
	 * This returns the size of the store in cm2. As a simplification of the problem, assume that the
	 * sizes of the products can be summed, and that value should not exceed the size of the oven. Otherwise an
	 * exception is thrown if adding a product.
	 * ​@return
	 */ 
	double size();
	
	/**
	 * 
	 * @return List of products in store queue
	 */
	List<Product> getStoreProducts();

}