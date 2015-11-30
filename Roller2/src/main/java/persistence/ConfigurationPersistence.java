package persistence;

import java.io.File;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;
import configuration.Constants;
import configuration.RollerConfiguration;
import datasets.ContextTuple;
import datasets.DataSet;
import datasets.RelationSet;

public class ConfigurationPersistence {

	public static void load(File folder) {
		Instances instances, trainingInstances, testInstances, relationInstances;
		RemoveWithValues filter;
		int falseIndex;
		DataSet trainingSet, testSet;
		RelationSet relationSet;

		try {
			instances = InstancesPersistence.load(folder, Constants.ATTRIBUTES_CSV);
			
			if ("false".equals(instances.firstInstance().attribute(0).value(0)))
				falseIndex = 0;
			else
				falseIndex = 1;
			
			filter = new RemoveWithValues();
			filter.setAttributeIndex("first");
			filter.setNominalIndicesArr(new int[]{falseIndex});
			filter.setInputFormat(instances);
						
			trainingInstances = Filter.useFilter(instances, filter);
			trainingInstances.deleteAttributeAt(0);
			trainingSet = new DataSet();
			trainingSet.setInstances(trainingInstances);
			trainingSet.setContexts(ContextTuple.defaultContexts());
						
			filter.setInvertSelection(true);
			testInstances = Filter.useFilter(instances, filter);
			testInstances.deleteAttributeAt(0);
			testSet = new DataSet();
			testSet.setInstances(testInstances);
			testSet.setContexts(ContextTuple.defaultContexts());
			
			relationInstances = InstancesPersistence.load(folder, Constants.RELATIONS_CSV);
			relationInstances.deleteAttributeAt(0);
			relationSet = new RelationSet();
			relationSet.setInstances(relationInstances);
						
			RollerConfiguration.setTrainingSet(trainingSet);
			RollerConfiguration.setTestSet(testSet);
			RollerConfiguration.setRelations(relationSet);
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
	}
	
}