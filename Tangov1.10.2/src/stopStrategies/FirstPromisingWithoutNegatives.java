package stopStrategies;

import branch_and_bound.data.InformationContext;

public class FirstPromisingWithoutNegatives implements IStopStrategies {

	// the fist literal that does not cover any negative bindings and its information gain 
	// exceeds a specific threshold (0.5)
	@Override
	public boolean stop(InformationContext icCurrentNode, double maxGain) {
		boolean result = false;
		double threshold = 0.5;
		if ((icCurrentNode.getOrigCoverage().getNegativeBindings().size() == 0) && 
				(icCurrentNode.getGain() >= (maxGain * threshold))) {
			result = true;
		}	
		return result;
	}
}
