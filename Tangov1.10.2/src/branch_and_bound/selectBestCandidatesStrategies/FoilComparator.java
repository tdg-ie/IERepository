package branch_and_bound.selectBestCandidatesStrategies;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import properties.PropertiesStore;
import utilities.FoilException;

import data.Literal;

public class FoilComparator extends BestCandidatesComparator {
	

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
		
		boolean literalL1Det = false, literalL2Det = false;
		double threshold = 0.0;
		try {
			threshold = Double.parseDouble(PropertiesStore.getProperty("threshold"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FoilException e) {
			e.printStackTrace();
		}
		double maxPossibleGain = maxGain * threshold;
		
		Literal l1 = arg0.getDataNode();
		Literal l2 = arg1.getDataNode();
		
		icl1 = arg0.getInformationContext();
		icl2 = arg1.getInformationContext();
		
		double informationGainL1 = 0;
		double informationGainL2 = 0;
		
		int l1NewVars = l1.getUnboundedVars().size();
		int l2NewVars = l2.getUnboundedVars().size();
		
		literalL1Det = icl1.isDeterminate();
		literalL2Det = icl2.isDeterminate();
		informationGainL1 = icl1.getGain();
		informationGainL2 = icl2.getGain();
		
		if ((informationGainL1 >= maxPossibleGain) || (informationGainL2 >= maxPossibleGain)){
			//which has a greater information gain
			if (informationGainL1 < informationGainL2)
				result = 1;
			else if (informationGainL1 == informationGainL2)
				result = selectPositiveLiteral(l1, l2);
			else
				result = -1;
		}
		else if (literalL1Det || literalL2Det) {
			//which is a determinate literal
			if (literalL1Det && literalL2Det) {
				//which has a greater information gain
				if (informationGainL1 < informationGainL2)
					result = 1;
				else if (informationGainL1 == informationGainL2)
					result = selectPositiveLiteral(l1, l2);
				else
					result = -1;
			}
			else if (literalL1Det && !literalL2Det)
				result = -1;
			else if (!literalL1Det && literalL2Det)
				result = 1;
		}
		
		else if (informationGainL1 > 0 || informationGainL2 > 0){
			//which has a greater information gain
			if (informationGainL1 < informationGainL2)
				result = 1;
			else if (informationGainL1 == informationGainL2)
				result = selectPositiveLiteral(l1, l2);
			else
				result = -1;
		}
		
		else if (l1NewVars > 0 || l2NewVars > 0) {
			//which has a greater number of new variables
			if (l1NewVars > l2NewVars)
				result = -1;
			else if (l1NewVars == l2NewVars)
				result = selectPositiveLiteral(l1, l2);
			else
				result = 1;
		}
		
		else {
			//which has a greater information gain
			if (informationGainL1 < informationGainL2)
				result = 1;
			else if (informationGainL1 == informationGainL2)
				result = 0;
			else
				result = -1;
		}
		return result;
	}

	private int selectPositiveLiteral(Literal l1, Literal l2) {
		int result = 0;
		if (l1.isPositive() && l2.isNegated())
			result = -1;
		else if (l1.isNegated() && l2.isPositive())
			result = 1;
		return result;
	}
}