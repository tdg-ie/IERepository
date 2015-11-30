package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class Collective_Strength extends Heuristic{
	
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
		double tot = cm.getN() + 1; // to avoid that one of the denominators is 0 when the rule is perfect (N - tpx - tnx = 0).
		
		
		if (tnx + fpx != 0) { // at least one of them must be different from 0
			result = (-((fnx + tnx) * (fpx + tnx) + (fnx + tpx) * (fpx + tpx) - tot) * (tnx + tpx)) 
			/ (((fnx + tnx) * (fpx + tnx) + (fnx + tpx) * (fpx + tpx)) * (tot- tnx - tpx));
		}
		
		if (isInfinity (result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}

	public double threshold(Node<Literal,InformationContext> node , double currentBaseInfo, Thresholds thresholds) {
	/**
	 * Compute the maximum number N1 of neg tuples that would allow P1 pos tuples
	 * (P orig pos tuples) to five a gain >= threshold. The underlying relation is
	 * P * (scoringFunction - Current.BaseInfo) >= MinUsefulGain
	 * where N1 is adjusted by the sampling factor
	 * 
	 * NOTE: This is the inverse of the gain calculation in Worth. If one is
	 * changed, the other must be modified accordingly
	 */
		double result = 0;
		double x, y;
		double minUsefulGain = thresholds.getMinUsefulGain();

		InformationContext icParent;
		if (node.getParent() == null)
			icParent = node.getRoot().getInformationContext();
		else
			icParent = node.getParent().getInformationContext();

		ConfusionMatrixList cml = node.getInformationContext()
				.getConfusionMatrixList();
		ConfusionMatrix cmParent = icParent.getConfusionMatrixList()
				.getOrigcm();
		ConfusionMatrix cm = cml.getOrigcm();

		double tpx = cm.getTruePositives();
		double fnx = cm.getFalsePositives();
		double tot = cmParent.getN() + 1;
		double N = tot - tpx - fnx;

		if (tpx != 0) {
			x = -1
					/ 2
					* (2
							* ((currentBaseInfo - 1) * tpx + minUsefulGain)
							* Math.pow(N, 2)
							+ (currentBaseInfo * Math.pow(tpx, 2)
									- (currentBaseInfo * tpx + minUsefulGain)
									* N + (currentBaseInfo * fnx + 1) * tpx + (fnx + tpx)
									* minUsefulGain) * tot + Math
							.sqrt(4
									* (Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
									* Math.pow(fnx, 2)
									* Math.pow(tpx, 4)
									+ 8
									* (Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
									* fnx
									* Math.pow(tpx, 5)
									+ 4
									* (Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
									* Math.pow(tpx, 6)
									+ 4
									* ((Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
											* Math.pow(fnx, 2)
											* Math.pow(tpx, 2)
											+ 2
											* (currentBaseInfo - 1)
											* Math.pow(fnx, 2)
											* tpx
											* minUsefulGain + Math.pow(fnx, 2)
											* Math.pow(minUsefulGain, 2))
									* Math.pow(N, 2)
									+ (Math.pow(currentBaseInfo, 2)
											* Math.pow(tpx, 4)
											+ 2
											* (Math.pow(currentBaseInfo, 2)
													* fnx + currentBaseInfo)
											* Math.pow(tpx, 3)
											+ (Math.pow(currentBaseInfo, 2)
													* Math.pow(tpx, 2) + 2
													* currentBaseInfo * tpx
													* minUsefulGain + Math.pow(
													minUsefulGain, 2))
											* Math.pow(N, 2)
											+ (Math.pow(currentBaseInfo, 2)
													* Math.pow(fnx, 2) + 2
													* currentBaseInfo * fnx + 1)
											* Math.pow(tpx, 2)
											+ (Math.pow(fnx, 2) + 2 * fnx * tpx + Math
													.pow(tpx, 2))
											* Math.pow(minUsefulGain, 2)
											- 2
											* (Math.pow(currentBaseInfo, 2)
													* Math.pow(tpx, 3)
													+ (Math.pow(
															currentBaseInfo, 2)
															* fnx + currentBaseInfo)
													* Math.pow(tpx, 2)
													+ (fnx + tpx)
													* Math.pow(minUsefulGain, 2) + (2
													* currentBaseInfo
													* Math.pow(tpx, 2) + (2
													* currentBaseInfo * fnx + 1)
													* tpx)
													* minUsefulGain) * N + 2
											* (currentBaseInfo
													* Math.pow(tpx, 3)
													+ (2 * currentBaseInfo
															* fnx + 1)
													* Math.pow(tpx, 2) + (currentBaseInfo
													* Math.pow(fnx, 2) + fnx)
													* tpx) * minUsefulGain)
									* Math.pow(tot, 2)
									+ 4
									* (Math.pow(fnx, 2) * Math.pow(tpx, 2) + 2
											* fnx * Math.pow(tpx, 3) + Math
											.pow(tpx, 4))
									* Math.pow(minUsefulGain, 2)
									+ 8
									* ((Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
											* Math.pow(fnx, 2)
											* Math.pow(tpx, 3)
											+ (Math.pow(currentBaseInfo, 2) - 2
													* currentBaseInfo + 1)
											* fnx
											* Math.pow(tpx, 4)
											+ (Math.pow(fnx, 2) * tpx + fnx
													* Math.pow(tpx, 2))
											* Math.pow(minUsefulGain, 2) + 2
											* ((currentBaseInfo - 1)
													* Math.pow(fnx, 2)
													* Math.pow(tpx, 2) + (currentBaseInfo - 1)
													* fnx * Math.pow(tpx, 3))
											* minUsefulGain)
									* N
									- 4
									* ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
											* Math.pow(tpx, 5)
											+ (2
													* (Math.pow(
															currentBaseInfo, 2) - currentBaseInfo)
													* fnx - currentBaseInfo + 1)
											* Math.pow(tpx, 4)
											+ ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
													* Math.pow(fnx, 2) - (currentBaseInfo - 1)
													* fnx)
											* Math.pow(tpx, 3)
											- ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
													* fnx
													* Math.pow(tpx, 2)
													+ (2 * currentBaseInfo - 1)
													* fnx * tpx * minUsefulGain + fnx
													* Math.pow(minUsefulGain, 2))
											* Math.pow(N, 2)
											+ (Math.pow(fnx, 2) * tpx + 2 * fnx
													* Math.pow(tpx, 2) + Math
													.pow(tpx, 3))
											* Math.pow(minUsefulGain, 2)
											- ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
													* Math.pow(tpx, 4)
													- ((Math.pow(
															currentBaseInfo, 2) - currentBaseInfo)
															* Math.pow(fnx, 2) - (currentBaseInfo - 1)
															* fnx)
													* Math.pow(tpx, 2)
													- (Math.pow(fnx, 2) - Math
															.pow(tpx, 2))
													* Math.pow(minUsefulGain, 2) + ((2 * currentBaseInfo - 1)
													* Math.pow(tpx, 3) - ((2 * currentBaseInfo - 1)
													* Math.pow(fnx, 2) - fnx)
													* tpx)
													* minUsefulGain) * N + ((2 * currentBaseInfo - 1)
											* Math.pow(tpx, 4)
											+ (2 * (2 * currentBaseInfo - 1)
													* fnx - 1)
											* Math.pow(tpx, 3) + ((2 * currentBaseInfo - 1)
											* Math.pow(fnx, 2) - fnx)
											* Math.pow(tpx, 2))
											* minUsefulGain)
									* tot
									+ 8
									* ((currentBaseInfo - 1) * Math.pow(fnx, 2)
											* Math.pow(tpx, 3) + 2
											* (currentBaseInfo - 1) * fnx
											* Math.pow(tpx, 4) + (currentBaseInfo - 1)
											* Math.pow(tpx, 5)) * minUsefulGain))
					/ ((currentBaseInfo - 1) * fnx * tpx
							+ (currentBaseInfo - 1) * Math.pow(tpx, 2)
							- ((currentBaseInfo - 1) * tpx + minUsefulGain) * N + (fnx + tpx)
							* minUsefulGain);

			y = -1
					/ 2
					* (2
							* ((currentBaseInfo - 1) * tpx + minUsefulGain)
							* Math.pow(N, 2)
							+ (currentBaseInfo * Math.pow(tpx, 2)
									- (currentBaseInfo * tpx + minUsefulGain)
									* N + (currentBaseInfo * fnx + 1) * tpx + (fnx + tpx)
									* minUsefulGain) * tot - Math
							.sqrt(4
									* (Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
									* Math.pow(fnx, 2)
									* Math.pow(tpx, 4)
									+ 8
									* (Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
									* fnx
									* Math.pow(tpx, 5)
									+ 4
									* (Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
									* Math.pow(tpx, 6)
									+ 4
									* ((Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
											* Math.pow(fnx, 2)
											* Math.pow(tpx, 2)
											+ 2
											* (currentBaseInfo - 1)
											* Math.pow(fnx, 2)
											* tpx
											* minUsefulGain + Math.pow(fnx, 2)
											* Math.pow(minUsefulGain, 2))
									* Math.pow(N, 2)
									+ (Math.pow(currentBaseInfo, 2)
											* Math.pow(tpx, 4)
											+ 2
											* (Math.pow(currentBaseInfo, 2)
													* fnx + currentBaseInfo)
											* Math.pow(tpx, 3)
											+ (Math.pow(currentBaseInfo, 2)
													* Math.pow(tpx, 2) + 2
													* currentBaseInfo * tpx
													* minUsefulGain + Math.pow(
													minUsefulGain, 2))
											* Math.pow(N, 2)
											+ (Math.pow(currentBaseInfo, 2)
													* Math.pow(fnx, 2) + 2
													* currentBaseInfo * fnx + 1)
											* Math.pow(tpx, 2)
											+ (Math.pow(fnx, 2) + 2 * fnx * tpx + Math
													.pow(tpx, 2))
											* Math.pow(minUsefulGain, 2)
											- 2
											* (Math.pow(currentBaseInfo, 2)
													* Math.pow(tpx, 3)
													+ (Math.pow(
															currentBaseInfo, 2)
															* fnx + currentBaseInfo)
													* Math.pow(tpx, 2)
													+ (fnx + tpx)
													* Math.pow(minUsefulGain, 2) + (2
													* currentBaseInfo
													* Math.pow(tpx, 2) + (2
													* currentBaseInfo * fnx + 1)
													* tpx)
													* minUsefulGain) * N + 2
											* (currentBaseInfo
													* Math.pow(tpx, 3)
													+ (2 * currentBaseInfo
															* fnx + 1)
													* Math.pow(tpx, 2) + (currentBaseInfo
													* Math.pow(fnx, 2) + fnx)
													* tpx) * minUsefulGain)
									* Math.pow(tot, 2)
									+ 4
									* (Math.pow(fnx, 2) * Math.pow(tpx, 2) + 2
											* fnx * Math.pow(tpx, 3) + Math
											.pow(tpx, 4))
									* Math.pow(minUsefulGain, 2)
									+ 8
									* ((Math.pow(currentBaseInfo, 2) - 2
											* currentBaseInfo + 1)
											* Math.pow(fnx, 2)
											* Math.pow(tpx, 3)
											+ (Math.pow(currentBaseInfo, 2) - 2
													* currentBaseInfo + 1)
											* fnx
											* Math.pow(tpx, 4)
											+ (Math.pow(fnx, 2) * tpx + fnx
													* Math.pow(tpx, 2))
											* Math.pow(minUsefulGain, 2) + 2
											* ((currentBaseInfo - 1)
													* Math.pow(fnx, 2)
													* Math.pow(tpx, 2) + (currentBaseInfo - 1)
													* fnx * Math.pow(tpx, 3))
											* minUsefulGain)
									* N
									- 4
									* ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
											* Math.pow(tpx, 5)
											+ (2
													* (Math.pow(
															currentBaseInfo, 2) - currentBaseInfo)
													* fnx - currentBaseInfo + 1)
											* Math.pow(tpx, 4)
											+ ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
													* Math.pow(fnx, 2) - (currentBaseInfo - 1)
													* fnx)
											* Math.pow(tpx, 3)
											- ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
													* fnx
													* Math.pow(tpx, 2)
													+ (2 * currentBaseInfo - 1)
													* fnx * tpx * minUsefulGain + fnx
													* Math.pow(minUsefulGain, 2))
											* Math.pow(N, 2)
											+ (Math.pow(fnx, 2) * tpx + 2 * fnx
													* Math.pow(tpx, 2) + Math
													.pow(tpx, 3))
											* Math.pow(minUsefulGain, 2)
											- ((Math.pow(currentBaseInfo, 2) - currentBaseInfo)
													* Math.pow(tpx, 4)
													- ((Math.pow(
															currentBaseInfo, 2) - currentBaseInfo)
															* Math.pow(fnx, 2) - (currentBaseInfo - 1)
															* fnx)
													* Math.pow(tpx, 2)
													- (Math.pow(fnx, 2) - Math
															.pow(tpx, 2))
													* Math.pow(minUsefulGain, 2) + ((2 * currentBaseInfo - 1)
													* Math.pow(tpx, 3) - ((2 * currentBaseInfo - 1)
													* Math.pow(fnx, 2) - fnx)
													* tpx)
													* minUsefulGain) * N + ((2 * currentBaseInfo - 1)
											* Math.pow(tpx, 4)
											+ (2 * (2 * currentBaseInfo - 1)
													* fnx - 1)
											* Math.pow(tpx, 3) + ((2 * currentBaseInfo - 1)
											* Math.pow(fnx, 2) - fnx)
											* Math.pow(tpx, 2))
											* minUsefulGain)
									* tot
									+ 8
									* ((currentBaseInfo - 1) * Math.pow(fnx, 2)
											* Math.pow(tpx, 3) + 2
											* (currentBaseInfo - 1) * fnx
											* Math.pow(tpx, 4) + (currentBaseInfo - 1)
											* Math.pow(tpx, 5)) * minUsefulGain))
					/ ((currentBaseInfo - 1) * fnx * tpx
							+ (currentBaseInfo - 1) * Math.pow(tpx, 2)
							- ((currentBaseInfo - 1) * tpx + minUsefulGain) * N + (fnx + tpx)
							* minUsefulGain);

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
		
		InformationContext icCurrentNode = currentNode.getInformationContext();
		ConfusionMatrixList cml = icCurrentNode.getConfusionMatrixList();
		
		ConfusionMatrix cm = cml.getOrigcm();
		double tp = cm.getTruePositives();
		double fp = cm.getFalsePositives();
		double tot = cm.getN() + 1;
		
		if (tp != 0) {
			// We have add + 1 in the numerator and denominator to avoid denominator = 0
			result = -(icCurrentNode.getScore() + (Math.pow(fp, 2) + Math.pow(tp, 2) - tot) * (fp + tp)
					/ ((Math.pow(fp, 2) + Math.pow(tp, 2)) * (tot - fp - tp)))
					* tp;
		}
		
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}
}