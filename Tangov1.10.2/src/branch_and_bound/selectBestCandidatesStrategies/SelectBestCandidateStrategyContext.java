package branch_and_bound.selectBestCandidatesStrategies;

public class SelectBestCandidateStrategyContext
{
	/**
	 */
	private BestCandidatesComparator strategy;

	public SelectBestCandidateStrategyContext(BestCandidateSelectionType strategyTypeIn)
	{
		
		if (strategyTypeIn == BestCandidateSelectionType.MAXIMUMGAIN)
		{
			this.strategy = new MaximumGainComparator();
		}
		else if (strategyTypeIn == BestCandidateSelectionType.FOILHEURISTIC)
		{
			this.strategy = new FoilComparator();
		}
		else { // by default the strategy is to select the one which reach the maximum gain
			this.strategy = new MaximumGainComparator();
		}
	}

	public BestCandidatesComparator getStrategy() {
		return strategy;
	}
}