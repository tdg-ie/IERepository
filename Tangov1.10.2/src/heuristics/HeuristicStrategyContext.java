package heuristics;

import heuristics.scoringFunctions.Calcata;
import heuristics.scoringFunctions.Collective_Strength;
import heuristics.scoringFunctions.Colonna;
import heuristics.scoringFunctions.ComplexDucal;
import heuristics.scoringFunctions.ComplexTrevi;
import heuristics.scoringFunctions.Confidence;
import heuristics.scoringFunctions.Confirmation;
import heuristics.scoringFunctions.Coverage;
import heuristics.scoringFunctions.Ducal;
import heuristics.scoringFunctions.FMeasure;
import heuristics.scoringFunctions.Fiori;
import heuristics.scoringFunctions.InformationGain;
import heuristics.scoringFunctions.InformationGainModified;
import heuristics.scoringFunctions.OriginalInformationGain;
import heuristics.scoringFunctions.Jaccard;
import heuristics.scoringFunctions.Kappa;
import heuristics.scoringFunctions.Laplace;
import heuristics.scoringFunctions.Leverage;
import heuristics.scoringFunctions.Lift;
import heuristics.scoringFunctions.Odds_Ratio;
import heuristics.scoringFunctions.Phi_Coefficient;
import heuristics.scoringFunctions.Popolo;
import heuristics.scoringFunctions.Satisfaction;
import heuristics.scoringFunctions.Support;
import heuristics.scoringFunctions.Termini;
import heuristics.scoringFunctions.Trevi;
import heuristics.scoringFunctions.WeightedInformationGain;
import heuristics.scoringFunctions.YulesQ;

public class HeuristicStrategyContext
{
	/**
	 */
	private Heuristic heuristicStrategy;
	private HeuristicType strategyTypeIn;

	public HeuristicStrategyContext(HeuristicType strategyTypeIn)
	{
		this.strategyTypeIn = strategyTypeIn;
		
		if (strategyTypeIn == HeuristicType.INFORMATIONGAIN)
		{
			this.heuristicStrategy = new InformationGain();
		}
		else if (strategyTypeIn == HeuristicType.COVERAGE)
		{
			this.heuristicStrategy = new Coverage();
		}
		else if (strategyTypeIn == HeuristicType.LAPLACE)
		{
			this.heuristicStrategy = new Laplace();
		}
		else if (strategyTypeIn == HeuristicType.LEVERAGE)
		{
			this.heuristicStrategy = new Leverage();
		}
		else if (strategyTypeIn == HeuristicType.PHI_COEFFICIENT)
		{
			this.heuristicStrategy = new Phi_Coefficient();
		}
		else if (strategyTypeIn == HeuristicType.SUPPORT)
		{
			this.heuristicStrategy = new Support();
		}
		else if (strategyTypeIn == HeuristicType.CONFIDENCE)
		{
			this.heuristicStrategy = new Confidence();
		}
		else if (strategyTypeIn == HeuristicType.SATISFACTION)
		{
			this.heuristicStrategy = new Satisfaction();
		}
		else if (strategyTypeIn == HeuristicType.CONFIRMATION)
		{
			this.heuristicStrategy = new Confirmation();
		}
		else if (strategyTypeIn == HeuristicType.FMEASURE)
		{
			this.heuristicStrategy = new FMeasure();
		}
		else if (strategyTypeIn == HeuristicType.KAPPA)
		{
			this.heuristicStrategy = new Kappa();
		}
		else if (strategyTypeIn == HeuristicType.ODDS_RATIO)
		{
			this.heuristicStrategy = new Odds_Ratio();
		}
		else if (strategyTypeIn == HeuristicType.YULESQ)
		{
			this.heuristicStrategy = new YulesQ();
		}
		else if (strategyTypeIn == HeuristicType.LIFT)
		{
			this.heuristicStrategy = new Lift();
		}
		else if (strategyTypeIn == HeuristicType.COLLECTIVE_STRENGTH)
		{
			this.heuristicStrategy = new Collective_Strength();
		}
		else if (strategyTypeIn == HeuristicType.JACCARD)
		{
			this.heuristicStrategy = new Jaccard();
		}
		else if (strategyTypeIn == HeuristicType.ORIGINALINFORMATIONGAIN)
		{
			this.heuristicStrategy = new OriginalInformationGain();
		}
		else if (strategyTypeIn == HeuristicType.INFORMATIONGAINMODIFIED)
		{
			this.heuristicStrategy = new InformationGainModified();
		}
		else if (strategyTypeIn == HeuristicType.WEIGHTEDINFORMATIONGAIN)
		{
			this.heuristicStrategy = new WeightedInformationGain();
		}
		else if (strategyTypeIn == HeuristicType.TERMINI)
		{
			this.heuristicStrategy = new Termini();
		}
		else if (strategyTypeIn == HeuristicType.POPOLO)
		{
			this.heuristicStrategy = new Popolo();
		}
		else if (strategyTypeIn == HeuristicType.FIORI)
		{
			this.heuristicStrategy = new Fiori();
		}
		else if (strategyTypeIn == HeuristicType.COLONNA)
		{
			this.heuristicStrategy = new Colonna();
		}
		else if (strategyTypeIn == HeuristicType.TREVI)
		{
			this.heuristicStrategy = new Trevi();
		}
		else if (strategyTypeIn == HeuristicType.DUCAL)
		{
			this.heuristicStrategy = new Ducal();
		}
		else if (strategyTypeIn == HeuristicType.COMPLEXTREVI)
		{
			this.heuristicStrategy = new ComplexTrevi();
		}
		else if (strategyTypeIn == HeuristicType.COMPLEXDUCAL)
		{
			this.heuristicStrategy = new ComplexDucal();
		}
		else if (strategyTypeIn == HeuristicType.CALCATA)
		{
			this.heuristicStrategy = new Calcata();
		}
		
		//update when more heuristics are defined
//		else if {
//			this.heuristicStrategy = null;
//		}
	}

	/**
	 * Getter of the property <tt>heuristicStrategy</tt>
	 * 
	 * @return Returns the heuristicStrategy.
	 */
	public Heuristic getHeuristicStrategy()
	{
		return heuristicStrategy;
	}

	/**
	 * Setter of the property <tt>heuristicStrategy</tt>
	 * 
	 * @param heuristicStrategy
	 *            The heuristicStrategy to set.
	 */
	public void setHeuristicStrategy(Heuristic heuristicStrategy)
	{
		this.heuristicStrategy = heuristicStrategy;
	}

	public HeuristicType getStrategyTypeIn() {
		return strategyTypeIn;
	}
}