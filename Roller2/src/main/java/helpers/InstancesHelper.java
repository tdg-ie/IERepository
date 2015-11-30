package helpers;

import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import configuration.Constants;

public class InstancesHelper {
	
	public static void addInstances(Instances target, Instances source) {
		Instance instance;
		
		for (int i = 0; i < source.numInstances(); i++) {
			instance = source.instance(i);
			target.add(instance);
		}
	}
	
	public static void balance(Instances instances) {
		Instance instance;
		int numNegatives, replica;
		String clazz;
		Map<String, Integer> clazzCount; // Class --> Counter
		Integer counter;

		clazzCount = new HashMap<String, Integer>();
		numNegatives = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);
			clazz = InstanceHelper.getAttribute(instance, Constants.NODE_CLAZZ);
			
			if (clazz.equals(Constants.NULL))
				numNegatives++;
			
			counter = clazzCount.get(clazz);
			if (counter == null)
				counter = 1;
			else
				counter++;
			clazzCount.put(clazz, counter);
		}

		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);			
			clazz = InstanceHelper.getAttribute(instance, Constants.NODE_CLAZZ);
			if (!clazz.equals(Constants.NULL)) {
				counter = clazzCount.get(clazz);
				replica = (numNegatives - counter) / counter + 1;
				instance.setWeight(replica);				
			}
		}
	}
	
	
	public static Instances computeMissclassified(Classifier classifier, Instances testInstances) {
		Instances result;
		Instance instance;
		double predictedClazz, actualClazz;
		
		try {
			result = new Instances(testInstances, 0, 0);
			
			for (int i = 0; i < testInstances.numInstances(); i++) {
				instance = testInstances.instance(i);
				predictedClazz = classifier.classifyInstance(instance);
				actualClazz = instance.classValue();
				if (predictedClazz != actualClazz)
					result.add(instance);
			}
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;
	}
	
	public static Instances computeWellclassified(Classifier classifier, Instances testInstances) {
		Instances result;
		Instance instance;
		double predictedClazz, actualClazz;
		
		try {
			result = new Instances(testInstances, 0, 0);
			for (int i = 0; i < testInstances.numInstances(); i++) {
				instance = testInstances.instance(i);
				predictedClazz = classifier.classifyInstance(instance);
				actualClazz = instance.classValue();
				if (predictedClazz == actualClazz)
					result.add(instance);
			}
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;
	}

	public static Instance filter(Filter filter, Instance instance) {
		Instance result;
		
		try {
			filter.input(instance);
			result = filter.output();
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;
	}
	
	public static Instances filter(Filter filter, Instances instances) {
		Instances result;
		
		try {
			result = Filter.useFilter(instances,  filter);
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;
	}

	public static double computeWeight(Instances instances) {
		int result;
		Instance instance;
		double weight;
		
		result = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);
			weight = instance.weight();
			result += weight;
		}
		
		return result;
	}
	
	

}
