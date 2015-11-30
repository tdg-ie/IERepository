package configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class GlobalConfiguration {

	// Datasets folder --------------------------------------------------------
	
	private static String datasetsFolder;
	
	public static String getDatasetsFolder() {
		return datasetsFolder;
	}

	public static void setDatasetsFolder(String datasetsFolder) {
		GlobalConfiguration.datasetsFolder = datasetsFolder;			
	}
	
	// Output file ------------------------------------------------------------
	
	private static PrintStream output;		
				
	static {
		output = System.out;		
	}
	
	public static PrintStream getOutput() {
		return output;
	}

	public static void setOutput(File file) {
		FileOutputStream fos;
		BufferedOutputStream bos;
		
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			GlobalConfiguration.output = new PrintStream(bos);
		} catch (FileNotFoundException oops) {
			throw new RuntimeException(oops);
		}
	}
	
}
