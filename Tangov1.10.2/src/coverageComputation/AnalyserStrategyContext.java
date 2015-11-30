package coverageComputation;

import heuristics.HeuristicStrategyContext;

public class AnalyserStrategyContext
{
	/**
	 */
	private Analyser analyser;
	private AnalyserType analyserType;

	public AnalyserStrategyContext(AnalyserType analyserType, HeuristicStrategyContext heuristicStrategy)
	{
		this.analyserType = analyserType;
		
		if (analyserType == AnalyserType.SIMPLE_ANALYSER)
			this.analyser = new SimpleAnalyser(heuristicStrategy);
		else if (analyserType == AnalyserType.ALPHABETA_ANALYSER)
			this.analyser = new AlphaBetaAnalyser(heuristicStrategy);
		else if (analyserType == AnalyserType.MDL_ANALYSER) {
			this.analyser = new SimpleAnalyser(heuristicStrategy);
			this.analyser = new AnalyserWithMDL(analyser);
		}
		else if (analyserType == AnalyserType.ALPHABETA_MDL_ANALYSER) {
			this.analyser = new AlphaBetaAnalyser(heuristicStrategy);
			this.analyser = new AnalyserWithMDL(analyser);
		}
	}

	public Analyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(Analyser analyser) {
		this.analyser = analyser;
	}

	public AnalyserType getAnalyserType() {
		return analyserType;
	}

	public void setAnalyserType(AnalyserType analyserType) {
		this.analyserType = analyserType;
	}
}