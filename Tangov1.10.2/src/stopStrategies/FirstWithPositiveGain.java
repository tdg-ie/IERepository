package stopStrategies;

import branch_and_bound.data.InformationContext;

public class FirstWithPositiveGain implements IStopStrategies {

	// the fist literal with positive gain
	@Override
	public boolean stop(InformationContext icCurrentNode, double maxGain) {
		boolean result = false;
		if (icCurrentNode.getGain() > 0.0) {
			result = true;
		}	
		return result;
	}

}
