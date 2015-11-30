package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class FMeasure extends Heuristic {
	
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
		ConfusionMatrix cm = cml.getOrigcm();
		
		double tp = cm.getTruePositives();
		double fp = cm.getFalsePositives();
		double fn = cm.getFalseNegatives();

		result = 2 * (tp / (2 * tp + fn + fp));
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
			double result = 0;
			double minUsefulGain = thresholds.getMinUsefulGain();
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix cm = cml.getOrigcm();
			
			double tpnow = cm.getTruePositives();
			double tp = cm.getTruePositives();
			double fn = cm.getFalseNegatives();
			double x = minUsefulGain/tpnow + currentBaseInfo;
	    	
			result = tp <= 0 ? 0.0 : ((2*tp / x) -2*tp -fn);
			
			return result;
		}

	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result = -1;
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		double tpnow = cml.getOrigcm().getTruePositives();
		double bestScore = 1; // 2 * (tp / (2 * tp)) == 1;
		result = tpnow * (bestScore - icCurrentNode.getScore());
		return result;
	}
}