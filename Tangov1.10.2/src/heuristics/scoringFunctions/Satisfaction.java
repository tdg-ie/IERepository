package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class Satisfaction extends Heuristic {
	
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
		double tot = tpx + fpx + fnx + tnx;
			    
		if ((tpx + fpx != 0) && (tnx + fpx != 0)) {
			result = ((fnx + tpx) / tot - tpx / (fpx + tpx)) / ((fnx + tpx) / tot - 1);
			
			if (isInfinity(result)) {
				System.err.println("Error, division by 0");
				System.exit(0);
			}
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
		
		double result = 0;
		double minUsefulGain = thresholds.getMinUsefulGain();

		InformationContext icParent;
		if (node.getParent() == null)
			icParent = node.getRoot().getInformationContext();
		else
			icParent = node.getParent().getInformationContext();

		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix cmParent = icParent.getConfusionMatrixList().getOrigcm();
		ConfusionMatrix cm = cml.getOrigcm();

		double tpx = cm.getTruePositives();
		double fnx = cm.getFalseNegatives();
		double tot = cmParent.getN();

		if (tpx != 0) {
			double numerator = -((currentBaseInfo - 1)
					* fnx
					* Math.pow(tpx, 2)
					+ (currentBaseInfo - 1)
					* Math.pow(tpx, 3)
					- ((currentBaseInfo - 1) * Math.pow(tpx, 2) + tpx
							* minUsefulGain) * tot + (fnx * tpx + Math.pow(tpx,
					2)) * minUsefulGain);

			double denominator = ((currentBaseInfo - 1) * fnx * tpx
					+ (currentBaseInfo - 1) * Math.pow(tpx, 2)
					- (currentBaseInfo * tpx + minUsefulGain) * tot + (fnx + tpx)
					* minUsefulGain);
			
			result = numerator / denominator;
			if (isInfinity(result)) {
				System.err.println("Error, division by 0");
				System.exit(0);
			}
		}
		return result;
	}

	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result = -1;
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		double tp = cml.getOrigcm().getTruePositives();
		double bestScore = 1;
		result = -(icCurrentNode.getScore() - bestScore) * tp;
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		return result;
	}
}