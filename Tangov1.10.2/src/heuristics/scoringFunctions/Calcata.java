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

public class Calcata extends Heuristic {
	
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
		 
		double G = score(node) - currentScore;
		
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
		double tot = P + N;
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tpx = origcm.getTruePositives();
		double fpx = origcm.getFalsePositives();
		double tnx = origcm.getTrueNegatives();
		double fnx = origcm.getFalseNegatives();
		
		if ((fpx + tnx) != 0 && tpx != 0) { // if fp == 0 means that the negative coverage has not been computed yet
			double numerator = 2 * (tpx + tnx) * (tot - fpx -fnx);
			double denominator = tot * (tot - fpx - fnx + tpx + tnx);
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
			double tot = P + N;
			
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix origcm = cml.getOrigcm();
			double tpx = origcm.getTruePositives();
			double fnx = origcm.getFalseNegatives();
			double tnx = origcm.getTrueNegatives();
			
			if (tpx != 0) {
				result = (Math.pow(tot, 2)
                        * (2 * currentBaseInfo + 2 * minUsefulGain - 1)
                        - (currentBaseInfo * fnx - (currentBaseInfo - 2) * tnx - (currentBaseInfo - 2) * tpx + (fnx - tnx - tpx)
                                      * minUsefulGain) * tot + 2 * fnx * tnx + 2 * fnx * tpx)
                        / (tot * (currentBaseInfo + minUsefulGain) - 2 * tnx - 2 * tpx);

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
		double tot = P * N;
		
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tp = origcm.getTruePositives();
		double fp = origcm.getFalsePositives();
		if (tp != 0)
			result = 2 * tot * (tp + fp) / tot * (tot + tp + fp) - icCurrentNode.getScore();
		
		return result;
	}
}