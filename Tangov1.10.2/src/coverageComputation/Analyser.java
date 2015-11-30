package coverageComputation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import knowledgeBase.KnowledgeBaseFactory;
import knowledgeBase.KnowledgeBaseOperations;

import data.Constant;
import data.Literal;
import data.Sign;
import data.Term;
import data.Type;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;
import dataCoverage.CoveragePair;
import heuristics.ConfusionMatrixList;
import heuristics.Heuristic;

public abstract class Analyser {
	protected Heuristic heuristic = null;
	protected ConfusionMatrixList confusionMatrixListL = null;
	protected ConfusionMatrixList confusionMatrixListNL = null;
	
	/**
	 * Analyse a specific node pair updating all data related with the coverage like
	 * positive and negative covered bindings, used bits, and determines if the node pair
	 * have to be pruned
	 * @param aito object with input data like node pair, rule in production, number of relations etc.
	 * @return object with the updated node pair
	 */
	public abstract AnalyseOutputTransferObject analyse(AnalyseInputTransferObject aito);
	/**
	 * Analyse positive bindings set that are covered by specific node pair
	 * @param apeito object with input data (node pair, rule in production, thresholds, etc)
	 * @return object with the updated output data
	 */
	protected abstract AnalysePositiveExamplesOutputTransferObject analysePositiveExamples(AnalysePositiveExamplesInputTransferObject apeito);
	/**
	 * Analyse negative bindings set that are covered by specific node pair
	 * @param aneito object with input data (node pair, rule in production, thresholds, etc)
	 * @return object with the updated output data
	 */
	protected abstract AnalyseOutputTransferObject analyseNegativeExamples(AnalyseNegativeExamplesInputTransferObject aneito);
	
	/**
	 * Analyse a coverage to check the set of bindings that are covered by a specific literal
	 * @param cr coverage
	 * @param l literal
	 * @return the set of bindings (positives and negatives) covered by the literal
	 */
	public abstract CoveragePair updateCoverage(Coverage ocr, Coverage cr, Literal l);
	
	
	/**
	 * Gets all existing variables that are compatible (same type) with 
	 * the new variables included in the argument list of the Literal
	 * @param existingVars existing variables contained in the current rule
	 * @param literal literal where looking for the new variables that are compatible with existing variables
	 * @return a map with the new variables as keys and a map with all existing compatible variables
	 */
	protected Map<Term, Map<Term, Boolean>> CheckPossibleDuplicateVars(Set<Term> existingVars, Literal literal) {
		
		Map<Term, Map<Term, Boolean>> result = new HashMap<Term, Map<Term,Boolean>>();
		Map<Term, Boolean> relatedVars = null;
		
		List<Type> typeList = literal.getPredicate().getArgsType();
		Iterator<Term> it = null;
		List<Term> args = literal.getArgs();
		
		for (int i = 0; i < args.size(); i++) {
			Term t1 = args.get(i);
			if (!t1.isBound()) {
				relatedVars = new HashMap<Term, Boolean>();
				Type type = typeList.get(i);
				it = existingVars.iterator();
				while (it.hasNext()) {
					Term t2 = it.next();
					if (t2 instanceof Variable && t2.getType().equals(type))// && (t2.belongs(args) == -1))
						relatedVars.put(t2, false);
				}
				
				if (!relatedVars.isEmpty())
					result.put(t1, relatedVars);
			}
		}
		
//		String s = "";
//		for (Term t: result.keySet()) {
//			s = "<" + t.getName() + " -- ";
//			for (Term term : result.get(t).keySet()) {
//				s += term.getName() + ",";
//			}
//			s = s.substring(0, s.lastIndexOf(","));
//			s += ">";
//			System.out.println(s);
//		}
		
		return result;
	}
	
