package foodfactory;

public class CapacityExceededException extends Exception {

	private static final long serialVersionUID = 3513593354084172286L;

	CapacityExceededException(String message) {
		super(message);
	}
}
