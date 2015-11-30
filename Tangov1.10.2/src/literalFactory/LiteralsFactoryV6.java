package literalFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utilities.Numbers;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;


import literalsFactoryStrategies.IPredicateSortStrategies;

import combinatorial.CombinationsWithoutRepetition;
import combinatorial.PermutationsOfVariablesWithoutRepetition;
import constantManagement.VarPair;

import data.Constant;
import data.Literal;
import data.LiteralPair;
import data.Predicate;
import data.Sign;
import data.Term;
import data.Variable;

public class LiteralsFactoryV6 extends LiteralsFactory {
	
	private int predicateSetIndex = 0;
	private int iterationIndex = 0;
	
	private int existingVarIndex = 0;
	private int cluster = 0;
	private boolean configured = false;
	private Predicate previousPredicate = null;
	private List<Term> allTerms = null;
	private List<Term> newVariables = null;
	
	private List<Predicate> predicateSet = null;
	private Map<String, Integer> possibleRel = null;
	private List<Term> rootArgs = null;
	
	private List<Node<Literal, InformationContext>> bodyRule = null;
	private Set<Term> varsInPredefefinedLit = null;
	private int constantOrderDirection;

	public LiteralsFactoryV6(IPredicateSortStrategies strategy, 
			List<Predicate> predicateSet, int cluster, List<Node<Literal, InformationContext>> bodyRule, int constantOrderDirection) {
		this(strategy, predicateSet);
		this.cluster = cluster;
		this.bodyRule = bodyRule;
		this.varsInPredefefinedLit = this.searchForVarsInPredefinedPred();
		this.constantOrderDirection = constantOrderDirection;
	}
	
	private LiteralsFactoryV6(IPredicateSortStrategies strategy, List<Predicate> predicateSet) {
		super(strategy);
		this.cluster = 0;
		this.predicateSet = this.strategy.sortPredicates(predicateSet);
		this.allTerms = new ArrayList<Term>();
		this.possibleRel = new HashMap<String, Integer>();
	}

