package persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;
import datasets.ContextTuple;
import datasets.DataSet;

public class DataSetPersistence {

	public static DataSet load(File folder, String fileName) {
		DataSet result;
		Instances instances;
		List<ContextTuple> contexts;
		
		try {
			instances = InstancesPersistence.load(folder, fileName);			

			contexts = new ArrayList<ContextTuple>();
			contexts.add(ContextTuple.defaultContext());
			
			result = new DataSet();			
			result.setInstances(instances);
			result.setContexts(contexts);
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}

		return result;
	}

}

