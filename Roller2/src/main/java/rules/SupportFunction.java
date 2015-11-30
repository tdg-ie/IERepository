package rules;


public class SupportFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();

		result = (confusionMatrix.getTruePositives() + confusionMatrix.getFalseNegatives()) / 
					confusionMatrix.getNumberOfSamples();
		
		return result;
	}

}
