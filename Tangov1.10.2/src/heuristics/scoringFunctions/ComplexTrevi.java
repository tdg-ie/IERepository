package heuristics.scoringFunctions;

import java.util.List;

import coverageComputation.Bits;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import dataCoverage.Coverage;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;
import utilities.Utilities;

public class ComplexTrevi extends Heuristic {
	
	private double threshold = 0.2;
	
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
		
		InformationContext icRoot = node.getRoot().getInformationContext();
		Coverage origRootCoverage = icRoot.getOrigCoverage();
		double P = origRootCoverage.getPositiveBindings().size();
		double N = origRootCoverage.getNegativeBindings().size();
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tpx = origcm.getTruePositives();
		double fpx = origcm.getFalsePositives();
		double fp = origcm.getFalsePositives() + origcm.getTrueNegatives();
		
		double termini = (2 * tpx * (fp - fpx)) / ((N * tpx) + P * (fp - fpx));
		 
		double q = Bits.ldiRule(node.getFullPath());
		double z = Utilities.encode((int) tpx, (int) (N + P));
		
		double G = termini * (score(node) - currentScore) - ((q/z) * threshold);		
		
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
		 
		if (tpx != 0)
			result = (2 * tpx * (N - fpx))/ ((N * tpx + P * (N - fpx)));
		
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
		
		InformationContext icRoot = node.getRoot().getInformationContext();
		Coverage origRootCoverage = icRoot.getOrigCoverage();
		double P = origRootCoverage.getPositiveBindings().size();
		double N = origRootCoverage.getNegativeBindings().size();
		
