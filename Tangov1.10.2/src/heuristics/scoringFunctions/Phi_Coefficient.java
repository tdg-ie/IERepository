package heuristics.scoringFunctions;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;
import thresholds.Thresholds;

public class Phi_Coefficient extends Heuristic {
	
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
		double tnx = cm.getTrueNegatives();
		double fnx = cm.getFalseNegatives();
		
		if ((fpx + tpx) != 0) {
			result = -(fnx * fpx - tnx * tpx - 1)
			/ Math.sqrt((fnx + tnx) * (fnx + tpx) * (fpx + tnx)
					* (fpx + tpx) + 1);
		}
		
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
//			System.exit(0);
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
		double x, y;
		double minUsefulGain = thresholds.getMinUsefulGain();
		
		InformationContext icParent;
		if (node.getParent() == null)
			 icParent = node.getRoot().getInformationContext();
		else
			icParent = node.getParent().getInformationContext();
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix origcm = cml.getOrigcm();
		ConfusionMatrix cmParent = icParent.getConfusionMatrixList().getOrigcm();
		double tpx = origcm.getTruePositives();
		double fnx = origcm.getFalseNegatives();
		double N = cmParent.getFalsePositives() + cmParent.getTrueNegatives();
		
		if (tpx != 0) {
			x = (Math.sqrt(Math.pow(fnx, 3)
					* N
					* (Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * N + 2 * currentBaseInfo * tpx * N * minUsefulGain
							+ 4 * Math.pow(tpx, 3) + N * Math.pow(minUsefulGain, 2))
					+ Math.pow(fnx, 2)
					* N
					* (Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * N * (3 * tpx + 2 * N)
							+ 2 * currentBaseInfo * tpx * N * minUsefulGain * (3 * tpx + 2 * N) + 8
							* Math.pow(tpx, 4) + 8 * Math.pow(tpx, 3) * N + 4
							* Math.pow(tpx, 2) + 3 * tpx * N * Math.pow(minUsefulGain, 2) + 2
							* Math.pow(N, 2) * Math.pow(minUsefulGain, 2))
					+ fnx
					* (Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * Math.pow(N, 2)
							* (tpx + N) * (3 * tpx + N) + 2 * currentBaseInfo * tpx
							* Math.pow(N, 2) * minUsefulGain * (tpx + N) * (3 * tpx + N) + 4
							* Math.pow(tpx, 5) * N + 8 * Math.pow(tpx, 4)
							* Math.pow(N, 2) + 4 * Math.pow(tpx, 3)
							* Math.pow(N, 3) + Math.pow(tpx, 2)
							* (Math.pow(N, 2) * (3 * Math.pow(minUsefulGain, 2) + 4) + 4) + 4
							* tpx * Math.pow(N, 3) * Math.pow(minUsefulGain, 2) + Math
							.pow(N, 4) * Math.pow(minUsefulGain, 2))
					+ Math.pow(currentBaseInfo, 2)
					* Math.pow(tpx, 2)
					* N
					* (Math.pow(tpx, 3) * N + 2 * Math.pow(tpx, 2) * Math.pow(N, 2)
							+ tpx * Math.pow(N, 3) + 4)
					+ 2
					* currentBaseInfo
					* tpx
					* N
					* minUsefulGain
					* (Math.pow(tpx, 3) * N + 2 * Math.pow(tpx, 2) * Math.pow(N, 2)
							+ tpx * Math.pow(N, 3) + 4) - 4 * Math.pow(tpx, 4) * N
					+ Math.pow(tpx, 3)
					* (Math.pow(N, 2) * (Math.pow(minUsefulGain, 2) - 4) + 4) + 2
					* Math.pow(tpx, 2) * N * (Math.pow(N, 2) * Math.pow(minUsefulGain, 2) - 2)
					+ tpx * Math.pow(N, 4) * Math.pow(minUsefulGain, 2) + 4 * N
					* Math.pow(minUsefulGain, 2))
					* Math.abs(currentBaseInfo * tpx + minUsefulGain) + Math.sqrt(fnx + tpx)
					* (fnx * N * Math.pow(currentBaseInfo * tpx + minUsefulGain, 2) + Math.pow(currentBaseInfo, 2)
							* Math.pow(tpx, 2) * N * (N - tpx) + 2 * currentBaseInfo * tpx * N
							* minUsefulGain * (N - tpx) + 2 * Math.pow(tpx, 3) * N + 2
							* Math.pow(tpx, 2) - tpx * N * Math.pow(minUsefulGain, 2) + Math
							.pow(N, 2) * Math.pow(minUsefulGain, 2)))
					/ (2 * Math.sqrt(fnx + tpx) * (fnx * Math.pow(tpx, 2)
							+ Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * N + 2 * currentBaseInfo * tpx
							* N * minUsefulGain + Math.pow(tpx, 3) + N * Math.pow(minUsefulGain, 2)));
			 y =  (Math.sqrt(fnx + tpx)
							* (fnx * N * Math.pow(currentBaseInfo * tpx + minUsefulGain, 2) + Math.pow(currentBaseInfo, 2)
									* Math.pow(tpx, 2) * N * (N - tpx) + 2 * currentBaseInfo * tpx * N
									* minUsefulGain * (N - tpx) + 2 * Math.pow(tpx, 3) * N + 2
									* Math.pow(tpx, 2) - tpx * N * Math.pow(minUsefulGain, 2) + Math
									.pow(N, 2) * Math.pow(minUsefulGain, 2)) - Math.sqrt(Math.pow(fnx,
							3)
							* N
							* (Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * N + 2 * currentBaseInfo * tpx * N * minUsefulGain
									+ 4 * Math.pow(tpx, 3) + N * Math.pow(minUsefulGain, 2))
							+ Math.pow(fnx, 2)
							* N
							* (Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * N * (3 * tpx + 2 * N)
									+ 2 * currentBaseInfo * tpx * N * minUsefulGain * (3 * tpx + 2 * N) + 8
									* Math.pow(tpx, 4) + 8 * Math.pow(tpx, 3) * N + 4
									* Math.pow(tpx, 2) + 3 * tpx * N * Math.pow(minUsefulGain, 2) + 2
									* Math.pow(N, 2) * Math.pow(minUsefulGain, 2))
							+ fnx
							* (Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * Math.pow(N, 2)
									* (tpx + N) * (3 * tpx + N) + 2 * currentBaseInfo * tpx
									* Math.pow(N, 2) * minUsefulGain * (tpx + N) * (3 * tpx + N) + 4
									* Math.pow(tpx, 5) * N + 8 * Math.pow(tpx, 4)
									* Math.pow(N, 2) + 4 * Math.pow(tpx, 3)
									* Math.pow(N, 3) + Math.pow(tpx, 2)
									* (Math.pow(N, 2) * (3 * Math.pow(minUsefulGain, 2) + 4) + 4) + 4
									* tpx * Math.pow(N, 3) * Math.pow(minUsefulGain, 2) + Math
									.pow(N, 4) * Math.pow(minUsefulGain, 2))
							+ Math.pow(currentBaseInfo, 2)
							* Math.pow(tpx, 2)
							* N
							* (Math.pow(tpx, 3) * N + 2 * Math.pow(tpx, 2) * Math.pow(N, 2)
									+ tpx * Math.pow(N, 3) + 4)
							+ 2
							* currentBaseInfo
							* tpx
							* N
							* minUsefulGain
							* (Math.pow(tpx, 3) * N + 2 * Math.pow(tpx, 2) * Math.pow(N, 2)
									+ tpx * Math.pow(N, 3) + 4)
							- 4
							* Math.pow(tpx, 4)
							* N
							+ Math.pow(tpx, 3)
							* (Math.pow(N, 2) * (Math.pow(minUsefulGain, 2) - 4) + 4)
							+ 2
							* Math.pow(tpx, 2)
							* N
							* (Math.pow(N, 2) * Math.pow(minUsefulGain, 2) - 2)
							+ tpx
							* Math.pow(N, 4)
							* Math.pow(minUsefulGain, 2)
							+ 4
							* N
							* Math.pow(minUsefulGain, 2))
							* Math.abs(currentBaseInfo * tpx + minUsefulGain))
							/ (2 * Math.sqrt(fnx + tpx) * (fnx * Math.pow(tpx, 2)
									+ Math.pow(currentBaseInfo, 2) * Math.pow(tpx, 2) * N + 2 * currentBaseInfo * tpx
									* N * minUsefulGain + Math.pow(tpx, 3) + N * Math.pow(minUsefulGain, 2)));
			
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
	    	   
		if (tp != 0)
			result = -(icCurrentNode.getScore() - (fp * tp + 1)
					/ Math.sqrt(Math.pow(fp, 2) * Math.pow(tp, 2) + 1))
					* tp;
		if (isInfinity(result)) {
			System.err.println("Error, division by 0");
			System.exit(0);
		}
		
		return result;
	}
}