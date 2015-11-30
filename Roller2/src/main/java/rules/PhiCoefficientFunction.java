package rules;


public class PhiCoefficientFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfusionMatrix confusionMatrix;

		confusionMatrix = rule.getConfusionMatrix();

		result = (confusionMatrix.getTruePositives() * confusionMatrix.getTrueNegatives() - 
					confusionMatrix.getFalsePositives() * confusionMatrix.getFalseNegatives() + 1) / 
				 Math.sqrt((confusionMatrix.getTruePositives() + confusionMatrix.getFalseNegatives()) * 
				    (confusionMatrix.getFalsePositives() + confusionMatrix.getTrueNegatives()) * 
				    (confusionMatrix.getTruePositives() + confusionMatrix.getFalsePositives()) * 
				    (confusionMatrix.getFalseNegatives() + confusionMatrix.getTrueNegatives()) + 1);
						
		return result;
	}

}