	// aquí tenemos que controlar si estamos comparando la variable con otra variable o con 
	// una constante
	protected Map<Term, Map<Term, Boolean>> CheckNewVars(Map<Term, Map<Term, Boolean>> relatedVarsMap, Set<Bindings> bindings, Sign sign) {
		Map<Term, Map<Term, Boolean>> result = new HashMap<Term, Map<Term,Boolean>>();
		Map<Term, Boolean> relatedVars = null;
		Map<Term, Boolean> updatedRelatedVars = null;
		boolean end = false;
		Constant c = null;
		
		Iterator<Bindings> bindingsIterator = null;
		
		for (Term t1 : relatedVarsMap.keySet()) {
			relatedVars = relatedVarsMap.get(t1);
			updatedRelatedVars = new HashMap<Term, Boolean>();
			for (Term t2: relatedVars.keySet()) {
				if (!relatedVarsMap.get(t1).get(t2)) {
//				if (!sign.equals(Sign.POSITIVE) || !relatedVarsMap.get(t1).get(t2)) {
					end = false;
					bindingsIterator = bindings.iterator();
					while (bindingsIterator.hasNext() && !end) {
						Map<Variable, Constant> map = bindingsIterator.next().getBindings();
						if (map.containsKey(t1)) {
							c = map.get(t1);
							
							if (t2 instanceof Constant && t2.getValue().equals(c.getValue()))
								updatedRelatedVars.put(t2, relatedVars.get(t2));
							else if (map.containsKey(t2) && map.get(t2).equals(c))
								updatedRelatedVars.put(t2, relatedVars.get(t2));
							else {
								if (sign.equals(Sign.NEGATIVE)) {
									//can be different in negative bindings
									updatedRelatedVars.put(t2, true);
								}
								else if (updatedRelatedVars.containsKey(t2))
									updatedRelatedVars.remove(t2);
								
								end = true;
							}
						}
					}
				}
				else if (t2 instanceof Variable) 
					updatedRelatedVars.put(t2, true);
			}
			if (!updatedRelatedVars.isEmpty())
				result.put(t1, updatedRelatedVars);
		}
		
//		String s = "";
//		for (Term t: result.keySet()) {
//			s = "<" + t.getName() + " -- ";
//			for (Term term : result.get(t).keySet()) {
//				s += term.getName() + ",";
//			}
//			s = s.substring(0, s.lastIndexOf(","));
//			s += ">";
//			System.out.println(s);
//		}
		
		return result;
	}
	
	protected Map<Term, Map<Term, Boolean>> CheckPossibleEqualityVC(Map<Term, Map<Term,Boolean>> duplicateVars, Literal literal) {
		
		KnowledgeBaseFactory kBase = KnowledgeBaseFactory.getInstance();
		KnowledgeBaseOperations kbo = kBase.getKnowledgeBaseOperations();
		
		Map<Term, Map<Term, Boolean>> result = new HashMap<Term, Map<Term,Boolean>>(duplicateVars);
		Map<String, List<Constant>> constantSet = kbo.getFullConstantSet();
		Map<Term, Boolean> relatedVarConstantMap = null;
		
		List<Type> typeList = literal.getPredicate().getArgsType();
		List<Term> args = literal.getArgs();
		
		
		for (int i = 0; i < args.size(); i++) {
			Term t = args.get(i);
			if (!t.isBound()) {
				String typeName = typeList.get(i).getTypeName();
				relatedVarConstantMap = (duplicateVars.containsKey(t) ? duplicateVars.get(t): new HashMap<Term, Boolean>());
				if (constantSet.containsKey(typeName))
					for (Term c : constantSet.get(typeName)) {
						relatedVarConstantMap.put(c, false);
					}
				result.put(t, relatedVarConstantMap);
			}
		}	
		
//		String s = "";
//		for (Term t: result.keySet()) {
//			s = "<" + t.getName() + " -- ";
//			for (Term term : result.get(t).keySet()) {
//				s += term instanceof Variable ? term.getName() : term.getValue() + ",";
//			}
//			s = s.substring(0, s.lastIndexOf(","));
//			s += ">";
//			System.out.println(s);
//		}
		
		return result;
	}
}