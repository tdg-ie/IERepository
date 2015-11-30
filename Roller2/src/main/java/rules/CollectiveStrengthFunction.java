package rules;


public class CollectiveStrengthFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();
		
		result = ((confusionMatrix.getTruePositives() + confusionMatrix.getTrueNegatives()) / 
					((confusionMatrix.getTruePositives() + confusionMatrix.getFalseNegatives()) * 
					(confusionMatrix.getTruePositives() + confusionMatrix.getFalsePositives()) + 
					(confusionMatrix.getFalsePositives() + confusionMatrix.getTrueNegatives()) * 
					(confusionMatrix.getTrueNegatives() + confusionMatrix.getFalseNegatives()))) * 
					((confusionMatrix.getNumberOfSamples() - (confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalseNegatives()) * (confusionMatrix.getTruePositives() + 
					confusionMatrix.getFalsePositives()) - (confusionMatrix.getFalsePositives() + 
					confusionMatrix.getTrueNegatives()) * (confusionMatrix.getFalseNegatives() + 
					confusionMatrix.getTrueNegatives())) / (confusionMatrix.getNumberOfSamples() - 
					confusionMatrix.getTruePositives() - confusionMatrix.getTrueNegatives()));

		return result;
	}

}
