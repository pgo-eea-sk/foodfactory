package foodfactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CookTask implements Callable<Integer> {

	ProductInOven pio;
	AssemblyLineStage assemblyLine;

	CookTask(ProductInOven productInOven, AssemblyLineStage als) {
		pio = productInOven;
		assemblyLine = als;
	}

	@Override
	public Integer call() {
		System.out.printf("Start cooking Task: %s, Product(%f, %d)[%s], Oven: %s, Oven size during cooking: %f\n",
				Thread.currentThread().getName(), pio.getProduct().size(), pio.getProduct().cookTime().getSeconds(),
				pio.getProduct().toString(), pio.getOven().toString(), pio.getOven().size());
		long timeToCook = pio.getProduct().cookTime().getSeconds();
		while (timeToCook > 0) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				System.out.printf("Oven (%s) timer interrupted!\n", Thread.currentThread().getName());
			}
			timeToCook--;
		}
		pio.getOven().take(pio.getProduct());
		System.out.printf(
				"Cooking task: %s, Product(%f, %d)[%s} taken from the oven after %d seconds. Current oven %s size is: %f\n",
				Thread.currentThread().getName(), pio.getProduct().size(), pio.getProduct().cookTime().getSeconds(),
				pio.getProduct().toString(), Duration.between(pio.getStartCooking(), LocalTime.now()).getSeconds(),
				pio.getOven().toString(), pio.getOven().size());
		assemblyLine.putAfter(pio.getProduct());
		System.out.printf("End Cooking task: %s finished. Cooked product returned to queue.\n",
				Thread.currentThread().getName());
		pio = null;
		return 0;
	}
}
