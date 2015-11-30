package stopStrategies;

public class StopStrategyContext
{
	/**
	 */
	private IStopStrategies stopStrategy;

	public StopStrategyContext(StopCriteriaType strategyTypeIn)
	{
		if (strategyTypeIn == StopCriteriaType.DEFAULTSTOPCRITERIA)
			this.stopStrategy = new DefaultStopCriteria();
		else if (strategyTypeIn == StopCriteriaType.FIRSTPROMISINGDETERMINATE)
			this.stopStrategy = new FirstPromisingDeterminate();
		else if (strategyTypeIn == StopCriteriaType.FIRSTPROMISINGWITHOUTNEGATIVES)
			this.stopStrategy = new FirstPromisingWithoutNegatives();
		else if (strategyTypeIn == StopCriteriaType.THEMOSTPROMISINGLITERAL)
			this.stopStrategy = new TheMostPromisingLiteral();
		else if (strategyTypeIn == StopCriteriaType.FIRSTWITHPOSITIVEGAIN)
			this.stopStrategy = new FirstWithPositiveGain();
		//default heuristic is information gain
		else {
			this.stopStrategy = new DefaultStopCriteria();
		}
	}

	/**
	 * Getter of the property <tt>stopStrategy</tt>
	 * 
	 * @return Returns the stopStrategy
	 */

	public IStopStrategies getStopStrategy() {
		return stopStrategy;
	}

	/**
	 * Setter of the property <tt>stopStrategy</tt>
	 * 
	 * @param stopStrategy
	 *            The stopStrategy to set.
	 */
	public void setStopStrategy(IStopStrategies stopStrategy) {
		this.stopStrategy = stopStrategy;
	}
}