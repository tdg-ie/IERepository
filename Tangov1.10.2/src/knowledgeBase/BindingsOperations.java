package knowledgeBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Constant;
import data.Literal;
import data.LiteralPair;
import data.Term;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

public abstract class BindingsOperations {
	
	
	/**
	 * 				STATIC METHODS
	 */
	
	/**
	 * Checks if a bindings is covered by another bindings 
	 * @param b1 bindings to compare
	 * @param b2 bindings to compare
	 * @return true if bindings b2 is equal to the binding b1 or an extension of this
	 */
	public static boolean covered(Bindings b1, Bindings b2){
		boolean result = true;
		
		Map<Variable, Constant> bindings = null;
		Iterator<Entry<Variable, Constant>> it = null;
		Entry<Variable, Constant> mapEntry = null;
		
		Variable v = null;
		Constant c = null;
		
		bindings = b1.getBindings();
		
		//iterating for each binding in b1.
		it = bindings.entrySet().iterator();
		while (it.hasNext() && result) {
			mapEntry = it.next();
			v = mapEntry.getKey();
			c = mapEntry.getValue();
			
			if (!b2.containsVariable(v)) {
				result = false;
			}
			else if (!b2.getConstant(v).equals(c))
					result = false;
		}
		
		return result;
	}
	
	/**
	 * Checks if bindings b belongs to a specific set of bindings
	 * @param bindingsSet set of bindings where the bindings b is searched for
	 * @return true, if bindings belong to bindingsSet, false otherwise
	 */

	public static boolean belongs(Bindings b, Set<Bindings> bindingSet){
		boolean result = false;
		
		Iterator<Bindings> it = bindingSet.iterator();
		while (it.hasNext() && !result) {
			Bindings bindings = it.next();
			if (BindingsOperations.covered(b, bindings))
				result = true;
		}
	
		return result;
	}
	
	/**
	 * Gets the set of Bindings, removing all the terms that are not in terms 
	 * @param currentBinding is the extended binding, it can contain new vars or not
	 * @param vars single set of variables that the currentBinding must contain 
	 * @return the equivalent binding that only contains the vars requested
	 */
	public static Bindings getOrigBinding(Bindings currentBinding, List<Term> terms){
		Bindings result = new Bindings();
		for (Term t : terms)
			if (t instanceof Variable) {
				Variable var = (Variable) t;
				try {
				 result.addBinding(var, currentBinding.getConstant(var));
				}
				catch(Exception e) {
					System.out.println(currentBinding.toString());
					for (Term term : terms) {
						System.out.println(term.getName());
					}
				}
			}
		return result;
	}
	
	/**
	 * Gets a set of Bindings, removing all vars that are no contained in terms 
	 * @param currentBinding is the extended binding, it can contain new vars or not
	 * @param vars single set of variables that the currentBinding must contain 
	 * @return the equivalent binding that only contains the vars requested
	 */
	public static Set<Bindings> getReducedBindingSet(Set<Bindings> currentSet, List<Term> terms){
		Set<Bindings> result = new HashSet<Bindings>();
		List<Variable> searchedTerms = new ArrayList<Variable>();
		for (Term t : terms)
			if (t instanceof Variable) {
				Variable var = (Variable) t;
				searchedTerms.add(var);
			}
		
		for (Bindings b : currentSet) {
			Bindings newBindings = new Bindings();
			for (Variable t : searchedTerms)
				newBindings.addBinding(t, b.getConstant(t));
			result.add(newBindings);
		}
		return result;
	}
	
	/**
	 * Bueno
	 * @param 
	 * @return 
	 */

//	public static Bindings getThisOrigBinding(Bindings origBinding, Set<Bindings> bindingsSet){
//		Bindings result = null;
//		boolean end = false;
//		Iterator<Bindings> it = bindingsSet.iterator();
//		while (it.hasNext() && !end){
//			Bindings b = it.next();
//			if (b.equals(origBinding)) {
//				result = b;
//				end = true;
//			}
//		}
//		
//		return result;
//	}
	/**
	 * Gets a binding that is covered by the newBinding
	 * @param newBinding: binding to search for in the set of oldBindings
	 * @param originalBindings: set of bindings that have at most the same number of variable that the newBinding
	 * @return the original bindings that are covered by the new binding
	 */
	public static Bindings getOriginalCoveredBindings(Bindings newBinding, 
			Set<Bindings> originalBindings) {
		
		Bindings result = null;
		Boolean end = false;
		Bindings origBinding = null;
		Iterator<Bindings> it = null;
		
		it = originalBindings.iterator();
		while (it.hasNext() && !end) {
			origBinding = it.next();
			if (BindingsOperations.covered(origBinding, newBinding)) {
				result = origBinding.clone();
				end = true;
			}
		}
		return result;
	}
	
