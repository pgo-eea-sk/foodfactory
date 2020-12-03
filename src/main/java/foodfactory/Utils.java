package foodfactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	private static final String TIME_FORMAT = "HH:mm:ss:SSS";

	public static void log(String message) {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
		System.out.println(sdf.format(new Date()) + " - " + Thread.currentThread().getName() + " - " + message);
	}
}