	@Override
	public List<LiteralPair> getNext() {
		List<LiteralPair> result = new ArrayList<LiteralPair>();
		int nPossibleLiterals = 0;
		LiteralPair literalPair = null;

		int predicateSetSize = predicateSet.size();
		Predicate p = null;
		int end = iterationIndex + cluster;
		boolean cond = true;
		int[] indexArray;
		int predicateArity = 0;
		String pName = "";
		
//		System.out.println("FABRICANDO LITERALES");
		
		if (predicateSetIndex == 0) {
			List<Term> varToRemove = new ArrayList<Term>();
			//eliminamos las variables que ya tienen tipo
			for (Term term : newGeneratedVars) {
				if (term.isBound())
					varToRemove.add(term);
			}
			for (Term term : varToRemove) {
				newGeneratedVars.remove(term);
			}
		}
		
		for (; predicateSetIndex < predicateSetSize; predicateSetIndex++) {
			p = predicateSet.get(predicateSetIndex);
			predicateArity = p.getArity();
			pName = p.getName();
//			List<Type> types = p.getArgsType();
			if (!(p == previousPredicate)) { 
				configured = false; 
				existingVarIndex = 0;
				comb_perm_Generator = null;
			}
			if (p.isPredefined()){
				
				while (existingVarIndex <= variableList.size()) {
					if (!configured && (existingVarIndex == variableList.size()))  { 
						allTerms.clear();
						allTerms.addAll(variableList);
						if (allTerms.size() >= predicateArity){
							comb_perm_Generator = new CombinationsWithoutRepetition(predicateArity, allTerms.size());
//							System.out.println("VV");
						}
						configured = true;
						pName = p.getName() + "VV";
						nPossibleLiterals = 0;
					}
					
					//if (existingVarIndex < variableList.size()) { --> else??
					int dif = variableList.size() - existingVarIndex;
					if (dif > 0) {
						allTerms.clear();
						String type = variableList.get(existingVarIndex).getType().getTypeName();
						allTerms.add(variableList.get(existingVarIndex));
						
						if (constantSet.containsKey(type)) {
							if (!p.getName().equals("EQUAL")) {
								if (Numbers.isNumber(constantSet.get(type).get(0).getValue()))
									allTerms.addAll(constantSet.get(type));
							}
							else
								allTerms.addAll(constantSet.get(type));
							
							if (!configured && allTerms.size()>= predicateArity){
								comb_perm_Generator = new CombinationsWithoutRepetition(predicateArity, allTerms.size());
//								System.out.println("VC");
							}
						}
						pName = p.getName() + "VC";
					}
					
					existingVarIndex++;
					
					cond = (comb_perm_Generator != null) && comb_perm_Generator.hasNext();
					
					while (cond) {
						List<Term> terms = new ArrayList<Term>();
						indexArray = comb_perm_Generator.next ();
						for (int i = 0; i < indexArray.length; i++) {
							terms.add(allTerms.get(indexArray[i]));
						}
						
						Literal l = new Literal();
						Literal nl = new Literal();
						
						
						if (l.makeLiteral(p, terms, Sign.POSITIVE) && (nl.makeLiteral(p, terms, Sign.NEGATIVE)) && this.repeated(l)) {

							literalPair = new LiteralPair();
							// Not negated literal
							literalPair.setLiteral(l);
							// Negated literal
							literalPair.setNegatedLiteral(nl);
//							l.print();
//							System.out.println();
//							nl.print();
//							System.out.println();
							
							//add literal pair to the list
							result.add(literalPair);
							if (possibleRel.containsKey(pName))
								nPossibleLiterals = possibleRel.get(pName);
							else nPossibleLiterals = 0;
							nPossibleLiterals++;
							possibleRel.put(pName, nPossibleLiterals);
							if (cluster != 0) cond = (++iterationIndex < end);
						}
						cond &= comb_perm_Generator.hasNext();
					}
					if (!cond && !((iterationIndex < end) && (predicateSetIndex < predicateSetSize))) {
						previousPredicate = p;
						iterationIndex = 0;
						if (!result.isEmpty() && (cluster != 0))
							return result;
					}
				}
			}
			if (p.isSupport() || (p.isTarget())) {
				
				/**
				 * First literal will be one which has more new variables and the first argument is a existing var
				 * for instance, if the predicate has arity equal to 4:
				 * 		firstLiteral(X0, NewV0, NewV1, NewV2) or
				 * 		firstLiteral(X0, X1, NewV0, NewV1) or
				 * 		firstLiteral(X0, X1, X2, NewV0) and so on.
				 * 
				 * the last literal will be one which have more existing variables
				 * for instance, if the predicate has arity equal to 4:
				 * 		lastLiteral(X0, X1, X2, X3) or
				 *  	lastLiteral(X1, X2, X0, X3) or
				 *   	lastLiteral(X3, X1, X2, X3) and so on
				 */
				
				if (!configured) {
					newVariables = this.createNewVariables(predicateArity - 1);
					configured = true;
					
					
					// Checks if there is any non recursive literal
					boolean existsVar = true;
					if (p.isTarget()) {
						existsVar = false;
						for (int i = 0; i < variableList.size() && !existsVar; i++) {
							Term v = variableList.get(i);
							if (this.searchVarInOrderedVars(v)) {
								existsVar = true; //can exist any combination which the literal is not recursive
							}
						}
					}
					
					cond = existsVar;
					if (cond)
						comb_perm_Generator = new PermutationsOfVariablesWithoutRepetition(variableList, newVariables, p.getArgsType());
//					comb_perm_Generator = new PermutationsWithRepetition(p.getArity(), variableList.size() + newVariables.size());
				}
				
				List<Term> mix = new ArrayList<Term>();
				mix.addAll(this.variableList);
				mix.addAll(this.newVariables);
				
				while (cond && comb_perm_Generator.hasNext()) {
//					
					List<Term> terms = new ArrayList<Term>();
					indexArray = comb_perm_Generator.next ();
//					System.out.println();
//					if (!this.repetitiousVars(indexArray)) {
						for (int i = 0; i < indexArray.length; i++) {
							int k = indexArray[i];
							terms.add(mix.get(k));
//							System.out.print(mix.get(k).getName() + " ");
//							if (k >= variableList.size()) {
//								terms.add(newVariables.get(k % variableList.size()));
//							}
//							else 
//								terms.add(variableList.get(k));
						}
//						System.out.println();
						Literal l = new Literal();
						Literal nl = new Literal();
						
						//esto de if l.makeLiteral ya se puede quitar porque la clase de las 
						// permutaciones siempre genera tipos compatibles
						if (l.makeLiteral(p, terms, Sign.POSITIVE) && (nl.makeLiteral(p, terms, Sign.NEGATIVE))) {
							
//							l.printScreen();System.out.println();
							
							//it tries to avoid permutations of the type
							// predicate (X0,New_0,New_1)
							// predicate (X0, New_1,New_0)
							
							boolean addLiteral = true;
							addLiteral = checkDuplicateLiteral(l);// && checkDuplicateVars(l);
							
							//esto se puede optimizar
							if (p.isTarget() && isRecursiveLiteral(l))
								addLiteral = false;
							
							if (addLiteral) {
								//add literal pair to the list
								literalPair = new LiteralPair();
								// Not negated literal
								literalPair.setLiteral(l);
								// Negated literal
								literalPair.setNegatedLiteral(nl);
//										l.print();
//										System.out.println();
//										nl.print();
//										System.out.println();
								result.add(literalPair);
								if (possibleRel.containsKey(pName))
									nPossibleLiterals = possibleRel.get(pName);
								else nPossibleLiterals = 0;
								nPossibleLiterals++;
								possibleRel.put(pName, nPossibleLiterals);
								if (cluster != 0) cond = (++iterationIndex < end);
							}
						}
//					}
				}
				
				if (iterationIndex == end) {
					if (!result.isEmpty() && (cluster != 0))
						return result;
				}
				else {
					previousPredicate = p;
				}	
			}
		}
		return result;
	}

