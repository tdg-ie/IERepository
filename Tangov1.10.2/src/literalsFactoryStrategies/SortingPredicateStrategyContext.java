package literalsFactoryStrategies;

public class SortingPredicateStrategyContext
{
	/**
	 */
	private IPredicateSortStrategies heuristicStrategy;
	private SortingType strategyTypeIn;

	public SortingPredicateStrategyContext(SortingType strategyTypeIn)
	{
		this.strategyTypeIn = strategyTypeIn;
		
		if (strategyTypeIn == SortingType.PredefinedTargetSupportStrategy)
		{
			this.heuristicStrategy = new PredefinedTargetSupportStrategy();
		}
		else if (strategyTypeIn == SortingType.TakeOrderFromTextFileStrategy)
		{
			this.heuristicStrategy = new TakeOrderFromTextFileStrategy();
		}
		else if (strategyTypeIn == SortingType.RandomStrategy)
		{
			this.heuristicStrategy = new RandomStrategy();
		}
		else {
			this.heuristicStrategy = null;
		}
	}

	/**
	 * Getter of the property <tt>heuristicStrategy</tt>
	 * 
	 * @return Returns the heuristicStrategy.
	 */
	public IPredicateSortStrategies getSortingStrategy()
	{
		return heuristicStrategy;
	}

	/**
	 * Setter of the property <tt>heuristicStrategy</tt>
	 * 
	 * @param heuristicStrategy
	 *            The heuristicStrategy to set.
	 */
	public void setHeuristicStrategy(IPredicateSortStrategies heuristicStrategy)
	{
		this.heuristicStrategy = heuristicStrategy;
	}

	public SortingType getStrategyTypeIn() {
		return strategyTypeIn;
	}
}