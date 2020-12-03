package foodfactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CookTask implements Callable<AssemblyLineStage> {

	ProductInOven pio;
	AssemblyLineStage assemblyLine;

	CookTask(ProductInOven productInOven, AssemblyLineStage als) {
		pio = productInOven;
		assemblyLine = als;
	}

	@Override
	public AssemblyLineStage call() {
		Utils.log(String.format("Start cooking Task: %s, %s(%.0f, %d), %s, Oven size during cooking: %.0f",
				Thread.currentThread().getName(), pio.getProduct().getProductName(), pio.getProduct().size(), pio.getProduct().cookTime().getSeconds(),
				pio.getOven().getOvenName(), pio.getOven().size()));
		long timeToCook = pio.getProduct().cookTime().getSeconds();
		while (timeToCook > 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				Utils.log(String.format("%s timer interrupted!", pio.getOven().getOvenName()));
			}
			timeToCook--;
		}
		pio.getOven().take(pio.getProduct());
		Utils.log(String.format(
				"Cooking task: %s, %s(%.0f, %d) taken from the oven after %d seconds. Current %s capacity is: %.0f",
				Thread.currentThread().getName(), pio.getProduct().getProductName(), pio.getProduct().size(), pio.getProduct().cookTime().getSeconds(),
				Duration.between(pio.getStartCooking(), LocalTime.now()).getSeconds(),
				pio.getOven().getOvenName(), pio.getOven().size()));
		assemblyLine.putAfter(pio.getProduct());
		Utils.log(String.format("End Cooking task: %s finished. Cooked %s returned to %s.",
				Thread.currentThread().getName(), pio.getProduct().getProductName(), assemblyLine.getAssemblyLineName()));
		pio = null;
		return assemblyLine;
	}
}
