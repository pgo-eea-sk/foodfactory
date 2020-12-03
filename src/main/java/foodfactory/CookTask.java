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
		System.out.printf("Start cooking Task: %s, Product(%f, %d)[%s], %s, Oven size during cooking: %f\n",
				Thread.currentThread().getName(), pio.getProduct().size(), pio.getProduct().cookTime().getSeconds(),
				pio.getProduct().toString(), pio.getOven().getOvenName(), pio.getOven().size());
		long timeToCook = pio.getProduct().cookTime().getSeconds();
		while (timeToCook > 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				System.out.printf("%s timer interrupted!\n", pio.getOven().getOvenName());
			}
			timeToCook--;
		}
		pio.getOven().take(pio.getProduct());
		System.out.printf(
				"Cooking task: %s, Product(%f, %d)[%s} taken from the oven after %d seconds. Current %s capacity is: %f\n",
				Thread.currentThread().getName(), pio.getProduct().size(), pio.getProduct().cookTime().getSeconds(),
				pio.getProduct().toString(), Duration.between(pio.getStartCooking(), LocalTime.now()).getSeconds(),
				pio.getOven().getOvenName(), pio.getOven().size());
		assemblyLine.putAfter(pio.getProduct());
		System.out.printf("End Cooking task: %s finished. Cooked Product[%s] returned to queue.\n",
				Thread.currentThread().getName(), pio.getProduct().toString());
		pio = null;
		return assemblyLine;
	}
}
