package learners;

import rules.Rule;

public interface GainFunction {

		public double compute(Rule current, Rule expansion);
		
}
