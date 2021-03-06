package foodfactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * 
 * This class implements cooking process. It waits for product to cook and put
 * the cooked product back to assembly line
 * 
 * @author Peter Golian
 */

public class CookTask implements Runnable {

	ProductInOven pio;
	AssemblyLineStage assemblyLine;

	CookTask(ProductInOven productInOven, AssemblyLineStage als) {
		pio = productInOven;
		assemblyLine = als;
	}

	@Override
	public void run() {
		Utils.log(String.format("Start cooking Task: %s, %s, Oven size during cooking: %.0f",
				pio.getProduct().toString(), pio.getOven().toString(), pio.getOven().size()));
		long timeToCook = pio.getProduct().cookTime().getSeconds();
		while (timeToCook > 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				Utils.log(String.format("%s timer interrupted!", pio.getOven().toString()));
			}
			timeToCook--;
		}
		pio.getOven().take(pio.getProduct());
		Utils.log(String.format("Cooking task: %s taken from the oven after %d seconds. Current %s capacity is: %.0f",
				pio.getProduct().toString(), Duration.between(pio.getStartCooking(), LocalTime.now()).getSeconds(),
				pio.getOven().toString(), pio.getOven().size()));
		assemblyLine.putAfter(pio.getProduct());
		Utils.log(String.format("Finished cooking task. Cooked %s returned to %s.", pio.getProduct().toString(),
				assemblyLine.toString()));
		pio = null;
	}
}
