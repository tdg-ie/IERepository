package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import dataCoverage.Coverage;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class Ducal extends Heuristic {
	
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
		
		InformationContext icParent;
		if (node.getParent() == null)
			 icParent = node.getRoot().getInformationContext();
		else
			icParent = node.getParent().getInformationContext();
			
		Coverage origParentCoverage = icParent.getOrigCoverage();
		double P = origParentCoverage.getPositiveBindings().size();
		double N = origParentCoverage.getNegativeBindings().size();
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tpx = origcm.getTruePositives();
		double fpx = origcm.getFalsePositives();
		double fp = origcm.getFalsePositives() + origcm.getTrueNegatives();
		
		double termini = (2 * tpx * (fp - fpx)) / ((N * tpx) + P * (fp - fpx)); 
		double G = termini * (score(node) - currentScore);
		
		if ( (G < 1E-3) && (tpx > 0))
			result = 0.0009 + (NewVariables * 0.0001);  /* very small notional gain */
		else
			result = G;
		
		return result;
	}
	
	public double score(Node<Literal,InformationContext> node) {
		double result = 0;
		
		InformationContext icParent;
		if (node.getParent() == null)
			 icParent = node.getRoot().getInformationContext();
		else
			icParent = node.getParent().getInformationContext();
			
		Coverage origParentCoverage = icParent.getOrigCoverage();
		double P = origParentCoverage.getPositiveBindings().size();
		double N = origParentCoverage.getNegativeBindings().size();
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tpx = origcm.getTruePositives();
		double fpx = origcm.getFalsePositives();
		 
		if (tpx != 0 && N != 0)
			result = (2 * tpx * (N - fpx)) / (N * tpx + P * (N - fpx));
		
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}

	public double threshold(Node<Literal,InformationContext> node, double currentBaseInfo, Thresholds thresholds) {
	
			// the beta threshold is not going to be computed because of its complexity, so it 
			// will return the maximum number of negative examples in the training set
			double result = 0;
			double x, y;
			double minUsefulGain = thresholds.getMinUsefulGain();
			double tpx, fp;
			
			InformationContext icParent;
			if (node.getParent() == null)
				 icParent = node.getRoot().getInformationContext();
			else
				icParent = node.getParent().getInformationContext();
				
			Coverage origParentCoverage = icParent.getOrigCoverage();
			double P = origParentCoverage.getPositiveBindings().size();
			double N = fp = origParentCoverage.getNegativeBindings().size();
			
			ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
			ConfusionMatrix origcm = cml.getOrigcm();
			tpx = origcm.getTruePositives();
			
			if (tpx != 0) {
				x = 1
				/ 2
				* (2 * N * (currentBaseInfo - 2) * Math.pow(tpx, 2) + (N * minUsefulGain + fp * minUsefulGain)
						* Math.pow(P, 2) - 4 * fp * Math.pow(tpx, 2) + 2
						* (currentBaseInfo * fp * tpx + (currentBaseInfo * tpx + tpx * minUsefulGain) * N) * P - Math
						.sqrt(16
								* N
								* (currentBaseInfo - 2)
								* fp
								* Math.pow(tpx, 4)
								+ 16
								* Math.pow(fp, 2)
								* Math.pow(tpx, 4)
								+ (Math.pow(N, 2) * Math.pow(minUsefulGain, 2) - 2 * N
										* fp * Math.pow(minUsefulGain, 2) + Math.pow(
										fp, 2) * Math.pow(minUsefulGain, 2))
								* Math.pow(P, 4)
								+ 4
								* (Math.pow(N, 2) * currentBaseInfo * tpx * minUsefulGain - 2 * N * currentBaseInfo
										* fp * tpx * minUsefulGain + currentBaseInfo
										* Math.pow(fp, 2) * tpx * minUsefulGain)
								* Math.pow(P, 3)
								+ 4
								* ((Math.pow(currentBaseInfo, 2) - 4 * currentBaseInfo + 4)
										* Math.pow(tpx, 4) + 4
										* Math.pow(tpx, 4) * minUsefulGain)
								* Math.pow(N, 2)
								+ 4
								* (Math.pow(currentBaseInfo, 2)
										* Math.pow(fp, 2)
										* Math.pow(tpx, 2)
										- 2
										* Math.pow(fp, 2)
										* Math.pow(tpx, 2)
										* minUsefulGain
										+ (Math.pow(currentBaseInfo, 2)
												* Math.pow(tpx, 2) + (currentBaseInfo - 2)
												* Math.pow(tpx, 2) * minUsefulGain)
										* Math.pow(N, 2) - (2
										* Math.pow(currentBaseInfo, 2) * fp
										* Math.pow(tpx, 2) + (currentBaseInfo - 4) * fp
										* Math.pow(tpx, 2) * minUsefulGain)
										* N)
								* Math.pow(P, 2)
								+ 8
								* ((Math.pow(currentBaseInfo, 2) - 2 * currentBaseInfo)
										* Math.pow(N, 2) * Math.pow(tpx, 3)
										- (Math.pow(currentBaseInfo, 2) - 4 * currentBaseInfo) * N * fp
										* Math.pow(tpx, 3) - 2 * currentBaseInfo
										* Math.pow(fp, 2)
										* Math.pow(tpx, 3)) * P))
				/ (2 * P * currentBaseInfo * tpx + Math.pow(P, 2) * minUsefulGain - 4 * Math.pow(tpx,
						2));
		y = 1
				/ 2
				* (2 * N * (currentBaseInfo - 2) * Math.pow(tpx, 2) + (N * minUsefulGain + fp * minUsefulGain)
						* Math.pow(P, 2) - 4 * fp * Math.pow(tpx, 2) + 2
						* (currentBaseInfo * fp * tpx + (currentBaseInfo * tpx + tpx * minUsefulGain) * N) * P + Math
						.sqrt(16
								* N
								* (currentBaseInfo - 2)
								* fp
								* Math.pow(tpx, 4)
								+ 16
								* Math.pow(fp, 2)
								* Math.pow(tpx, 4)
								+ (Math.pow(N, 2) * Math.pow(minUsefulGain, 2) - 2 * N
										* fp * Math.pow(minUsefulGain, 2) + Math.pow(
										fp, 2) * Math.pow(minUsefulGain, 2))
								* Math.pow(P, 4)
								+ 4
								* (Math.pow(N, 2) * currentBaseInfo * tpx * minUsefulGain - 2 * N * currentBaseInfo
										* fp * tpx * minUsefulGain + currentBaseInfo
										* Math.pow(fp, 2) * tpx * minUsefulGain)
								* Math.pow(P, 3)
								+ 4
								* ((Math.pow(currentBaseInfo, 2) - 4 * currentBaseInfo + 4)
										* Math.pow(tpx, 4) + 4
										* Math.pow(tpx, 4) * minUsefulGain)
								* Math.pow(N, 2)
								+ 4
								* (Math.pow(currentBaseInfo, 2)
										* Math.pow(fp, 2)
										* Math.pow(tpx, 2)
										- 2
										* Math.pow(fp, 2)
										* Math.pow(tpx, 2)
										* minUsefulGain
										+ (Math.pow(currentBaseInfo, 2)
												* Math.pow(tpx, 2) + (currentBaseInfo - 2)
												* Math.pow(tpx, 2) * minUsefulGain)
										* Math.pow(N, 2) - (2
										* Math.pow(currentBaseInfo, 2) * fp
										* Math.pow(tpx, 2) + (currentBaseInfo - 4) * fp
										* Math.pow(tpx, 2) * minUsefulGain)
										* N)
								* Math.pow(P, 2)
								+ 8
								* ((Math.pow(currentBaseInfo, 2) - 2 * currentBaseInfo)
										* Math.pow(N, 2) * Math.pow(tpx, 3)
										- (Math.pow(currentBaseInfo, 2) - 4 * currentBaseInfo) * N * fp
										* Math.pow(tpx, 3) - 2 * currentBaseInfo
										* Math.pow(fp, 2)
										* Math.pow(tpx, 3)) * P))
				/ (2 * P * currentBaseInfo * tpx + Math.pow(P, 2) * minUsefulGain - 4 * Math.pow(tpx,
						2));
		
				if (!isInfinity(x) && x >= y)
					result = x;
				else if (!isInfinity(y) && y > x)
					result = y;
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
		
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		
		InformationContext icParent;
		if (currentNode.getParent() == null)
			 icParent = currentNode.getRoot().getInformationContext();
		else
			icParent = currentNode.getParent().getInformationContext();
			
		Coverage origParentCoverage = icParent.getOrigCoverage();
		double P = origParentCoverage.getPositiveBindings().size();
		double N = origParentCoverage.getNegativeBindings().size();
		
		ConfusionMatrix origcm = cml.getOrigcm();
		double tp = origcm.getTruePositives();
		double fp = origcm.getFalsePositives();
		
		if (tp != 0 && N != 0)
			result = -2 * (icCurrentNode.getScore() - 2 * tp / (P * (tp / P + 1))) * fp * tp
			/ (N * P * (fp / N + tp / P));
	
		if (isInfinity (result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}
}