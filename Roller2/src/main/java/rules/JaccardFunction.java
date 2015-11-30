package rules;


public class JaccardFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();
		
		result = confusionMatrix.getTruePositives() / (confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalseNegatives() + confusionMatrix.getFalsePositives());
						
		return result;
	}

}
