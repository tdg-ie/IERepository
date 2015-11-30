package stopStrategies;

import branch_and_bound.data.InformationContext;

public interface IStopStrategies {
	public boolean stop(InformationContext icCurrentNode, double maxGain);
}
