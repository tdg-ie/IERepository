package coverageComputation;

import java.util.ArrayList;
import java.util.List;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;

import data.Literal;

import utilities.Utilities;

public class MDL {

	private double newRuleBits;
	private double availableBits;
	
	public MDL() {
		newRuleBits = 0;
	}
	
	/**
	 * intensional description length of a specific literal
	 * @param nRelations number of the relations in the knowledge base
	 * @param nTryArgs number of combinations with a predicate being studied
	 * @param negatedLitAllowed determines if the negated literals are allowed
	 * @return the number of bits consumed by the literal
	 */
	public double ldiLiteral(int nRelations, int nTryArgs, boolean negatedLitAllowed){
		return Utilities.round((negatedLitAllowed ? 1:0) + Utilities.log2(nRelations) + Utilities.log2(nTryArgs +1E-3));
	}
	
	/**
	 * intensional description length of a specific rule
	 * @param 
	 * @return the number of bits consumed by the rule
	 */
	public double ldiRule(Node<Literal, InformationContext> nodeRoot, List<Node<Literal, InformationContext>> nodeList){
		double result = 0;
		int detLitNumber = 0; 
		List<Node<Literal, InformationContext>> nodesAux = new ArrayList<Node<Literal,InformationContext>>();
		
		InformationContext icNode = null;
		double extraBits = 0;
		double currentRuleBits = 0;
		boolean someGainfulLiteral = false;
		
		for (Node<Literal, InformationContext> n : nodeList) {
			icNode = n.getInformationContext();
			if (icNode.isDeterminate())
				detLitNumber++;
			extraBits = icNode.getBits() - Utilities.log2(nodesAux.size() - detLitNumber + 1+1E-3);
			result = Utilities.max(0, extraBits);
			
			if (someGainfulLiteral){
				result += currentRuleBits;
			}
				
			nodesAux.add(n);
			
			if (!icNode.isDeterminate()) {
				currentRuleBits = result;
				someGainfulLiteral = true;
			}
		}
		result += nodeRoot.getInformationContext().getNewRuleBits();
		return Utilities.round(result);
	}
	
	/**
	 * intensional description length of a specific rule set
	 * @param rs rule set being studied
	 * @return the number of bits consumed by the rule set
	 */
	public double ldiRuleSet(List<List<Node<Literal,InformationContext>>> ruleSet){
		double result = 0;
		for (List<Node<Literal,InformationContext>> list : ruleSet) {
			result += ldiRule(list.get(0), list.subList(1, list.size())); 
		}
		
//		result -= Utilities.factorial(rs.size()); 
		return Utilities.round(result);
	}
	
	/**
	 * Determines if a specific literal can still be studied
	 * @param extraBits number of bits consumed if the literal is added to the rule
	 * @return true if the literal can still be studied, false otherwise
	 */
	public boolean exploreLiteral(double extraBits) {
		return extraBits < this.availableBits;
	}
	
	/**
	 * Checks if the number of consumed bits to explain the original positive bindings (or tuples)
	 * is minor that the number of consumed bits by the rule that explain these bindings
	 * @param number of original positive bindings
	 * @param allTuples number of original positive and negative tuples
	 * @return true if the literal must be pruned, false otherwise
	 */
	public boolean checkMDLPrune(int number,int allTuples) {
		return Utilities.encode(number,allTuples) <= this.newRuleBits;
	}
	
	public double getNewRuleBits() {
		return newRuleBits;
	}

	public void setNewRuleBits(double newRuleBits) {
		this.newRuleBits = Utilities.round(newRuleBits);
	}

	public double getAvailableBits() {
		return availableBits;
	}

	public void setAvailableBits(double availableBits) {
		this.availableBits = availableBits;
	}
	
//	public double ldeLiteral(int p){
//	int allTuples = KnowledgeBase.getOrigTuplesNumber();
//	double result = ((allTuples > 0) ? (1.1*(Utilities.log2(allTuples) + Utilities.comb(allTuples, p))) : 0.0);
//	return result;
//}
//

//si los bits para codificar el literal son superiores a los bits disponibles,
//no se estudia dicho literal
//public boolean exploreLiteral(int nRelations, int nTryArgs, boolean negatedLitAllowed) {
//	return (ldiLiteral(nTryArgs, nRelations, negatedLitAllowed) - Utilities.log2(d) < this.availableBits);
//}

}