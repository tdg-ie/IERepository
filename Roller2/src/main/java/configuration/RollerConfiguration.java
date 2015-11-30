package configuration;

import java.util.List;

import learners.GainFunction;
import rules.ScoringFunction;
import weka.classifiers.Classifier;
import datasets.DataSet;
import datasets.RelationSet;

public class RollerConfiguration {

	// Training set -----------------------------------------------------------
		
	private static DataSet trainingSet;
			
	public static DataSet getTrainingSet() {
		return trainingSet;
	}

	public static void setTrainingSet(DataSet trainingSet) {
		RollerConfiguration.trainingSet = trainingSet;
	}
	
	// Test set ---------------------------------------------------------------
		
	private static DataSet testSet;	
	
	public static DataSet getTestSet() {
		return testSet;
	}

	public static void setTestSet(DataSet testSet) {
		RollerConfiguration.testSet = testSet;
	}
	
	// Attributes -------------------------------------------------------------

	public static List<String> getAttributes() {
		return trainingSet.getAttributes(false);
	}

	// Relations --------------------------------------------------------------

	private static RelationSet relations;

	public static List<String> getRelationNames() {
		return relations.getAttributes(false);
	}
	
	public static RelationSet getRelations() {
		return relations;
	}

	public static void setRelations(RelationSet relations) {
		RollerConfiguration.relations = relations;
	}

	// Gain function ----------------------------------------------------------

	private static GainFunction gainFunction;
	
	public static GainFunction getGainFunction() {
		return gainFunction;
	}

	public static void setGainFunction(GainFunction gainFunction) {
		RollerConfiguration.gainFunction = gainFunction;
	}

	// Scoring function -------------------------------------------------------

	private static ScoringFunction scoringFunction;

	public static ScoringFunction getScoringFunction() {
		return scoringFunction;
	}

	public static void setScoringFunction(ScoringFunction scoringFunction) {
		RollerConfiguration.scoringFunction = scoringFunction;
	}

	// Learner clazz ----------------------------------------------------------

	private static Class<Classifier> learnerClazz;

	public static Class<Classifier> getLearnerClazz() {
		return learnerClazz;
	}

	public static void setLearnerClazz(Class<Classifier> learnerClazz) {
		RollerConfiguration.learnerClazz = learnerClazz;
	}
	
}
