package helpers;


import weka.classifiers.Classifier;
import weka.core.Instances;
import configuration.RollerConfiguration;

public class ClassifierHelper {
	
	public static Classifier learnClassifier(Instances instances) {
		Classifier result;
		Class<?> learnerClazz;
		
		try {
			learnerClazz = RollerConfiguration.getLearnerClazz();
			result = (Classifier) learnerClazz.newInstance();
			result.buildClassifier(instances);
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;
	}

}
