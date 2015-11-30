package persistence;

import java.io.File;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

public class InstancesPersistence {
	
	public static Instances load(File folder, String fileName) {
		Instances result;
		File file;
		CSVLoader loader;
		
		try {
			file = new File(folder, fileName);			
			loader = new CSVLoader();
			loader.setFile(file);
			result = loader.getDataSet();
			result.setRelationName(fileName);
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;
	}

	public static void save(Instances instances, File file) {
		CSVSaver saver;
		
		try {
			saver = new CSVSaver();
			saver.setFile(file);
			saver.setInstances(instances);
			saver.writeBatch();
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
	}
	
}
