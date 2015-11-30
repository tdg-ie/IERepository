package rules;

import weka.classifiers.Evaluation;

public class CalcataFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		Evaluation evaluation;
		double a, b;
		
		evaluation = rule.getEvaluation();
		a = evaluation.pctCorrect();
		b = 100 - evaluation.pctIncorrect();
		result = 2 * (a * b) / (a + b) / 100;
						
		return result;
	}

}
