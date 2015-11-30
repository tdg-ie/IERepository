package system;

import java.util.HashMap;
import java.util.Map;

public class Watch {
	
	protected static class Timing {
		long beginMillis;
		long endMillis;		
	}
	
	protected static Map<Thread, Timing> timings;
	
	static {
		timings = new HashMap<Thread, Timing>();
	}
	
	protected static synchronized Timing getCurrentThreadTiming() {
		Timing result;
		Thread currentThread;
		
		currentThread = Thread.currentThread();
		result = timings.get(currentThread);
		if (result == null) {
			result  = new Timing();
			timings.put(currentThread, result);
		}

		return result;
	}

	
	public static void start() {
		Timing timing;
		
		timing = getCurrentThreadTiming();
		timing.beginMillis = System.currentTimeMillis();
		timing.endMillis = 0;			
	}

	
	public static void stop() {
		Timing timing;
		
		timing = getCurrentThreadTiming();		
		timing.endMillis = System.currentTimeMillis();
	}
	
	public static long getTiming() {
		long result;
		Timing timing;
		
		timing = getCurrentThreadTiming();
		result = timing.endMillis - timing.beginMillis;
		
		return result;
	}

	

}
