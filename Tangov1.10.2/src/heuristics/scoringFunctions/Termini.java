package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import dataCoverage.Coverage;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

/**
 * Range of this heuristic [0,1]
 * @author patriciajimenezaguirre
 *
 */

public class Termini extends Heuristic {
	
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
		double result;
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		int tpx = origcm.getTruePositives();
		 
		double G = score(node);
		
		if ( (G < 1E-3) && (tpx > 0))
			result = 0.0009 + (NewVariables * 0.0001);  /* very small notional gain */
		else
			result = G;
		
		return result;
	}
	
	public double score(Node<Literal,InformationContext> node) {
		double result = 0;
		
		InformationContext icRoot = node.getRoot().getInformationContext();
		Coverage origRootCoverage = icRoot.getOrigCoverage();
		double P = origRootCoverage.getPositiveBindings().size();
		double N = origRootCoverage.getNegativeBindings().size();
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tpx = origcm.getTruePositives();
		double fpx = origcm.getFalsePositives();
		double tnx = origcm.getTrueNegatives();
		double fp = origcm.getFalsePositives() + tnx;
		
		if (fp != 0 && tpx != 0) { // if fp == 0 means that the negative coverage has not been computed yet
			double numerator = (2 * tpx * (fp - fpx));
			double denominator = ((N * tpx) + P * (fp - fpx));
			result = numerator / denominator;
		}
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		return result;
	}

	public double threshold(Node<Literal,InformationContext> node, double currentBaseInfo, Thresholds thresholds) {
	
			double result = 0;
			double minUsefulGain = thresholds.getMinUsefulGain();

			InformationContext icRoot = node.getRoot().getInformationContext();
			Coverage origRootCoverage = icRoot.getOrigCoverage();
			double P = origRootCoverage.getPositiveBindings().size();
			double N = origRootCoverage.getNegativeBindings().size();

			InformationContext icParent;
			if (node.getParent() == null)
				 icParent = icRoot;
			else
				icParent = node.getParent().getInformationContext();
			
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix origcm = cml.getOrigcm();
			double tpx = origcm.getTruePositives();
			double fp = icParent.getOrigCoverage().getNegativeBindings().size();
			
			if (tpx != 0) {
				result = (P * fp * minUsefulGain + N * tpx * minUsefulGain - 2 * fp * tpx) / (P * minUsefulGain - 2 * tpx);
			}
		
			if (isInfinity(result)) {
				System.err.println("Error, division by 0");
				result = N;
			}
			return result;
		}

	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result = 0;
		
		InformationContext icRoot = currentNode.getRoot().getInformationContext();
		Coverage origRootCoverage = icRoot.getOrigCoverage();
		double P = origRootCoverage.getPositiveBindings().size();
		double N = origRootCoverage.getNegativeBindings().size();
		
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tp = origcm.getTruePositives();
		double fp = origcm.getFalsePositives();
		// fpx must be 0 to reach the best score
		if (tp != 0)
			result = 2 * fp * tp / (N * P * (fp / N + tp / P));
		
		return result;
	}
}