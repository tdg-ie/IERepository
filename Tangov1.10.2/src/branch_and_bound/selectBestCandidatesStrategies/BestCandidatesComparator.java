package branch_and_bound.selectBestCandidatesStrategies;

import java.util.Comparator;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;

public abstract class BestCandidatesComparator implements Comparator<Node<Literal, InformationContext>> {

	double maxGain = 0.0;
	
	public double getMaxGain() {
		return maxGain;
	}
	
	public void setMaxGain(double maxGain) {
		this.maxGain = maxGain;
	}

	/**
	 * Compares two arguments to order them. 
	 * Returns 
	 * 		a negative integer if the first argument is less than the second one, 
	 * 		zero, if the first one is equal than the second one.
	 * 		or a positive integer if the first one is greater than the second one.
	 */

	@Override
	public abstract int compare(Node<Literal, InformationContext> arg0, Node<Literal, InformationContext> arg1);
}