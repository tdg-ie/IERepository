package persistence;

import java.io.File;

import rules.Rule;
import utilities.Utilities;
import weka.classifiers.Classifier;
import weka.core.SerializationHelper;
import configuration.Constants;

public class RulePersistence {
	
	public static void save(File site, Rule rule) {
		Classifier classifier;
		String fileName;
		
		try {
			classifier = rule.getClassifier();
			fileName = Utilities.buildRollerFileName(site, Constants.RULE_FILE_BASENAME, Constants.RULE_FILE_EXTENSION);			
			SerializationHelper.write(fileName, classifier);
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
	}

}

