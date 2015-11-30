package rules;


public class SatisfactionFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();

		result = (confusionMatrix.getTruePositives() / (confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalsePositives()) - (confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalseNegatives()) / confusionMatrix.getNumberOfSamples()) / 
				 (1 - ((confusionMatrix.getTruePositives() + confusionMatrix.getFalseNegatives()) / 
						 confusionMatrix.getNumberOfSamples()));
						
		return result;
	}

}
