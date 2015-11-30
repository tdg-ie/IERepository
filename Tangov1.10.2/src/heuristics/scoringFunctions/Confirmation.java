package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class Confirmation extends Heuristic {
	
	public double score(Node<Literal,InformationContext> node) {
	/**
	 * The ratio P/T is tweaked slightly to (P+1) /(T+1) so that, if
	 * two sets of tuples have the same proportion of pos tuples, the smaller
	 * is preferred. The reasoning is that it is easier to filter out
	 * all neg tuples from a smaller set. If you don't like this idea
	 * and change it back to P/T, NegThresh must be changed also
	 */
		double result = 0;
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix cm = cml.getOrigcm();
		
		double tpx = cm.getTruePositives();
		double fpx = cm.getFalsePositives();
		double fnx = cm.getFalseNegatives();
		double tnx = cm.getTrueNegatives();
		double t = tpx + fpx + fnx + tnx;
		
		if (tpx + fpx != 0)
			result = Math.pow(tpx * tnx - fpx * fnx, 2) / (Math.pow(t, 2) * (tpx + fpx) * (tnx + fpx));
		
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
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
			
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix cm = cml.getOrigcm();
			double tp = cm.getTruePositives();
			
			result = tp <= 0 ? 0.0 : Double.MAX_VALUE;
			
			return result;
		}
	
	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result = 0;
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		ConfusionMatrix cm = cml.getOrigcm();
		double tpnow = cm.getTruePositives();
		double tp = cm.getTruePositives();
		double tn = cm.getFalsePositives();	    	    
		
		if (tp + tn != 0) {
			double bestScore = Math.pow(tp * tn, 2) / (Math.pow(tp + tn, 2) * tp * tn);
			result = tpnow * (bestScore - icCurrentNode.getScore());
		}
		
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}
}