	/**
	 * Evita generar dos literales del tipo P(X0,X1,X2) y P(X0,X2,X1) cuando
	 * X1 y X2 son variables nuevas ya que van a generar el mismo conjunto de bindings
	 * @param l
	 * @return
	 */
	private boolean checkDuplicateLiteral(Literal l) {
		List<Term> newVars = new ArrayList<Term>();
		boolean result = false;
		List<Term> args = l.getArgs();
		int i = 0;
		while (i < args.size() && !result) {
			Term t = args.get(i);
			if (!t.isBound()) {
				if (newVars.contains(t) || (!newVars.isEmpty() && newVars.get(newVars.size()-1).sameName(t) < 0))
					result = true;
				else 
					newVars.add(t);
			}
				
			i++;
		}
//		System.out.println(!result);
		return !result;
	}

	private List<Term> createNewVariables(int numOfNewVariables){
		int count = 0;
		List<Term> result = null;
//		if (newGeneratedVars == null)
//			newGeneratedVars = new ArrayList<Term>();
		if (!newGeneratedVars.isEmpty())
			count = newGeneratedVars.size();
		for (int i = count; i < numOfNewVariables; i++) {
			newGeneratedVars.add(new Variable());
		}
		if (count > numOfNewVariables) {
			result = new ArrayList<Term>();
			for (int i = 0; i < numOfNewVariables; i++) {
				result.add(newGeneratedVars.get(i));
			}	
		}
		else 
			result = newGeneratedVars;
			
		return result;
	}

	@Override
	public boolean hasNext() {
		return comb_perm_Generator.hasNext();
	}

	@Override
	public int getPossibleCombNumber(Literal literal) {
		int result = 0;
		Predicate p = literal.getPredicate();
		StringBuffer pName = new StringBuffer(p.getName());
		
		if (p.isPredefined()) {
			boolean end = false;
			List<Term> args = literal.getArgs();
			int i = 0;
			while (i < args.size() && !end) {
				if (args.get(i) instanceof Constant) {
					pName.append("VC");
					end = true;
				}
				i++;
			}
			if (!end)
				pName.append("VV");
		}
		String s = pName.toString();
		if (this.possibleRel.containsKey(s))
			result = this.possibleRel.get(s);
		return result;
	}

	@Override
	public int getPossiblePredNumber() {
		return this.possibleRel.size();
	}

	@Override
	public void decPossibleCombNumber(String pName) {
		int value = 0;
		if (this.possibleRel.containsKey(pName)) {
			value = this.possibleRel.get(pName);
			this.possibleRel.put(pName, value-1);
		}
	}

	@Override
	public void setVariableList(List<Term> variableList) {
		this.variableList = variableList;
		rootArgs = variableList;
	}
	
	// check if l can cause infinite recursion
	// check if l can cause infinite recursion
	private boolean isRecursiveLiteral(Literal l) {
		boolean result = false;
		boolean reverse = false;
		VarPair vp;
		List<Term> arguments = l.getArgs();
		int i = 0;
		while (i < arguments.size() && !result) {
			Variable var1 = (Variable) rootArgs.get(i);
			Variable var2 = (Variable) arguments.get(i);
			if (this.constantOrderDirection < 2)
				vp = new VarPair(var2, var1, 1);
			else
				vp = new VarPair(var1, var2, 1);
			result = vp.belongs(this.orderedVars);
			if (!result && this.constantOrderDirection == 0) {
				vp = new VarPair(var1, var2, 1);
				reverse = vp.belongs(this.orderedVars);
			}
			i++;
		}
	
		if (result)
			result = false;
		else if (i == arguments.size() && !result)
			if (!(reverse && this.constantOrderDirection == 0))
				result = true;
	
		return result;
	}
	
	public void setConstantOrderDirection(int constantOrderDirection) {
		this.constantOrderDirection = constantOrderDirection;
	}
	
//	private List<VarPair> reversePairs(List<VarPair> orderedVars) {
//		List<VarPair> result = new ArrayList<VarPair>();
//		Iterator<VarPair> it = orderedVars.iterator();
//		while (it.hasNext()) {
//			VarPair varPair = it.next();
//			VarPair revVarPair = new VarPair(varPair.getVar2(),varPair.getVar1(),varPair.getSign());
//			result.add(revVarPair);
//		}
//		return result;
//	}

