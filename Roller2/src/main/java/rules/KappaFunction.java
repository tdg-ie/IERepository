package rules;


public class KappaFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();

		result = ((confusionMatrix.getTruePositives() + confusionMatrix.getTrueNegatives()) / 
					confusionMatrix.getNumberOfSamples() - ((confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalseNegatives()) / confusionMatrix.getNumberOfSamples() * 
					(confusionMatrix.getTruePositives() + confusionMatrix.getFalsePositives()) / 
					confusionMatrix.getNumberOfSamples() + (confusionMatrix.getFalsePositives() + 
					confusionMatrix.getTrueNegatives()) / confusionMatrix.getNumberOfSamples() * 
					(confusionMatrix.getFalseNegatives() + confusionMatrix.getTrueNegatives()) / 
					confusionMatrix.getNumberOfSamples())) / (1 - ((confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalseNegatives()) / confusionMatrix.getNumberOfSamples() * 
					(confusionMatrix.getTruePositives() + confusionMatrix.getFalsePositives()) / 
					confusionMatrix.getNumberOfSamples() + (confusionMatrix.getFalsePositives() + 
					confusionMatrix.getTrueNegatives()) / confusionMatrix.getNumberOfSamples() * 
					(confusionMatrix.getFalseNegatives() + confusionMatrix.getTrueNegatives()) / 
					confusionMatrix.getNumberOfSamples()));
						
		return result;
	}

}