	/**
	 * Remove the bindings b from the set of bindings bindingSet
	 * @param b bindings to remove from the bindingSet
	 * @param bindingSet set of bindings where bindings b is searched for
	 * @return the new set of bindings without including bindings b.
	 */
	public static Set<Bindings> removeBindings(Bindings b, 
			Set<Bindings> bindingSet) {
		
		Set<Bindings> result = new HashSet<Bindings>();
		Bindings bindings = null;
		Iterator<Bindings> it = null;
		
		it = bindingSet.iterator();
		while (it.hasNext()) {
			bindings = it.next();
			if (!bindings.equals(b))
				result.add(bindings.clone());
		}
		return result;
	}
	
	/**
	 * Gets the set of bindings that are covered by a new set of bindings. Bindings belonging to oldBindingsSet contain at most
	 * the same variables that newBindingsSet. It is usually that bindings in newBindingsSet contains greater number of variables.
	 * @param newBindingsSet set of new bindings
	 * @param oldBindingsSet set of old bindings
	 * @return set of old bindings that are covered by newBindingsSet
	 */
	
	// este método es parecido al de origmatch con la diferencia de que este sólo comprueba un conjunto de bindings
	// y el otro la cobertura entera, es decir, positive and negative bindings.
	public static Set<Bindings> getOriginalBindingsSetCovered(Set<Bindings> newBindingsSet, Set<Bindings> oldBindingsSet) {
		Set<Bindings> result = new HashSet<Bindings>();
		
		Bindings newBinding = null, oldBinding = null, b = null;
		Iterator<Bindings> it1 = null;
		Iterator<Bindings> it2 = null;
		boolean end = false;
		
		if (newBindingsSet.size() < oldBindingsSet.size()) {
			//iterating over the set of new Bindings
			it1 = newBindingsSet.iterator();
			//checking what original bindings are covered by new bindings
			while (it1.hasNext()) {
				newBinding = it1.next();
				b = getOriginalCoveredBindings(newBinding, oldBindingsSet);
				if (!belongs(b, result))
					result.add(b);
			}
		}
		else {
			//iterating over the set of old Bindings
			it1 = oldBindingsSet.iterator();
			//checking whether each old binding is covered by the set of new bindings
			while (it1.hasNext()) {
				oldBinding = it1.next();
				end = false;
				it2 = newBindingsSet.iterator();
				while (it2.hasNext() && !end) {
					newBinding = it2.next();
					if (BindingsOperations.covered(oldBinding, newBinding)) {
						result.add(oldBinding.clone());
						end = true;
					}
				}
			}
		}
		return result;		
	}
	
	/**
	 * Gets the intersection between both set of bindings. The first set of bindings has usually greater number of variables. Otherwise,
	 * the resulting bindingSet will have the same number of variables than the bindings belonging to the bindingSet1 and could have 
	 * repeated bindings.
	 * @param bindingsSet1 first set of bindings to perform the intersection
	 * @param bindingsSet2 second set of bindings to perform the intersection
	 * @return a set of bindings resulting bindingsSet1 intersection bindingsSet2
	 */
	public static Set<Bindings> matchBindings(Set<Bindings> bindingsSet1, Set<Bindings> bindingsSet2) {
		Set<Bindings> result = new HashSet<Bindings>();
		
		boolean end = false;
		Bindings binding = null, binding1 = null, binding2 = null;
		Iterator<Bindings> it1 = null;
		Iterator<Bindings> it2 = null;
		
		it1 = bindingsSet1.iterator();
		//checks what bindings of the set 1 are covered by the set 2
		while (it1.hasNext()) {
			end = false;
			binding1 = it1.next();
			it2 = bindingsSet2.iterator();
			while (it2.hasNext() && !end) {
				binding2 = it2.next();
				if (BindingsOperations.covered(binding2, binding1)) {
					binding = binding2.clone();
					Iterator<Variable> varsIterator = binding1.getBindings().keySet().iterator();
					while (varsIterator.hasNext()) {
						Variable v = varsIterator.next();
						 if (!binding.containsVariable(v))
							 binding.addBinding(v, binding1.getConstant(v));
					}
					result.add(binding);
					end = true;
				}
			}
		}
		return result;		
	}
	
