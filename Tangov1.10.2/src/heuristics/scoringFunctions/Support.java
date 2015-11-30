package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class Support extends Heuristic {
	
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
		double tn = cm.getTrueNegatives();
		double t = tp + fp + fn + tn;
	    
		result = tp == 0 ? 0 : tp / t;
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
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix cm = cml.getOrigcm();
			
			double tp = cm.getTruePositives();
	    	
			result = tp <= 0 ? 0.0 : Double.MAX_VALUE;
			
			return result;
		}
	
	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result = -1;
		double tpnow, tp;
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		ConfusionMatrix cm = cml.getOrigcm();
		tpnow = tp = cm.getTruePositives();
		double tn = cm.getFalsePositives();	
		double bestScore = tp / (tp + tn);
		result = tpnow * (bestScore - icCurrentNode.getScore());
		return result;
	}
}