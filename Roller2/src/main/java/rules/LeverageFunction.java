package rules;


public class LeverageFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();
		
		result = (confusionMatrix.getTruePositives() * confusionMatrix.getTrueNegatives() - 
					confusionMatrix.getFalsePositives() * confusionMatrix.getFalseNegatives()) / 
				 Math.pow(confusionMatrix.getNumberOfSamples(), 2);
						
		return result;
	}

}
