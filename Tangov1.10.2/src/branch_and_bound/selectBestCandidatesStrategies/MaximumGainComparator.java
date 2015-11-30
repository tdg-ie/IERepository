package branch_and_bound.selectBestCandidatesStrategies;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;

public class MaximumGainComparator extends BestCandidatesComparator {
	

	/**
	 * Compares two arguments to order them. 
	 * Returns 
	 * 		a negative integer if the first argument is less than the second one, 
	 * 		zero, if the first one is equal than the second one.
	 * 		or a positive integer if the first one is greater than the second one.
	 */

	@Override
	public int compare(Node<Literal, InformationContext> arg0, Node<Literal, InformationContext> arg1) {
		int result = 0;
		InformationContext icl1 = null;
		InformationContext icl2 = null;
		int hashCode1, hashCode2;
		
		icl1 = arg0.getInformationContext();
		icl2 = arg1.getInformationContext();
		
		double informationGainL1 = icl1.getGain();
		double informationGainL2 = icl2.getGain();
		
		if (informationGainL1 < informationGainL2)
			result = 1;
		else if (informationGainL1 == informationGainL2) {
			hashCode1 = arg0.hashCode();
			hashCode2 = arg1.hashCode();
			if (hashCode1 < hashCode2)
				result = 1;
			else 
				result = -1;
		}
		else
			result = -1;
				
		return result;
	}
}