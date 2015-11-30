package rules;

import weka.classifiers.Evaluation;
import datasets.DataSet;

public class InformationContentFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		Evaluation evaluation;
		DataSet trainingSet;		
		double tp, tn;
		
		evaluation = rule.getEvaluation();
		trainingSet = rule.getTrainingDataset();
		
		tp = 0;
		tn = 0;
		
		for (int i = 0; i < trainingSet.numClazzes(); i++) {
			tp += evaluation.numTruePositives(i);
			tn += evaluation.numTrueNegatives(i);
		}
		
		result = -Math.log(tp / (tp + tn));
					
		return result;
	}


}
