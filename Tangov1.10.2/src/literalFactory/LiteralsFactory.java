package literalFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import literalsFactoryStrategies.IPredicateSortStrategies;

import combinatorial.ICombinatorial;
import constantManagement.VarPair;

import data.Constant;
import data.Literal;
import data.LiteralPair;
import data.Term;

public abstract class LiteralsFactory {
	
	protected IPredicateSortStrategies strategy = null;
	protected ICombinatorial comb_perm_Generator = null;
	protected List<Term> variableList = null;
	protected Map<String, List<Constant>> constantSet = null;
	protected List<VarPair> orderedVars = null;
	protected List<Term> newGeneratedVars = null;
	
	public LiteralsFactory(IPredicateSortStrategies strategy){
		this.strategy = strategy;
	}
		
	public abstract List<LiteralPair> getNext();
	public abstract boolean hasNext();
	public abstract int getPossiblePredNumber();
	public abstract int getPossibleCombNumber(Literal literal);
	public abstract void decPossibleCombNumber(String pName);
	
//	public abstract boolean isEstablishedOrder();
//	public abstract void setEstablishedOrder(boolean establishedOrder);
	
	public List<Term> getVariableList() {
		return this.variableList;
	}

	public void setVariableList(List<Term> variableList) {
		this.variableList = variableList;
	}

	public Map<String, List<Constant>> getConstantSet() {
		return this.constantSet;
	}
	
	public void setConstantSet(Map<String, List<Constant>> constantSet) {
		this.constantSet = constantSet;
	}
	
	public void setOrderedVars(List<VarPair> orderedVars) {
		this.orderedVars = orderedVars;
	}
	
	public List<Term> getNewGeneratedVars() {
		return newGeneratedVars;
	}
	
	public void setNewGeneratedVars(List<Term> newGeneratedVars) {
		this.newGeneratedVars = newGeneratedVars;
	}

	public void resetNewVars() {
		newGeneratedVars = new ArrayList<Term>();
	}
	
	public abstract void setConstantOrderDirection(int constantOrderDirection);
}
