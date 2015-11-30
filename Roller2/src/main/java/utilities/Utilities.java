package utilities;

import java.io.File;

import configuration.RollerConfiguration;

public class Utilities {
	
	public static String buildRollerFileName(File root, String prefix, String extension) {
		String result;
		String fileName;
				
		fileName = String.format("%s-%s-%s-%s.%s", 
			prefix, 
			RollerConfiguration.getLearnerClazz().getSimpleName(),
			RollerConfiguration.getGainFunction().getClass().getSimpleName(),
			RollerConfiguration.getScoringFunction().getClass().getSimpleName(),
			extension);		
		result = new File(root, fileName).getPath();
			
		return result;		
	}

}
