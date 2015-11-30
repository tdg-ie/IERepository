package stopStrategies;

import branch_and_bound.data.InformationContext;

public class DefaultStopCriteria implements IStopStrategies {

	@Override
	public boolean stop(InformationContext icCurrentNode, double maxGain) {
		return false;
	}	
}
