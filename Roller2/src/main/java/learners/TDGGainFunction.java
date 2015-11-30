package learners;

import rules.Rule;
import rules.ScoringFunction;
import configuration.RollerConfiguration;

public class TDGGainFunction implements GainFunction {

	public double compute(Rule current, Rule expansion) {
		double result;
		ScoringFunction scorer;
		
		scorer = RollerConfiguration.getScoringFunction();
		result = scorer.compute(expansion);
		
		return result;
	}

}
