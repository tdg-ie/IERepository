package rules;

import helpers.ClassifierHelper;
import helpers.InstancesHelper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import utilities.Statistics;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import configuration.Constants;
import configuration.RollerConfiguration;
import datasets.DataSet;

public class Rule {

	private Classifier classifier;	
	private DataSet trainingSet;
	private DataSet testSet;
//	private double score;
	private Evaluation evaluation;
	private ConfusionMatrix confusionMatrix;
	private DataSet misclassified;
	private Statistics statistics;
		
	public Classifier getClassifier() {
		return classifier;
	}

	public DataSet getTrainingDataset() {
		return trainingSet;
	}

	public void setTrainingDataset(DataSet dataSet) {
		this.trainingSet = dataSet;
	}
	
	public DataSet getTestingDataset() {
		return testSet;
	}
	
	public void setTestingDataset(DataSet testingData) {
		this.testSet = testingData;		
	}
	
	public Evaluation getEvaluation() {
		return evaluation;
	}
	
//	public double getScore() {
//		return score;
//	}
	
	public ConfusionMatrix getConfusionMatrix() {
		return confusionMatrix;
	}

	public DataSet getMissclassified() {
		return misclassified;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}
	
	public void learn() {
		Filter trainingFilter;
		Instances trainingInstances;
		Instances misclassifiedInstances;	

		try {
			System.out.printf("LEARNING A CLASSIFIER FOR DATASET %s%n%n", trainingSet.getInstances().relationName());
									
			trainingFilter = trainingSet.getFilter();
			
			trainingInstances = trainingSet.getInstances();
			trainingInstances = Filter.useFilter(trainingInstances, trainingFilter);
			InstancesHelper.balance(trainingInstances);
			classifier = ClassifierHelper.learnClassifier(trainingInstances);
			misclassifiedInstances = InstancesHelper.computeMissclassified(classifier, trainingInstances);
			
			System.out.printf("UNBALANCED TRAINING = %d attributes x %d instances (weight = %f)%n", trainingInstances.numAttributes(), trainingInstances.numInstances(), InstancesHelper.computeWeight(trainingInstances));
			System.out.printf("  BALANCED TRAINING = %d attributes x %d instances (weight = %f)%n", trainingInstances.numAttributes(), trainingInstances.numInstances(), InstancesHelper.computeWeight(trainingInstances));
			System.out.printf("      MISCLASSIFIED = %d attributes x %d instances (weight = %f)%n", misclassifiedInstances.numAttributes(), misclassifiedInstances.numInstances(), InstancesHelper.computeWeight(misclassifiedInstances));
			
			evaluation = new Evaluation(trainingInstances);
			evaluation.evaluateModel(classifier,  trainingInstances);
			confusionMatrix = computeConfusionMatrix();
			
			System.out.printf("%s%n", this);
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}			
	}

	private ConfusionMatrix computeConfusionMatrix() {
		ConfusionMatrix result;
		int tp, fp, tn, fn;
		
		tp = fp = tn = fn = 0;
		for (int i = 0; i < trainingSet.numClazzes(); i++) {
			tp += evaluation.numTruePositives(i);
			fp += evaluation.numFalsePositives(i);
			tn += evaluation.numTrueNegatives(i);
			fn += evaluation.numFalseNegatives(i);
		}

		result = new ConfusionMatrix();
		result.setTruePositives(tp);
		result.setFalsePositives(fp);
		result.setTrueNegatives(tn);
		result.setFalseNegatives(fn);
		
		return result;
	}

	public boolean isConsistent() {
		boolean result;
		
		result = (evaluation.pctIncorrect() <= Constants.MINIMIUM_CONSISTENCY);
		
		return result;
	}
	
	public String toString() {
		String result;
		ByteArrayOutputStream stream;
		PrintStream out;
		
		try {
			stream = new ByteArrayOutputStream();
			out = new PrintStream(stream);

			out.printf("=== RULE ===%n%n");
			out.printf("Technique: %s%n", classifier.getClass().getName());
			out.printf("Context:  %s%n", trainingSet.getContexts());
			out.printf("Score:     %f%n%n", RollerConfiguration.getScoringFunction().compute(this));
			
			out.printf("%s%n", evaluation.toSummaryString(true));			
			out.printf("%s%n", evaluation.toClassDetailsString());
			out.printf("%s%n", evaluation.toMatrixString());
			
			// out.printf("=== Cumulative Margin Distribution ===%n%n");
			// out.printf("%s%n%n", evaluation.toCumulativeMarginDistributionString());
			
			out.printf("=== Classifier ===%n%n");
			out.printf("%s%n", classifier);
			
			result = stream.toString("ISO-8859-1");
		} catch (Throwable oops) {
			result = null;
			throw new RuntimeException(oops);
		}
		
		return result;	
	}
	
}