	/**
	 * Gets the set of Bindings covered by a new coverage which can include
	 * new variables in the set of bindings
	 * @param origcr
	 * @param newcr
	 * @return a coverage with the set of bindings belonging
	 */
	public static Coverage origmatch(Coverage origcr, Coverage newcr) {	
			Coverage result = new Coverage();
			Bindings b = null;
			Set<Bindings> origPosBindings = origcr.getPositiveBindings();
			Set<Bindings> origNegBindings = origcr.getNegativeBindings();
			Set<Bindings> newPosBindings = newcr.getPositiveBindings();
			Set<Bindings> newNegBindings = newcr.getNegativeBindings();
			Set<Bindings> posBindingsCovered = new HashSet<Bindings>();
			Set<Bindings> negBindingsCovered = new HashSet<Bindings>();
			
			
			for (Bindings currentBinding : newPosBindings) {
				b = BindingsOperations.getOriginalCoveredBindings(currentBinding, origPosBindings);
				if (!BindingsOperations.belongs(b, posBindingsCovered))
					posBindingsCovered.add(b);
			}
			
			for (Bindings currentBinding : newNegBindings) {
				b = BindingsOperations.getOriginalCoveredBindings(currentBinding, origNegBindings);
				if (!BindingsOperations.belongs(b,negBindingsCovered))
					negBindingsCovered.add(b);
			}
			
			result.setPositiveBindings(posBindingsCovered);
			result.setNegativeBindings(negBindingsCovered);
			
			return result;
	}
	
	/**
	 * Gets the number of bindings belonging to bindingsSet1 that are covered by the other set of bindings bindingsSet2 
	 * @param bindingsSet1 first set of bindings (this has to be the largest set of bindings).
	 * @param bindingsSet2 second set of bindings
	 * @return the number of bindings covered
	 */
	
	public static int getNumberOfCoveredBinding(Set<Bindings> bindingsSet1,
			Set<Bindings> bindingsSet2) {
		int result = 0;
		
		for (Bindings bindings1 : bindingsSet1)
			if (belongs(bindings1, bindingsSet2))
				result++;
		
		return result;
	}
	
	
	/**
	 * 				ABSTRACT METHODS
	 */
	
	/**
	 * Gets the number of expanded bindings belonging to the literal that cover the currentBinding
	 * @param currentBinding bindings with the values to make the query (bindings to cover)
	 * @param literal literal to check if covers the currentBindings
	 * @return the number of Bindings that are covered by the literal
	 */
	public abstract int nMatches(Bindings b, Literal l);
	
	public abstract int nMatches(Bindings b, Node<Literal,InformationContext> root, List<Literal> literalList);
	
	public abstract int nMatches(Set<Bindings> bindingSet, Node<Literal,InformationContext> root, List<Literal> literalList);
	
	public abstract Coverage origmatch(Coverage cr, Literal l);
	
	public abstract Coverage origmatch(Coverage cr, Node<Literal,InformationContext> root, List<Literal> literalList);
	
	public abstract Coverage branchOrigMatch(Coverage origCoverage, Literal root,List<Literal> literalList);
	
	public abstract Coverage match(Coverage cr, Literal l);
	
	public abstract Set<Bindings> match(Bindings b, Literal l);
	
	public abstract Coverage match(Coverage cr, Literal root, List<Literal> literalList);

	public abstract Set<Bindings> match(Bindings b, Literal root, List<Literal> literalList);

	public abstract boolean possibleLiteral(LiteralPair lp, Bindings b);
	
	/**
	 * Check if r is essential, i.e., if its coverage is not satisfied by any other rule in the list
	 * @param r rule to check if it is essential
	 * @param rules list of rules where we check if there is one that also satisfied the coverage of r
	 * @return
	 */
	public abstract boolean essential(Set<Bindings> posBindings, Set<Bindings> restOfPosBindings);
}