	/**
	 * Checks if the variable v passed as parameter is less than some of the existing vars
	 * @param v
	 * @return
	 */
	private boolean searchVarInOrderedVars(Term v) {
		boolean result = false;
		int i = 0;
		while (i < this.orderedVars.size() && !result) {
			VarPair varPair = this.orderedVars.get(i);
			int sign = varPair.getSign();
			if ((v.equals(varPair.getVar1()) && sign == 1) || 
					(v.equals(varPair.getVar2()) && sign == 2))
				result = true;
			i++;
		}
		return result;
	}
	
	private Set<Term> searchForVarsInPredefinedPred() {
		Set<Term> result = new HashSet<Term>();
		List<Node<Literal, InformationContext>> body = bodyRule;
		for (Node<Literal, InformationContext> node : body) {
			Literal l = node.getDataNode();
			if (l.getPredicate().getName().equals("EQUAL") && l.isPositive())
				result.add(l.getArgs().get(0));
		}
		return result;
	}
	
	private boolean repeated(Literal l) {
		boolean result = true;
		if (l.getPredicate().getName().equals("EQUAL")){
			Term arg1 = l.getArgs().get(0);
			if (this.varsInPredefefinedLit.contains(arg1))
				result = false;
		}
		return result;
		
	}
	
//	private boolean repetitiousVars(int[] permutation) {
//		boolean result = false;
//		int index1 = 0, index2 = 0;
//		for(int i = 0; i < permutation.length && !result; i++) {
//			index1 = permutation[i];
//			for(int j = i+1; j < permutation.length && !result; j++) {
//				index2 = permutation[j];
//				if (index1 == index2)
//					result = true;
//			}
//		}
//		return result;
//	}
	
//	/**
//	 * ES NECESARIO ESTE M�TODO
//	 * @param l
//	 * @return
//	 */
//	private boolean checkDuplicateVars(Literal l) {//yes, el literal puede a�adirse, no otherwise
//		boolean result = true, end = false, ok = false;
//		List<List<Integer>> duplicateArgs = new ArrayList<List<Integer>>();
//		List<Integer> index = null;
//		Sign sign = l.isPositive() ? Sign.POSITIVE : Sign.NEGATIVE;
//		
//		KnowledgeBaseFactory kb = KnowledgeBaseFactory.getInstance();
//		KnowledgeBaseOperations kBase = kb.getKnowledgeBaseOperations();
//		
//		List<Term> args = l.getArgs();
//		Term t1 = null, t2 = null;
//		
//		for (int i = 0; i < args.size(); i++) {//no tenemos que recorrer la lista entera, valdr�a recorrerla a la mitad o algo similar
//			index = new ArrayList<Integer>();
//			t1 = args.get(i);
//			ok = false;
//			for (int j = i+1; j< args.size(); j++){
//				t2 = args.get(j);
//				if (t1.equals(t2)) {
//					ok = true;
//					index.add(j);
//				}
//			}
//			if (ok) {
//				index.add(i);
//				duplicateArgs.add(index);
//			}
//		}
//		
//		if (!duplicateArgs.isEmpty()) {
//			List<List<Constant>> tuples = kBase.consultFacts(l.getPredicate().getName(), sign);
//			
//			int i = 0, z = 0, j = 0;
//			Constant c1 = null, c2 = null;
//			boolean found = false;
//			while ( z < tuples.size() && !found) {
//				List<Constant> constants = tuples.get(z);
//				i = 0;
//				end = false;
//				while (i < duplicateArgs.size() && !end) {
//					index = duplicateArgs.get(i);
//					c1 = constants.get(index.get(0));
//					j = 1;
//					while (j< index.size() && !end){
//						c2 = constants.get(index.get(j));
//						if (!c1.equals(c2)) 
//							end = true;
//						j++;
//					}
//					i++;
//				}
//				if (i == duplicateArgs.size() && !end)
//					found = true;
//				z++;
//			}
//			result = found;
//		}
//		return result;
//	}
//	
//	/**
//	 * ES NECESARIO ESTE M�TODO??
//	 * @param variableSet
//	 * @param arity
//	 * @param withRep
//	 * @return
//	 */
//	//obtiene todas las combinaciones agrupadas de arity en arity del conjunto de variables
//	private List<List<Term>> getCombinations (List<Term> variableSet, int arity, boolean withRep){
//		CombinationGeneration<Term> comb = null;
//		
//		if (withRep)
//			comb = new CombinationsWithRepetition<Term>(arity,variableSet.size());
//		else
//			comb = new CombinationsWithoutRepetition<Term>(arity,variableSet.size());
//		
//		List<List<Term>> resultList = new ArrayList<List<Term>>();
//		resultList.addAll(comb.getAllCombinations(variableSet));
//	
//		return resultList;
//	}
}