		InformationContext icParent = node.getRoot().getInformationContext();
		Coverage origParentCoverage = icParent.getOrigCoverage();
		double tp = origParentCoverage.getPositiveBindings().size();
		double fp = origParentCoverage.getNegativeBindings().size();
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tpx = origcm.getTruePositives();
		
		
		if (tpx != 0) {
			List<Node<Literal, InformationContext>> ruleNodes = node.getFullPath();
			double q = (Bits.ldiRule(ruleNodes) + averageNumberOfBits(ruleNodes)) * threshold;
			double z = Utilities.encode((int) tp, (int) (N + P));

			x = 1
					/ 2
					* (2
							* N
							* (currentBaseInfo - 2)
							* Math.pow(tpx, 2)
							* z
							- 4
							* fp
							* Math.pow(tpx, 2)
							* z
							+ (fp * minUsefulGain * z + (minUsefulGain * z + q)
									* N + fp * q)
							* Math.pow(P, 2)
							+ 2
							* (currentBaseInfo * fp * tpx * z + (q * tpx + (currentBaseInfo
									* tpx + tpx * minUsefulGain)
									* z)
									* N) * P - Math
							.sqrt(16
									* N
									* (currentBaseInfo - 2)
									* fp
									* Math.pow(tpx, 4)
									* Math.pow(z, 2)
									+ 16
									* Math.pow(fp, 2)
									* Math.pow(tpx, 4)
									* Math.pow(z, 2)
									+ (Math.pow(fp, 2)
											* Math.pow(minUsefulGain, 2)
											* Math.pow(z, 2)
											+ 2
											* Math.pow(fp, 2)
											* q
											* minUsefulGain
											* z
											+ Math.pow(fp, 2)
											* Math.pow(q, 2)
											+ (Math.pow(minUsefulGain, 2)
													* Math.pow(z, 2) + 2 * q
													* minUsefulGain * z + Math
													.pow(q, 2))
											* Math.pow(N, 2) - 2
											* (fp * Math.pow(minUsefulGain, 2)
													* Math.pow(z, 2) + 2 * fp
													* q * minUsefulGain * z + fp
													* Math.pow(q, 2)) * N)
									* Math.pow(P, 4)
									+ 4
									* (currentBaseInfo
											* Math.pow(fp, 2)
											* tpx
											* minUsefulGain
											* Math.pow(z, 2)
											+ currentBaseInfo
											* Math.pow(fp, 2)
											* q
											* tpx
											* z
											+ (currentBaseInfo * tpx
													* minUsefulGain
													* Math.pow(z, 2) + currentBaseInfo
													* q * tpx * z)
											* Math.pow(N, 2) - 2
											* (currentBaseInfo * fp * tpx
													* minUsefulGain
													* Math.pow(z, 2) + currentBaseInfo
													* fp * q * tpx * z) * N)
									* Math.pow(P, 3)
									+ 4
									* (4 * q * Math.pow(tpx, 4) * z + ((Math
											.pow(currentBaseInfo, 2)
											- 4
											* currentBaseInfo + 4)
											* Math.pow(tpx, 4) + 4
											* Math.pow(tpx, 4) * minUsefulGain)
											* Math.pow(z, 2))
									* Math.pow(N, 2)
									- 4
									* (2
											* Math.pow(fp, 2)
											* q
											* Math.pow(tpx, 2)
											* z
											- ((currentBaseInfo - 2) * q
													* Math.pow(tpx, 2) * z + (Math
													.pow(currentBaseInfo, 2)
													* Math.pow(tpx, 2) + (currentBaseInfo - 2)
													* Math.pow(tpx, 2)
													* minUsefulGain)
													* Math.pow(z, 2))
											* Math.pow(N, 2)
											- (Math.pow(currentBaseInfo, 2)
													* Math.pow(fp, 2)
													* Math.pow(tpx, 2) - 2
													* Math.pow(fp, 2)
													* Math.pow(tpx, 2)
													* minUsefulGain)
											* Math.pow(z, 2) + ((currentBaseInfo - 4)
											* fp * q * Math.pow(tpx, 2) * z + (2
											* Math.pow(currentBaseInfo, 2)
											* fp
											* Math.pow(tpx, 2) + (currentBaseInfo - 4)
											* fp
											* Math.pow(tpx, 2)
											* minUsefulGain)
											* Math.pow(z, 2))
											* N)
									* Math.pow(P, 2)
									+ 8
									* ((Math.pow(currentBaseInfo, 2) - 2 * currentBaseInfo)
											* Math.pow(N, 2)
											* Math.pow(tpx, 3)
											* Math.pow(z, 2)
											- (Math.pow(currentBaseInfo, 2) - 4 * currentBaseInfo)
											* N
											* fp
											* Math.pow(tpx, 3)
											* Math.pow(z, 2) - 2
											* currentBaseInfo * Math.pow(fp, 2)
											* Math.pow(tpx, 3) * Math.pow(z, 2))
									* P))
					/ (2 * P * currentBaseInfo * tpx * z
							+ (minUsefulGain * z + q) * Math.pow(P, 2) - 4
							* Math.pow(tpx, 2) * z);
			y = 1
					/ 2
					* (2
							* N
							* (currentBaseInfo - 2)
							* Math.pow(tpx, 2)
							* z
							- 4
							* fp
							* Math.pow(tpx, 2)
							* z
							+ (fp * minUsefulGain * z + (minUsefulGain * z + q)
									* N + fp * q)
							* Math.pow(P, 2)
							+ 2
							* (currentBaseInfo * fp * tpx * z + (q * tpx + (currentBaseInfo
									* tpx + tpx * minUsefulGain)
									* z)
									* N) * P + Math
							.sqrt(16
									* N
									* (currentBaseInfo - 2)
									* fp
									* Math.pow(tpx, 4)
									* Math.pow(z, 2)
									+ 16
									* Math.pow(fp, 2)
									* Math.pow(tpx, 4)
									* Math.pow(z, 2)
									+ (Math.pow(fp, 2)
											* Math.pow(minUsefulGain, 2)
											* Math.pow(z, 2)
											+ 2
											* Math.pow(fp, 2)
											* q
											* minUsefulGain
											* z
											+ Math.pow(fp, 2)
											* Math.pow(q, 2)
											+ (Math.pow(minUsefulGain, 2)
													* Math.pow(z, 2) + 2 * q
													* minUsefulGain * z + Math
													.pow(q, 2))
											* Math.pow(N, 2) - 2
											* (fp * Math.pow(minUsefulGain, 2)
													* Math.pow(z, 2) + 2 * fp
													* q * minUsefulGain * z + fp
													* Math.pow(q, 2)) * N)
									* Math.pow(P, 4)
									+ 4
									* (currentBaseInfo
											* Math.pow(fp, 2)
											* tpx
											* minUsefulGain
											* Math.pow(z, 2)
											+ currentBaseInfo
											* Math.pow(fp, 2)
											* q
											* tpx
											* z
											+ (currentBaseInfo * tpx
													* minUsefulGain
													* Math.pow(z, 2) + currentBaseInfo
													* q * tpx * z)
											* Math.pow(N, 2) - 2
											* (currentBaseInfo * fp * tpx
													* minUsefulGain
													* Math.pow(z, 2) + currentBaseInfo
													* fp * q * tpx * z) * N)
									* Math.pow(P, 3)
									+ 4
									* (4 * q * Math.pow(tpx, 4) * z + ((Math
											.pow(currentBaseInfo, 2)
											- 4
											* currentBaseInfo + 4)
											* Math.pow(tpx, 4) + 4
											* Math.pow(tpx, 4) * minUsefulGain)
											* Math.pow(z, 2))
									* Math.pow(N, 2)
									- 4
									* (2
											* Math.pow(fp, 2)
											* q
											* Math.pow(tpx, 2)
											* z
											- ((currentBaseInfo - 2) * q
													* Math.pow(tpx, 2) * z + (Math
													.pow(currentBaseInfo, 2)
													* Math.pow(tpx, 2) + (currentBaseInfo - 2)
													* Math.pow(tpx, 2)
													* minUsefulGain)
													* Math.pow(z, 2))
											* Math.pow(N, 2)
											- (Math.pow(currentBaseInfo, 2)
													* Math.pow(fp, 2)
													* Math.pow(tpx, 2) - 2
													* Math.pow(fp, 2)
													* Math.pow(tpx, 2)
													* minUsefulGain)
											* Math.pow(z, 2) + ((currentBaseInfo - 4)
											* fp * q * Math.pow(tpx, 2) * z + (2
											* Math.pow(currentBaseInfo, 2)
											* fp
											* Math.pow(tpx, 2) + (currentBaseInfo - 4)
											* fp
											* Math.pow(tpx, 2)
											* minUsefulGain)
											* Math.pow(z, 2))
											* N)
									* Math.pow(P, 2)
									+ 8
									* ((Math.pow(currentBaseInfo, 2) - 2 * currentBaseInfo)
											* Math.pow(N, 2)
											* Math.pow(tpx, 3)
											* Math.pow(z, 2)
											- (Math.pow(currentBaseInfo, 2) - 4 * currentBaseInfo)
											* N
											* fp
											* Math.pow(tpx, 3)
											* Math.pow(z, 2) - 2
											* currentBaseInfo * Math.pow(fp, 2)
											* Math.pow(tpx, 3) * Math.pow(z, 2))
									* P))
					/ (2 * P * currentBaseInfo * tpx * z
							+ (minUsefulGain * z + q) * Math.pow(P, 2) - 4
							* Math.pow(tpx, 2) * z);

			if (!isInfinity(x) && x >= y)
				result = x;
			else if (!isInfinity(y) && y > x)
				result = y;
		}
			
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
			 
