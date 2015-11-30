package utilities;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * This class can be used to measure CPU times for the current thread or a given set of threads.
 * <ul>
 *  <li> User Time: The time the user's code has consumed to execute. 
 *  <li> System Time: The time spent running operational system's code on behalf of your application.
 *  <li> CPU Time: The total time consumed to run the user's code. This is computed by summing User time + System time.
 *</ul>
 * @author Rafael Z. Frantz
 */
public class ThreadMonitor {
	
	/** 
	 *  Gets the CPU time for a set of threads.
	 *  @param ids - the ids of threads to which the CPU time must be taken.
	 *  @return long the total CPU time in nanoseconds.
	 */
	public static long getCpuTime( long[] ids ) {
	    ThreadMXBean xbean = ManagementFactory.getThreadMXBean();
	    if ( !xbean.isThreadCpuTimeSupported() )
	        return 0L;
	    long time = 0L;
	    for ( int i = 0; i < ids.length; i++ ) {
	        long t = xbean.getThreadCpuTime( ids[i] );
	        if ( t != -1 )	        	
	            time += t;
	    }
	    return time;
	}
	
	/** 
	 *  Gets the CPU time for a current thread.
	 *  @return long the CPU time in nanoseconds.
	 */
	public static long getCpuTime() {
	    ThreadMXBean xbean = ManagementFactory.getThreadMXBean();
	    return xbean.isCurrentThreadCpuTimeSupported()?xbean.getCurrentThreadCpuTime():0L;
	}
	
	 
	/** 
	 *  Gets the User time for a set of threads.
	 *  @param ids - the ids of threads to which the User time must be taken.
	 *  @return long the total User time in nanoseconds.
	 */
	public static long getUserTime( long[] ids ) {
	    ThreadMXBean xbean = ManagementFactory.getThreadMXBean();
	    if ( !xbean.isThreadCpuTimeSupported() )
	        return 0L;
	    long time = 0L;
	    for ( int i = 0; i < ids.length; i++ ) {
	        long t = xbean.getThreadUserTime( ids[i] );
	        if ( t != -1 )
	            time += t;
	    }
	    return time;
	}
	
	  
	/** 
	 *  Gets the System time for a set of threads.
	 *  @param ids - the ids of threads to which the System time must be taken.
	 *  @return long the total System time in nanoseconds.
	 */
	public static long getSystemTime( long[] ids ) {
	    ThreadMXBean xbean = ManagementFactory.getThreadMXBean( );
	    if ( !xbean.isThreadCpuTimeSupported() )
	        return 0L;
	    long time = 0L;
	    for ( int i = 0; i < ids.length; i++ ) {
	        long tc = xbean.getThreadCpuTime( ids[i] );
	        long tu = xbean.getThreadUserTime( ids[i] );
	        if ( tc != -1 && tu != -1 )
	            time += (tc - tu);
	    }
	    return time;
	}	
	 
	
	/** 
	 *  Gets the User time for a current thread.
	 *  @return long the User time in nanoseconds.
	 */	
	public static long getUserTime() {
	    ThreadMXBean xbean = ManagementFactory.getThreadMXBean();
	    return xbean.isCurrentThreadCpuTimeSupported()?xbean.getCurrentThreadUserTime():0L;
	}
	

	/** 
	 *  Gets the System time for a current thread.
	 *  @return long the System time in nanoseconds.
	 */
	public static long getSystemTime() {
	    ThreadMXBean xbean = ManagementFactory.getThreadMXBean( );
	    return xbean.isCurrentThreadCpuTimeSupported()?(xbean.getCurrentThreadCpuTime() - xbean.getCurrentThreadUserTime()):0L;
	}
}
