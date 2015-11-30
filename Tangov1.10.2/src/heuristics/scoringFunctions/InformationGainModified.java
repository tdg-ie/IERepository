package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;
import utilities.Utilities;

public class InformationGainModified extends Heuristic {
	
	@Override

	public double evaluate(double currentScore, Node<Literal,InformationContext> node, int NewVariables) {
	/**
	 * Compute aggregate gain from a test on relation R, tuple T.
	 * The Basic gain is the number of positive tuples * information
	 * gained regarding each; but there is a minor adjustment: - a literal that
	 * has some positive tuples and no gain but introduces one o more new
	 * variables, is given a slight gain
	 * 
	 */
		double result = -1;
		double TG;
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix nowcm = cml.getNowcm();
		int TNow = nowcm.getTruePositives();

		double G = currentScore - score(node);
		TG = TNow * G;
		
		if ( (G < 1E-3) && (TNow > 0))
			result = 0.0009 + (NewVariables * 0.0001);  /* very small notional gain */
		else
			result = TG;
		
		return result;
	}
	
	public double score(Node<Literal,InformationContext> node) {
	/**
	 * The ratio P/T is tweaked slightly to (P+1) /(T+1) so that, if
	 * two sets of tuples have the same proportion of pos tuples, the smaller
	 * is preferred. The reasoning is that it is easier to filter out
	 * all neg tuples from a smaller set. If you don't like this idea
	 * and change it back to P/T, NegThresh must be changed also
	 */
		double result = -1;
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix cm = cml.getNowcm();
		
		int tp = cm.getTruePositives();
		int tn = cm.getTrueNegatives();
		int t = cm.getN();
		
		result = Utilities.log2(t+1) - Utilities.log2(tp+tn+1);
		return result;
	}

	public double threshold(Node<Literal,InformationContext> node, double currentBaseInfo, Thresholds thresholds) {
	/**
	 * Compute the maximum number N1 of neg tuples that would allow P1 pos tuples
	 * (P orig pos tuples) to five a gain >= threshold. The underlying relation is
	 * P * (Current.BaseInfo + log(P1/(P1+N1)) >= MinUsefulGain
	 * where N1 is adjusted by the sampling factor
	 * 
	 * NOTE: This is the inverse of the gain calculation in Worth. If one is
	 * changed, the other must be modified accordingly
	 */
			double result;
			double ln2 = Math.log(2);
			double minUsefulGain = thresholds.getMinUsefulGain();
			
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix nowcm = cml.getNowcm();
			int tpnow = nowcm.getTruePositives();
			int tnnow = nowcm.getTrueNegatives();
			int fnnow = nowcm.getFalseNegatives();
			double totneg = thresholds.getCurrentNNeg();
			double x = Math.exp(ln2 * (currentBaseInfo - minUsefulGain/tpnow));
			
			result = (tpnow + tnnow) <= 0 ? 0.0 : ((((tpnow + totneg + 1) * x) - (tpnow + totneg + fnnow + 1)) / x);
			
			return result;
		}

	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result;
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		double tpnow = cml.getNowcm().getTruePositives();	    
	    result = tpnow * icCurrentNode.getScore(); // bestScore = 0
		return result;
	}

	@Override
	public int getMaxNumberOfFalsePositivesforL(
			ConfusionMatrixList confusionMatrixListL) {
		ConfusionMatrix cml = confusionMatrixListL.getNowcm();
		return cml.getFalsePositives();
	}

	@Override
	public int getMaxNumberOfFalsePositivesforNL(
			ConfusionMatrixList confusionMatrixListNL) {
		ConfusionMatrix cmnl = confusionMatrixListNL.getNowcm();
		return cmnl.getFalsePositives();
	}
}