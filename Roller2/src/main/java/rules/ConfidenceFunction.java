package rules;


public class ConfidenceFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();

		result = confusionMatrix.getTruePositives() / (confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalsePositives());
		
		return result;
	}

}
