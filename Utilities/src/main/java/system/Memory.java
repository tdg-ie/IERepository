package system;


public class Memory {
	
	public static void collectGarbage() {
		try {
			Runtime.getRuntime().gc();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			;
		}
	}

	public static long getFreeMemory() {
		long result;
		
		result = Runtime.getRuntime().freeMemory();
		
		return result;
	}

}
