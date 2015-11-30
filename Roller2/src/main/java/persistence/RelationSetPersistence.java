package persistence;

import java.io.File;

import weka.core.Instances;
import datasets.RelationSet;

public class RelationSetPersistence {

	public static RelationSet load(File folder, String fileName) {
		RelationSet result;
		Instances instances;
		
		try {
			instances = InstancesPersistence.load(folder, fileName);

			result = new RelationSet();			
			result.setInstances(instances);
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}

		return result;
	}

}

