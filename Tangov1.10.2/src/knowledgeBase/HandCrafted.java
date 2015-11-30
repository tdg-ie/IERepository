package knowledgeBase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;

import data.Literal;
import data.LiteralPair;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

public class HandCrafted extends BindingsOperations {

	private KnowledgeBaseOperations kBase = null;
	
	public HandCrafted(KnowledgeBaseOperations kbop) {
		this.kBase = kbop;
	}

	@Override
	public Set<Bindings> match(Bindings currentBinding, Literal literal) { //enlargingBinding

		Set<Bindings> result = new HashSet<Bindings>();
		Set<Bindings> predicateInstances = null;
//		if (literal.isNegated())
//			predicateInstances = kBase.getNegTuples(literal.getPredicate().getName());
//		else 
//			predicateInstances = kBase.getPosTuples(literal.getPredicate().getName());
		
		Bindings newBinding = null;
		
		//Get first binding of predicateInstances
		Bindings predicateBinding = null;
		if (predicateInstances.iterator().hasNext())
			predicateBinding = predicateInstances.iterator().next();
		
		if (predicateBinding != null) {
			// Search the existing variables
			// and generate the subset of variables with assigned value
			Bindings subBindingSet = new Bindings();
			for (Variable v: currentBinding.getBindings().keySet()) {
				if (predicateBinding.containsVariable(v))
					subBindingSet.addBinding(v, currentBinding.getConstant(v));
			}
			
			// Search the new Variables
			Set<Variable> newVariables = new HashSet<Variable>();
			for (Variable v: predicateBinding.getBindings().keySet()) {
				if (!currentBinding.containsVariable(v)){
					newVariables.add(v);
				}	
			}
		
			
			for (Bindings b: predicateInstances) {
				if (b.containsBindings(subBindingSet.getBindings())) {
						newBinding = subBindingSet.clone();
						
						//add binding for each new Variable
						for(Variable v: newVariables)
							newBinding.addBinding(v, b.getConstant(v));
						
						result.add(newBinding);
				}
			}
		}
		
		return result;
	}

	@Override
	public int nMatches(Bindings currentBinding, Literal literal) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Coverage origmatch(Coverage cr, Literal l) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Coverage match(Coverage cr, Literal l) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean possibleLiteral(LiteralPair lp, Bindings b) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Coverage match(Coverage cr, Literal root, List<Literal> literalList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Bindings> match(Bindings b, Literal root,
			List<Literal> literalList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean essential(Set<Bindings> posBindings,
			Set<Bindings> restOfPosBindings) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int nMatches(Bindings b, Node<Literal, InformationContext> root,
			List<Literal> literalList) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int nMatches(Set<Bindings> bindingSet,
			Node<Literal, InformationContext> root, List<Literal> literalList) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Coverage origmatch(Coverage cr,
			Node<Literal, InformationContext> root, List<Literal> literalList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Coverage branchOrigMatch(Coverage origCoverage, Literal root,
			List<Literal> literalList) {
		// TODO Auto-generated method stub
		return null;
	}

}