		return result;
	}

	@Override
	public double getMaxPossibleGain(Node<Literal,InformationContext> currentNode) {
		double result = 0; 
		double q, z;
		
		InformationContext icRoot, icCurrentNode;
		List<Node<Literal, InformationContext>> ruleNodes;
		
		icRoot = currentNode.getRoot().getInformationContext();
		Coverage origRootCoverage = icRoot.getOrigCoverage();
		double P = origRootCoverage.getPositiveBindings().size();
		double N = origRootCoverage.getNegativeBindings().size();
		
		icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		double tp = origcm.getTruePositives();
		double fp = origcm.getFalsePositives();
		ruleNodes = currentNode.getFullPath();
		q = (Bits.ldiRule(ruleNodes) + averageNumberOfBits(ruleNodes)) * threshold;
		z = Utilities.encode((int) tp, (int) (N + P));

		if (tp != 0)
			result = -q / z - 2 * (icCurrentNode.getScore() - 2 * tp / (P * (tp / P + 1))) * fp * tp
			/ (N * P * (fp / N + tp / P));
	
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}

	private double averageNumberOfBits(
			List<Node<Literal, InformationContext>> ruleNodes) {
		double result = 0;
		for(Node<Literal, InformationContext> n: ruleNodes)
			result += n.getInformationContext().getBits();
		result = result / ruleNodes.size();
		return result;
	}
}