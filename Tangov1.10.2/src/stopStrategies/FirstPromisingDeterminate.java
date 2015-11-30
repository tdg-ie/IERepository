package stopStrategies;

import branch_and_bound.data.InformationContext;

public class FirstPromisingDeterminate implements IStopStrategies {

	@Override
	public boolean stop(InformationContext icCurrentNode, double maxGain) {
		boolean result = false;
		double threshold = 0.5;
		if (icCurrentNode.isDeterminate() && 
				(icCurrentNode.getGain() >= (maxGain * threshold))) {
			result = true;
		}
		return result;
	}
}
