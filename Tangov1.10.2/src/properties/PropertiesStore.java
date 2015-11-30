package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import utilities.FoilException;

public class PropertiesStore {
	private static String CONFIGURATION_FILE = "";
	private static Properties prop = new Properties();
		 
	public static void refreshFile(String filePath) {
		InputStream is = null;
		try {
			CONFIGURATION_FILE = filePath;
			File configFile = new File(CONFIGURATION_FILE);
		         
		    is=new FileInputStream(configFile.getPath());
		    prop.load(is);
		    is.close();
		 
		} catch(IOException ioe) {}
	}
	
	public static String getProperty(String name)
			throws FoilException {
		
		String valor = prop.getProperty(name);
		
		if (valor == null)
			throw new FoilException("Missing configuration parameter: '" + name + "'");
		 
		return valor;
	}
}
