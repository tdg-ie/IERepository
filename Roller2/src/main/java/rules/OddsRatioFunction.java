package rules;


public class OddsRatioFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();

		result = (confusionMatrix.getTruePositives() * confusionMatrix.getTrueNegatives() + 1) / 
					(confusionMatrix.getFalseNegatives() * confusionMatrix.getFalsePositives() + 1);
		
		return result;
	}

}
