package rules;

import weka.classifiers.Evaluation;
import datasets.DataSet;

public class TribecaFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		Evaluation evaluation;
		DataSet trainingSet;		
		double tp, tn, fp, fn;
		
		evaluation = rule.getEvaluation();
		trainingSet = rule.getTrainingDataset();
		
		tp = 0;
		tn = 0;
		fp = 0;
		fn = 0;
		
		for (int i = 0; i < trainingSet.numClazzes(); i++) {
			tp += evaluation.numTruePositives(i);
			tn += evaluation.numTrueNegatives(i);
			fp += evaluation.numFalsePositives(i);
			fn += evaluation.numFalseNegatives(i);
		}
		
		result = -Math.log((tp + tn) / (tp + tn + fp + fn));
					
		return result;
	}

}
