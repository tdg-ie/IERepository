package knowledgeBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import properties.PropertiesStore;
import utilities.FoilException;

import combinatorial.PermutationsOfConstantWithRepetition;

import data.Constant;
import data.Term;
import data.Type;
import data.Variable;
import dataCoverage.Bindings;

public class ClosedWorldAssumption
{
	/**
	 * Generates all possible combinations of negative bindings
	 * @param predicate
	 * @param constantSet list of constants that appears in positive bindings
	 * @param positiveBindings set of positive bindings belonging to the predicate 
	 * @param list variable list belonging to the bindings of the predicate indicated as parameter
	 * @return a bindings set with negative bindings
	 */
	public static Set<Bindings> generateNegativeExamples (PrologTransferObject pTO)
	{	//Genera N permutaciones para un determinado predicado
		Set<Bindings> result = new HashSet<Bindings>();
		Map<String, Integer> constantSetsize = null;
		Iterator<Entry<String, List<Constant>>> it = null;
		PermutationsOfConstantWithRepetition<List<Constant>> perm = null;
		int combNumber = 0;
		
		Map<String, List<Constant>> constantSet = pTO.getConstantSet();
		Set<Bindings> positiveBindings = pTO.getPositiveBindings();
		List<Type> types = pTO.getPredicate().getArgsType();
		List<Term> list = pTO.getVariables();
		
		constantSetsize = new HashMap<String, Integer>();
		it = constantSet.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Constant>> entry = it.next();
			constantSetsize.put(entry.getKey(), entry.getValue().size());
		}
		perm = new PermutationsOfConstantWithRepetition<List<Constant>>(types, constantSetsize);
		
		int max = perm.count() - positiveBindings.size();
		float percentaje = 1;
		try {
			percentaje = Float.parseFloat(PropertiesStore.getProperty("percentajeNegativeExamples"));
			max = Math.round(max * percentaje);
		} catch (FoilException e) {
			e.printStackTrace();
		}
		while (perm.hasNext()  && combNumber < max) {
			int[] index = perm.next();
			Bindings b = new Bindings();
			for (int i = 0; i < index.length; i++) {
				Constant c = constantSet.get(types.get(i).getTypeName()).get(index[i]);
				b.addBinding((Variable) list.get(i), c);
			}
			if (!BindingsOperations.belongs(b,positiveBindings)) {
				result.add(b);
				combNumber++;
			}
		}
		
		return result;
	}
}

//
///**
// * 
// * @param predicate
// * @param possibleConstants
// * @param variables
// * @return
// */
//private static Set<Bindings> generateAllPossibleCombinations (Predicate predicate, 
//		List<Constant> possibleConstants, List<Variable> variables)
//{	
//	//Genera todas las permutaciones para un predicado	
//	List<Type> typeList = predicate.getArgsType();
//	Set<Bindings> result = new HashSet<Bindings>();
//	List<Constant> args = null; 
//	
//	PermutationGeneration<Constant> perm = null;
//	perm = new PermutationsWithRepetition<Constant>(predicate.getArity(), possibleConstants.size());
//	
//	List<List<Constant>> resultList = new ArrayList<List<Constant>>();
//	resultList.addAll(perm.getAllPermutations(possibleConstants));
//	
//	boolean end;
//	for (int j = 0; j < resultList.size(); j++) {
//		end = false;
//		args = new ArrayList<Constant>();
//		//check if argument type has the same type than the constant generated
//		for (int i = 0; i < resultList.get(j).size(); i++) {
//			Constant c = (Constant) resultList.get(j).get(i);
//			if (!typeList.get(i).equals(c.getType()))
//				end = true;
//			if (!end) 
//				args.add(c);
//		}
//		if (!end) {
//			Bindings b = new Bindings();
//			for (int i = 0; i < args.size(); i++)
//				b.addBinding((Variable) variables.get(i), args.get(i));
////			b.printBindings();
//			result.add(b);
//		}
//	}
//	
//	return result;
//}
//}