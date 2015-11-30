package rules;


public class LaplaceFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();
		
		result = (confusionMatrix.getTruePositives() + 1) / 
					(confusionMatrix.getTruePositives() + confusionMatrix.getFalsePositives() + 2);
						
		return result;
	}

}
