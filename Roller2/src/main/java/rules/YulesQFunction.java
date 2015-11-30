package rules;


public class YulesQFunction implements ScoringFunction {

	public double compute(Rule rule) {
		double result;
		OddsRatioFunction oddsRatio;

		oddsRatio = new OddsRatioFunction();
		
		result = (oddsRatio.compute(rule) - 1) / (oddsRatio.compute(rule) + 1);
		
		return result;
	}

}
