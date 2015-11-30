package utilities;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryMonitor {


	public static MemoryUsage getHeapMemoryUsage() {
		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		return bean.getHeapMemoryUsage();
	}

	public static MemoryUsage getNonHeapMemoryUsage() {
		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		return bean.getNonHeapMemoryUsage();
	} 
	
	public static long getMemoryUsage() {
		Runtime r = Runtime.getRuntime();
		return r.totalMemory() - r.freeMemory();
	}
		
}
