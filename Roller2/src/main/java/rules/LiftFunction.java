package rules;


public class LiftFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		ConfidenceFunction confidence;
		SupportFunction support;
		
		confidence = new ConfidenceFunction();
		support = new SupportFunction();

		result = confidence.compute(rule) / support.compute(rule);
		
		return result;
	}

}

