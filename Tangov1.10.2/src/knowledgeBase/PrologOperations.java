package knowledgeBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import combinatorial.CombinationGeneration;
import combinatorial.CombinationsWithoutRepetition;
import combinatorial.ICombinatorial;
import combinatorial.PermutationsOfConstantWithRepetition;
import combinatorial.PermutationsWithRepetition;

import properties.PropertiesStore;
import utilities.FoilException;
import utilities.Utilities;

import jpl.Atom;
import jpl.Compound;
import jpl.Query;
import jpl.Util;

import data.Constant;
import data.Literal;
import data.Predicate;
import data.PredicateType;
import data.Sign;
import data.Term;
import data.Type;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

/**
 * 
 * @author Patricia Jim�nez
 * 
 * This class is used to do all query operations with the prolog knowledge base
 *
 */
public class PrologOperations extends KnowledgeBaseOperations {
	
//	private String auxFileName = "";
	private String targetPredicateName;
		
	protected PrologOperations() {
		try {
			this.targetPredicateName = PropertiesStore.getProperty("goalPredicate");
			this.fileName = PropertiesStore.getProperty("knowledgeBase");
		} catch (FoilException e) {
			e.printStackTrace();
		}
	
		this.loadKnowledgeBase();
		this.tuples = new HashMap<String, List<List<Constant>>>();
		this.targetVars = new ArrayList<Term>();
		this.typeSet = new HashSet<String>();
		this.predicateSet = this.readDefinedPredicates(); // it has to be read before the constants because it loads the different kind of types
//		this.constantSet = this.readConstantSet(); // no deber�amos de cargar esto, ver donde se necesita
		this.partialconstantSet = this.getPartialConstantSet();
		this.loadData();
	}
	
//	protected PrologOperations reload() {	
//		try {
////			this.fileName = PropertiesStore.getProperty("knowledgeBase").toLowerCase();
//			if (this.reloadKnowledgeBase(PropertiesStore.getProperty("knowledgeBase"))) {
//				Variable.resetExistingVariableCounter(0);
//				this.loadData();
//			}
//		} catch (FoilException e) {
//			e.printStackTrace();
//		}
//		return this;
//	}
	
	/**
	 * Loads the knowledge base
	 */
	private void loadKnowledgeBase() {
		try {
//			fileName = PropertiesStore.getProperty("knowledgeBase");
			fileName = PropertiesStore.getProperty("knowledgeBase").toLowerCase();
//			fileName = PropertiesStore.getProperty("trainingFileName").toLowerCase();
			this.consult(this.fileName);
//			this.auxFileName = this.fileName;
			/**
			* Consult cwa file where is defined the predicate called create_negatives
			* which generates all negatives examples
			*/
			
			//este fichero ya no lo usamos cwa*.pl porque cuando tiene que generar muchos
			// ejemplos negativos lanza excepci�n
			
/*			String cwaFile = null;	
			
			cwaFile = PropertiesStore.getProperty("configurationArgsFile").toLowerCase();
			if (cwaFile.startsWith("."))
				cwaFile = cwaFile.replaceFirst(".", "");
			cwaFile = System.getProperty("user.dir") + cwaFile;
			this.consult(cwaFile);
			
			cwaFile = PropertiesStore.getProperty("cwaFile").toLowerCase();
			if (cwaFile.startsWith("."))
				cwaFile = cwaFile.replaceFirst(".", "");
			cwaFile = System.getProperty("user.dir") + cwaFile;
			this.consult(cwaFile); */
			
//			this.getKBPredicates();
			
		} catch (FoilException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reload the knowledge base if that has changed. This occurs when we use cross validation and
	 * we have to load the knowledge base test file
	 * @param fileName name of the file where is the knowledge base
	 * @return true if the knowledge base has changed and false in otherwise (if it remains the same)
	 */
	private boolean reloadKnowledgeBase(String fileName) {

		boolean result = false;

		//if the knowledgebase has changed, it is reloaded
		if (!this.fileName.equals(fileName)) {
			this.consult(fileName);
			this.fileName = fileName;
			result = true;
		}
		return result;
	}
	
	/**
	 * Consult a prolog file
	 * @param path absolute path of the knowledge base (*.pl format)
	 */
	private void consult(String path){

//		Atom atom = new Atom(path); // For MAC OS
		Atom atom = new Atom(path.toLowerCase());
		Query query = new Query("consult", atom);
//		System.out.println("query: " + query);
		if (!query.hasSolution())
			try {
				throw new FoilException("consult('" + path + "') failed");
			} catch (FoilException e) {
				e.printStackTrace();
			}
			
		query.close();
	}
	
	/**
	 * loads all required data in targetLiteral and gets 
	 * the coverage (negative and positive bindings set) of the target literal
	 */
	private void loadData() {
	
		if (!this.predicateSet.containsKey(targetPredicateName)){
			System.err.println("The target predicate \"" + targetPredicateName + "\" does not exist in the knowledge base");
			System.exit(1);
		}
		this.targetPredicate = this.predicateSet.get(targetPredicateName);
		PrologTransferObject pTO = this.loadBindingsSet(targetPredicate);
		
		Literal l = new Literal();
		l.setPredicate(targetPredicate);
		l.setArgs(pTO.getVariables());
		l.setSign(Sign.POSITIVE);
		
		this.targetLiteral = l;
		this.targetLiteral.setArgs(pTO.getVariables());
		this.targetVars = pTO.getVariables();
		this.nRelations = this.getRelationsNumber();
		this.origCoverage = new Coverage();
		
		//positive Bindings
		this.origCoverage.addPositiveBindingsSet(pTO.getPositiveBindings());
		//Negative Bindings
		if (pTO.getNegativeBindings().size() > 0) //from the knowledge base
			this.origCoverage.addNegativeBindingsSet(pTO.getNegativeBindings());
		else {//if the negative examples are not explicit in the knowledge base these are made from closed world assumption
			pTO.setConstantSet(this.readConstantSet(targetPredicate));
			this.origCoverage.addNegativeBindingsSet(HandCraftedOperations.cwa(pTO));
		}
//			instance.origCoverage.addNegativeBindingsSet(PrologOperations.cwa(pTO));
	
		this.argNotEqualTable = this.loadEqualProperty(); 
	}
	
	private Map<String, boolean[]> loadEqualProperty() {
		Map<String, boolean[]> result = new HashMap<String, boolean[]>();
		int arity = 0, index = 0;
		int[] combination = null;
		Query query = null;
		boolean[] argNotEqual = null;
		CombinationGeneration<Integer> comb = null;
		Predicate predicate = null;
		List<Type> predicateArgs = null;
		Set<Integer> existingVarsPositions = null;
		
		for (String predicateName : this.predicateSet.keySet()) {
			predicate = this.predicateSet.get(predicateName);
			arity = predicate.getArity();
			
			if (arity > 1) {
				predicateArgs = predicate.getArgsType();
				comb = new CombinationsWithoutRepetition<Integer>(2, arity);
				argNotEqual = new boolean[comb.count()];
				existingVarsPositions = new HashSet<Integer>();
				
				while (comb.hasNext()){
					combination = comb.next();
					index = Utilities.argPair(combination[1], combination[0]);
					// if the arguments in position combination[0] and combination[1] have not the same type, they cannot be equals
					if (predicateArgs.get(combination[0]).equals(predicateArgs.get(combination[1]))) {
					
						existingVarsPositions.add(combination[0]);
						existingVarsPositions.add(combination[1]);
						
						query = PrologUtilities.setofQuery(predicate, existingVarsPositions);
						
						try {
							if (query.hasSolution())
								argNotEqual[index] = false;
							else 
								argNotEqual[index] = true;
						} catch(Exception e) {
							argNotEqual[index] = true;
						}
						query.close();
					}
					else 
						argNotEqual[index] = true;
				}
				
				
				
//				while (comb.hasNext()){
//					combination = comb.next();
//					index = Utilities.argPair(combination[1], combination[0]);
//					if (predicateArgs.get(combination[0]).equals(predicateArgs.get(combination[1]))) {
//						counter = 0;
//						for (int i = 0; i < arity; i++) {
//							if (combination[0] == i || combination[1] == i)
//								var = existingvar;
//							else {
//								var = new jpl.Variable("X" + counter);
//								counter++;
//							}
//							args[i] = var;
//						}
//						
//						query = new Query(predicateName,args);
//						if (query.hasSolution())
//							argNotEqual[index] = false;
//						else 
//							argNotEqual[index] = true;
//					}
//					else 
//						argNotEqual[index] = true;
//				}
				
				result.put(predicateName, argNotEqual);
			}
		}
		
//		for (String predicateName : this.predicateSet.keySet()) {
//			System.out.print(predicateName + " --> ");
//			argNotEqual = result.get(predicateName);
//			predicate = this.predicateSet.get(predicateName);
//			arity = predicate.getArity();
//			comb = new CombinationsWithoutRepetition<Integer>(2, arity);
//			while(comb.hasNext()) {
//				combination = comb.next();
//				System.out.print("arg1 = " + combination[0] + " arg2 = " + combination[1]);
//				System.out.print(" " + argPair(combination[1], combination[0]));
//				System.out.print("\t");
//			}
//			System.out.println();
//		}
		
		return result;
	}

//	@Override
//	/**
//	 * Loads the knowledge base test file and load the new target literal and bindings set
//	 */
//	public Coverage loadTestCoverage() {
//		Coverage result = new Coverage();
//		try {
//			String file = PropertiesStore.getProperty("testFileName").toLowerCase();
//			this.reloadKnowledgeBase(file);
//			PrologTransferObject pTO = this.loadBindingsSet(this.targetPredicate.getName());
//			result.addPositiveBindingsSet(pTO.getPositiveBindings());
//			result.addNegativeBindingsSet(pTO.getNegativeBindings());
//		} catch (FoilException e) {
//			e.printStackTrace();
//		}
//		
//		return result;
//	}
	
	/**
	 * Given a predicate p, get a PrologTransferObject which contains all
	 * negatives and positives bindings,existing constants and variables (from positive bindings).  
	 * All this is taken from the KnowledgeBase
	 * @param targetPredicate: name of the target predicate to search for
	 * @return a PrologTransferObject 
	 */
	private PrologTransferObject loadBindingsSet(Predicate p) {
		
		String predicateName = p.getName();
		List<Type> argsType = p.getArgsType();
		PrologTransferObject result = new PrologTransferObject();
		Compound predicate = this.readPredicate(predicateName);
		PrologTermToBindingsTO prologTermToBindingsTO = new PrologTermToBindingsTO();
		Hashtable<?,?> solution;
		
		if (predicate != null) {//the predicate exist in the knowledge base
			jpl.Variable[] vars = new jpl.Variable[predicate.arity()];
			List<Term> variables = new ArrayList<Term>();
			
			if (!this.targetVars.isEmpty()) {
				//generates new predicates to make the query
				
				for (int i = 0; i < this.targetVars.size(); i++) {
					jpl.Variable X = new jpl.Variable(this.targetVars.get(i).getName());
					vars[i] = X; 
				}
				variables.addAll(this.targetVars);		
			}
			
			for (int i = this.targetVars.size(); i < predicate.arity(); i++) {
				jpl.Variable X = new jpl.Variable("New" + i);
				vars[i] = X;
				variables.add(new Variable(argsType.get(i)));
			}
			
			Query query = new Query(predicateName, vars);
			prologTermToBindingsTO.setVariables(variables);
			while (query.hasMoreSolutions()) {
				solution = query.nextSolution();
				prologTermToBindingsTO = PrologOperations.prologTermToBindings(solution, prologTermToBindingsTO, vars);
			}
			query.close();
			
			result.setPositiveBindings(prologTermToBindingsTO.getBindingsSet());
			result.setVariables(prologTermToBindingsTO.getVariables());
			
			//reading negative Bindings
			if (PrologUtilities.exists_Predicate(this.fileName, "not_" + predicateName, vars)) {
				query = new Query("not_" + query.toString());
				prologTermToBindingsTO = new PrologTermToBindingsTO();
				prologTermToBindingsTO.setVariables(variables);
				while (query.hasMoreSolutions()) {
					solution = query.nextSolution();
					prologTermToBindingsTO = PrologOperations.prologTermToBindings(solution, prologTermToBindingsTO, vars);
				}
				query.close();
				result.setNegativeBindings(prologTermToBindingsTO.getBindingsSet());
//				result.addConstants(prologTermToBindingsTO.getConstants()); //Should we add constants in negative bindings?? let it as it is by now... 
			}
			result.setPredicate(p);
		}
		
		PrologUtilities.garbage_collector();
		return result;
	}
	
	/**
	 * Transform the output of a prolog query executed before into our structure of Bindings
	 * @param elements results of prolog query
	 * @param variables variables list belonging to our Bindings set
	 * @param terms term list belonging to the query arguments
	 * @return ReadBindingsTO object that contains the Bindings Set, and the list of constants and 
	 * variables belonging to the Bindings Set
	 */
	private static PrologTermToBindingsTO prologTermToBindings(Hashtable<?, ?> solution, PrologTermToBindingsTO prologTtB, jpl.Variable[] terms) {
		PrologTermToBindingsTO result = new PrologTermToBindingsTO();
		
		int index;
		Variable v = null;
		Constant c = null;
		jpl.Term arg = null;
		Bindings bindings = null;
		List<Constant> constList = new ArrayList<Constant>(prologTtB.getConstants());
		Set<Bindings> bindingsSet = new HashSet<Bindings>(prologTtB.getBindingsSet());
		List<Term> variables = new ArrayList<Term>(prologTtB.getVariables());
		
		index = 0;
		bindings = new Bindings();
		String value;
		for (jpl.Term term : terms) {
			arg = (jpl.Term) solution.get(term.name());
			value = getTermValue(arg);
			v = (Variable) variables.get(index);
			Type type = v.getType();
			
			c = new Constant(value, type);
			if (c.belongs(constList) == -1)
				constList.add(c);
			bindings.addBinding(v, c);
			index++;
		}
		if (!BindingsOperations.belongs(bindings, bindingsSet))
			bindingsSet.add(bindings);
		
		result.setConstants(constList);
		result.setVariables(variables);
		result.setBindingsSet(bindingsSet);
		return result;
	}
	
	/**
	 * Given a predicate name, and the type of the resulting examples (positives or negatives)
	 * returns a list of constants with all tuples (pos or neg) existing in the knowledge base 
	 * @param predicateName: name of the predicate to search for
	 * @param sign: sign of the query to get positives or negatives examples
	 * @return a List<List<Constant>> with all obtained tuples
	 */
	
	public List<List<Constant>> consultFacts(String predicateName, Sign sign) {
		List<List<Constant>> result;
		String name = "";
		if (sign.equals(Sign.NEGATIVE))
			name = "not_" + predicateName;
		else
			name = predicateName;
		if (this.tuples.containsKey(name))
			result = this.tuples.get(name);
		else {
			result = new ArrayList<List<Constant>>();
			Compound predicate = this.readPredicate(predicateName); 
			
			if (predicate != null) {//the predicate exist in the knowledge base
				Query query = null;
				Hashtable<?,?> solution; // Hashtable<String, JPL.Term>
				jpl.Variable[] vars = null;
				
				//generating the query
				vars = new jpl.Variable[predicate.arity()];
				for (int i = 0; i < predicate.arity(); i++) {
					jpl.Variable X = new jpl.Variable("New" + i);
					vars[i] = X; 
				}
	
				if (PrologUtilities.isFact(predicateName, vars.length, this.fileName)) {
					query = new Query (predicate.name(), vars);
					if (sign == Sign.NEGATIVE) {
						query = new Query ("not(" + query.toString() + ")");
					}
					//reading the results of the query
					
					List<Type> argsType = this.predicateSet.get(predicateName).getArgsType();
					while (query.hasMoreSolutions()) {
						solution = query.nextSolution();
						result.add(PrologOperations.prologTermToTupleSet(solution, vars, argsType));
					}
					query.close();
					this.tuples.put(name, result);
					
					query.close();
				}
			}
		}
		PrologUtilities.garbage_collector();
		return result;
	}
	
	/**
	 * Given a predicate name, and the type of the resulting examples (positives or negatives)
	 * returns a list of constants with all tuples (pos or neg) existing in the knowledge base 
	 * @param predicateName: name of the predicate to search for
	 * @param sign: sign of the query to get positives or negatives examples
	 * @return a List<List<Constant>> with all obtained tuples
	 */
	
	
	public List<List<Constant>> consultTuplesFromRules(Predicate pred, Sign sign) {
		List<List<Constant>> result;
		String predicateName = pred.getName();
		String name = "";
		
		Map<String, Integer> constantSetSize = new HashMap<String, Integer>();
//		Iterator<Entry<String, List<Constant>>> it = constantSet.entrySet().iterator();
		Map<String, List<Constant>> constantSet = this.readConstantSet(pred);
		Iterator<Entry<String, List<Constant>>> it = constantSet.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, List<Constant>> entry = it.next();
			constantSetSize.put(entry.getKey(), entry.getValue().size());
		}
		
		if (sign.equals(Sign.NEGATIVE))
			name = "not_" + predicateName;
		else
			name = predicateName;
		if (this.tuples.containsKey(name))
			result = this.tuples.get(name);
		else {
			result = new ArrayList<List<Constant>>();
			Predicate predicate = this.predicateSet.get(predicateName); 
			int arity = predicate.getArity();
			if (predicate != null) {//the predicate exist in the knowledge base
				Hashtable<?, ?> solution;
				Query query = null;				
				if (PrologUtilities.isFact(predicateName, arity, this.fileName)) {
					jpl.Variable[] vars = new jpl.Variable[arity];
					//making the query
					for (int i = 0; i < arity; i++) {
						jpl.Variable X = new jpl.Variable("New" + i);
						vars[i] = X; 
					}
					
					query = new Query (predicate.getName(), vars);
					if (sign == Sign.NEGATIVE) {
						query = new Query ("not(" + query.toString() + ")");
					}
					//reading the results of the query
					List<Type> argsType = this.predicateSet.get(predicateName).getArgsType();
					while (query.hasMoreSolutions()) {
						solution = query.nextSolution();
						result.add(PrologOperations.prologTermToTupleSet(solution, vars, argsType));
					}
					query.close();
					this.tuples.put(name, result);
				}
				else {
					result = new ArrayList<List<Constant>>();
					List<Constant> acceptedValues = null;
					Predicate p = this.predicateSet.get(predicateName);
					List<Type> argsTypes = p.getArgsType();
					jpl.Term[] atoms = new jpl.Term[arity];
					ICombinatorial<Integer> ic = new PermutationsOfConstantWithRepetition<Integer>(p.getArgsType(), constantSetSize);
					while (ic.hasNext()) {
						int[] tuple = ic.next();
						for (int i = 0; i < tuple.length; i++) {
							List<Constant> constants = constantSet.get(argsTypes.get(i).getTypeName());
							jpl.Term a = Util.textToTerm(constants.get(tuple[i]).getValue());
							atoms[i] = a;
						}
						query = new Query(p.getName(), atoms);
						if (sign == Sign.NEGATIVE) {
							query = new Query ("not(" + query.toString() + ")");
						}
//						System.out.println(query.toString());
						if (query.hasSolution()) {
							acceptedValues = new ArrayList<Constant>();
							Constant c = null;
							for (int i = 0; i < atoms.length; i++) {
								if (atoms[i].isAtom() || atoms[i].isVariable())
									c = new Constant(atoms[i].name(), argsTypes.get(i));
								else if (atoms[i].isInteger())
									c = new Constant(String.valueOf(atoms[i].intValue()), argsTypes.get(i));
								else if (atoms[i].isFloat())
									c = new Constant(String.valueOf(atoms[i].floatValue()), argsTypes.get(i));
								else if (atoms[i].isCompound()) {
									jpl.Term[] terms1 = atoms[i].toTermArray();
									String value = "[";
									if (terms1.length == 0)
										value += "],";
									else { 
										for (int j = 0; j < terms1.length; j++) {
											value += terms1[j].intValue() + ",";
										}
										value = value.substring(0, value.lastIndexOf(",")) + "]";
									}
									c = new Constant(value, argsTypes.get(i));
								}
								acceptedValues.add(c);
							}
							result.add(acceptedValues);
						}
						query.close();
					}
					this.tuples.put(name, result);
				}
			}
		}
		PrologUtilities.garbage_collector();
		return result;
	}
	
	/**
	 * Transform the output of a prolog query executed before into our structure of Tuples (constant List) 
	 * @param elements result of the prolog query
	 * @param terms variable list belonging to the query arguments
	 * @return a constant list with the obtained results
	 */
	private static List<Constant> prologTermToTupleSet(Hashtable<?, ?> solution, jpl.Term[] terms, List<data.Type> typeList) {
		
		List<Constant> result = new ArrayList<Constant>();
		jpl.Term arg = null;
		int index = 0;
		
		index = 0;
		for (jpl.Term term : terms) {
			arg = (jpl.Term) solution.get(term.name());
			String value = getTermValue(arg);
			data.Type type = typeList.get(index);
				
			Constant c = new data.Constant(value, type);
			result.add(c);
			index++;
		}
		
		PrologUtilities.garbage_collector();
		return result;
	}
	
	
	/**
	 * Searches for a predicate name for the KnowledgeBase and returns in 
	 * the form of a Predicate class
	 * @param targetPredicate: name of the target predicate to search
	 * @return the predicate searched if it exists in the KnowledgeBase
	 */
	private Compound readPredicate(String predicateName) {
		Compound result = null;
		Query query = null;
		Atom predicate = new Atom(predicateName);
		jpl.Variable X = new jpl.Variable("X");
		jpl.Term[] args = new jpl.Term[]{predicate, X};
		//consult specific predicate in the knowledge base
		query = new Query("current_predicate",args);
		
		if (query.hasSolution()){
			Hashtable<?, ?> solution = query.oneSolution();
			Object pred = solution.get("X");
			if (pred instanceof jpl.Compound)
				result = (Compound) pred;
		}
		query.close();
		PrologUtilities.garbage_collector();
		return result;
	}
	
	/**
	 * @return the number of predicates contained in the knowledge base 
	 */
	private int getRelationsNumber() {
		int result = 0;
		List<String> predicateNames = this.getAllPredicatesNames();
		//it does not count not predicate (if exists)
		if (predicateNames.contains("not"))
			result = predicateNames.size() - 1;
		else 
			result = predicateNames.size();
		
		return result - 1; //-1 because of the typedef predicate
	}
	
	/**
	 * Gets all the predicate names stated in the knowledge base
	 * @return predicate names list
	 */
	private List<String> getAllPredicatesNames() {
		
		List<String> result = new ArrayList<String>();
		jpl.Variable Z = new jpl.Variable("Z");
		jpl.Atom atom = new Atom(this.fileName);
		jpl.Term[] args = new jpl.Term[]{Z, atom};
		
		Query query = new Query("source_file",args);
		if (query.hasSolution())
			while(query.hasMoreSolutions()) {
				Hashtable<?, ?> solution = query.nextSolution();
				if (solution.get("Z") instanceof Compound) {
					Compound c = (Compound) solution.get("Z");
					if (!c.name().equals("not") && !c.name().equals("mode"))
						result.add(c.name());
				}
				solution = null;
			}
		query.close();
		
		PrologUtilities.garbage_collector();
		return result;	
	}
	
	/**
	 * Get all predicates stated in the knowledgeBase in Predicate class structure 
	 * @return a list with all predicates stated in the knowledgebase
	 */
	
	protected void getKBPredicates() {
		List<String> predicateNames = null;
		ArrayList<Type> argsType = null;
		Compound predicate = null;
		Predicate p = null;
		Type type = null;
		Query query = null;
		
		predicateNames = this.getAllPredicatesNames();
		for (String predicateName : predicateNames) {
			predicate = this.readPredicate(predicateName);
			p = new Predicate();
			p.setName(predicate.name());
			
			jpl.Variable[] vars = new jpl.Variable[predicate.arity()];
			//generates new predicates to make the query
			for (int i = 0; i < predicate.arity(); i++) {
				jpl.Variable X = new jpl.Variable("New" + i);
				vars[i] = X;
			}

			query = new Query(new Compound(predicate.name(), vars));
			
			//getting the types of the arguments belonging to the predicate
			Hashtable<?, ?> solution = query.oneSolution();
			argsType = new ArrayList<Type>();
			if (!solution.isEmpty()){
				
				List<String> types = this.readTypes(predicate.name(), vars);
				for (int i = 0; i < types.size(); i++) {
					type = new Type(types.get(i));
					argsType.add(type);
				}
				
			}
			query.close();
			
			p.setArgsType(argsType);
			if (this.targetPredicate.getName().equals(p.getName()))
				p.setPredicateType(PredicateType.TARGET);
			else 
				p.setPredicateType(PredicateType.SUPPORT);
			
			predicateSet.put(p.getName(), p);
		}
		
		PrologUtilities.garbage_collector();
	}
	
	public List<String> readTypes(String predicateName, jpl.Term[] vars) {
		List<String> result = new ArrayList<String>();
		Query query = null;
		
		Atom predicate = new Atom(predicateName);
		jpl.Term[] args = new jpl.Term[vars.length + 1];
		args[0] = predicate;
		for(int i = 0; i< vars.length; i++) {
			args[i+1] = vars[i];
		}
		
		//consult specific predicate in the knowledge base
		query = new Query("mode",args);
		
		if (query.hasSolution()){
			Hashtable<?, ?> solution = query.oneSolution();
			for(int i = 0; i < vars.length; i++) {
				String index = "New" + i;
				jpl.Term term = null;
				if (solution.containsKey(index))
					term = (jpl.Term)solution.get(index);
				result.add(term.name());
			}
		} 
		query.close();
		PrologUtilities.garbage_collector();
		return result;
	}
	
	
	/**
	 * Gets all constants from the KnowledgeBase group by type
	 */
	@Override
	public Map<String, List<Constant>> getConstantSet(Predicate p) {
		return this.readConstantSet(p);
	}	
	
	@Override
	public List<Constant> getConstantSet(String type) {
		return this.readConstantSet(type);
	}
	
	/**
	 * 
	 * @param argument
	 * @return
	 */
	private static String getTermValue(jpl.Term argument) {
		String result = "";
				//getting constant value
		if (argument.getClass().getName().equals("jpl.Compound")) {
			result = PrologUtilities.compoundToString((Compound) argument);
		}
		else
			result = argument.toString();
		return result;
	}
	
	 public static jpl.Term termArrayToList(jpl.Term[] terms) {
         jpl.Term list = new jpl.Atom("[]");

         for (int i = terms.length - 1; i >= 0; --i) {
               list = new jpl.Compound(".", new jpl.Term[] { terms[i], list });
         }
         return list;
   }

	@Override
	public List<Predicate> getDefinedPredicates() {
		List<Predicate> result = new ArrayList<Predicate>();
		Iterator<Entry<String, Predicate>> it = this.predicateSet.entrySet().iterator();
		while (it.hasNext()) {
			Predicate p = it.next().getValue();
			result.add(p);
		}
		return result;
		
		
//		List<Predicate> result = new ArrayList<Predicate>();
//		
//		Iterator<Entry<String, Predicate>> it = this.predicateSet.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry e = (Map.Entry)it.next();
//			Predicate p = (Predicate) e.getValue();
//			result.add(p);
//		}
//		return result;
	}

	@Override
	public Map<String, List<Constant>> getPartialConstantSet() {
		if (this.partialconstantSet == null)
			this.partialconstantSet = this.readConstantsAllowed(typeSet);
		return this.partialconstantSet;
	}
	
	@Override
	public Map<String, List<Constant>> getFullConstantSet() {
		if (this.fullconstantSet == null) {
			Iterator<String> it = typeSet.iterator();
			this.fullconstantSet = new HashMap<String, List<Constant>>();
			while(it.hasNext()) {
				String type = it.next();
				this.fullconstantSet.put(type, this.readConstantSet(type));
			}	
		}
		return this.fullconstantSet;
	}
	
	private Map<String, List<Constant>> readConstantsAllowed(Set<String> typeSet) {
		Map<String, List<Constant>> result = new HashMap<String, List<Constant>>();
		List<Constant> constants = null;
		Query query = null, q = null;
		
		jpl.Variable v1= new jpl.Variable("X");
		jpl.Variable v2= new jpl.Variable("Y");
		jpl.Term[] args_typedef = new jpl.Variable[2];
		args_typedef[0] = v1;
		args_typedef[1] = v2;
		jpl.Compound compound = new jpl.Compound("constantsList", args_typedef);
//		jpl.Atom atom = new Atom(this.fileName); // for MAC
		jpl.Atom atom = new Atom(this.fileName.toLowerCase());
		jpl.Term[] args = new jpl.Term[]{compound,atom};
		query = new Query("source_file", args);
		if (query.hasSolution()) {
			args = new jpl.Term[2]; 
			args[1] = new jpl.Variable("X");
			
			Iterator<String> iterator = typeSet.iterator();
			
			while (iterator.hasNext()) {
				constants = new ArrayList<Constant>();
				String typeName = iterator.next();
				args[0] = new Atom(typeName);
				//consult specific predicate in the knowledge base
				q = new Query("constantsList",args);
				if (q.hasSolution()){
					Hashtable<?, ?> solution = q.oneSolution();
					jpl.Compound term = (jpl.Compound)solution.get("X");
					List<String> list = this.listToString(term);
					for (String s : list) {
						Constant c = new Constant(s, new Type(typeName));
						constants.add(c);
					}
					result.put(typeName, constants);
				} 
				q.close();
			}
			
			PrologUtilities.garbage_collector();
		}
		query.close();
		return result;
	}
	
	private List<String> listToString(Compound compound) {
		List<String> result = new ArrayList<String>();
		jpl.Term[] terms = compound.toTermArray();
		
		for (jpl.Term term : terms) {
			if (term.isCompound())
				result.add(PrologUtilities.compoundToString(term));
			else
				result.add(term.toString());
		}
		
		return result;
	}
	
	
	protected Hashtable<String, Predicate> readDefinedPredicates() {
		
		Hashtable<String, Predicate> result = new Hashtable<String, Predicate>();
		List<Type> argsType = null;
		Predicate p = null;
		Query query = null, predicateQuery = null;
		jpl.Variable X = null, Y = null;
		jpl.Integer instArgNumber = null;
		jpl.Term c = null;
		jpl.Term[] args = null, vars = null;
		Hashtable<?, ?> solution, solution2;
		List<int[]> instantiatedArgsList = null;
		int[] instantiatedArgs = null;
		
		X = new jpl.Variable("X");
		Y = new jpl.Variable("Y");
		args = new jpl.Term[]{X,Y};
		
		query = new Query("mode",args);
		if (query.hasSolution()) {
			while (query.hasMoreSolutions()) {
				instantiatedArgsList = new ArrayList<int[]>();
				solution = query.nextSolution();
				System.out.println("solution: " + solution.toString());
				c = (Compound) solution.get("Y");
				System.out.println("compound Y: " + c.toString() + " arity: " + c.arity());
				if (!result.containsKey(c.name())) {
					p = new Predicate();
					p.setName(c.name());
					
					vars = new jpl.Variable[c.arity()];
					//generates new predicates to make the query
					for (int i1 = 0; i1 < c.arity(); i1++) {
						jpl.Variable Z = new jpl.Variable("New" + i1);
						vars[i1] = Z;
					}
					
					args = new jpl.Term[]{X,new Compound(c.name(), vars)};
					predicateQuery = new Query("mode", args);
					
					//getting the types of the arguments belonging to the predicate
					boolean instantiated = true;
					while (predicateQuery.hasMoreSolutions() && instantiated) {
						solution2 = predicateQuery.nextSolution();
						X = new jpl.Variable("X");
						instArgNumber = (jpl.Integer) solution2.get("X");
						instantiatedArgs = new int[c.arity()];
						
						argsType = new ArrayList<Type>();
						for (int i1 = 0; i1 < c.arity(); i1++) {
							jpl.Term t = (jpl.Term) solution2.get(vars[i1].name());
							String typeName = t.name();
							
							if (typeName.startsWith("+")) {
								instantiatedArgs[i1] = 1; //true
								typeName = t.args()[0].name();
							}
							else if (typeName.startsWith("-")){
								instantiatedArgs[i1] = 0; //false
								typeName = t.args()[0].name();
							}
							else
								instantiated = false;
							Type type = new Type(typeName);
							argsType.add(type);
							if (!this.typeSet.contains(type.getTypeName()))
								this.typeSet.add(type.getTypeName());
						}
						
						if (!instantiated){
							ICombinatorial<Integer> ic = new PermutationsWithRepetition<Integer>(c.arity(), 2); //2 because there is only two values (positive = 1, negative = 0).
							while (ic.hasNext()) {
								int mandatory = 0;
								instantiatedArgs = ic.next();
								for (int i1 = 0; i1 < instantiatedArgs.length; i1++) {
									if (instantiatedArgs[i1] == 1) {
										mandatory++;
									}
								}
								if (mandatory >= instArgNumber.intValue()) {
									int[] aux = new int[instantiatedArgs.length];
									aux = Arrays.copyOfRange(instantiatedArgs, 0, instantiatedArgs.length);
									instantiatedArgsList.add(aux);
								}
							}
						}
						else 
							instantiatedArgsList.add(instantiatedArgs);
					}
					predicateQuery.close();
				
					p.setInstantiations(instantiatedArgsList);
					p.setArgsType(argsType);
					if (targetPredicateName.equals(p.getName()))
						p.setPredicateType(PredicateType.TARGET);
					else 
						p.setPredicateType(PredicateType.SUPPORT);
					
					result.put(p.getName(), p);
					System.out.println("OK");
				}
			}
		}
		query.close();
		PrologUtilities.garbage_collector();
		return result;
	}
	
	private List<Constant> readConstantSet(String typeName) {
		
		List<Constant> result = new ArrayList<Constant>();
		Set<String> constantSet = new HashSet<String>();
		Query source_file = null, query = null;
		Constant constant = null;
		String value = "", pname = "", predicateName = "";
		List<Type> typeList = null;
		Hashtable<?, ?> solution;
		Set<Integer> index;
		
		jpl.Variable Z = new jpl.Variable("Z");
//		Atom file = new Atom(fileName); // for MAC OS
		Atom file = new Atom(fileName.toLowerCase());
		jpl.Term[] args = new jpl.Term[]{Z, file};
		
		source_file = new Query("source_file",args);
		
		if (source_file.hasSolution())
			while(source_file.hasMoreSolutions()) {
				solution = source_file.nextSolution();
				if (solution.get("Z") instanceof Compound) {
					Compound p = (Compound) solution.get("Z");
					pname = p.name();
					if (!pname.equals("mode") && !pname.equals("constantsList")) {
						
						predicateName = pname.startsWith("not_") ? pname.substring(pname.lastIndexOf("not_")+4) : pname;
						if (predicateSet.containsKey(predicateName)) {
							typeList = predicateSet.get(predicateName).getArgsType();
							index = new HashSet<Integer>();
							for (int j = 0; j < typeList.size(); j++) {
								if (typeList.get(j).getTypeName().equals(typeName))
									index.add(j);
							}
							
							if (!index.isEmpty()) {
								//Getting the constants for each predicate
								if (PrologUtilities.isFact(pname, p.arity(), this.fileName)) { //we have to figure out if a specific predicate is defined intensionally or extensionally.
									jpl.Term[] terms = new jpl.Variable[p.arity()];
									int i = 0;
									while (i < p.arity()) {
										jpl.Variable X = new jpl.Variable("New" + i);
										terms[i] = X;
										i++;
									}
									
									query = new Query(pname,terms);
									if (query.hasSolution()) {
										if (pname.startsWith("not_"))
											pname = pname.substring(pname.lastIndexOf("not_")+4);
										
										while (query.hasMoreSolutions()) {
											Hashtable<?,?> argList = query.nextSolution();
											for (Integer ind : index) {
												jpl.Term term = terms[ind];
												value = "";
												Object parameterType = argList.get(term.name()).getClass().getName();
													
												//if the term is a Compound
												if (parameterType.equals("jpl.Compound")) {
													Compound constants = (Compound) argList.get(term.name());
													value = PrologUtilities.compoundToString(constants);
													constant = new Constant(value, new Type(typeName));
												}
												else if (!(parameterType.equals("jpl.Variable"))) {
													value = argList.get(term.name()).toString();
													constant = new Constant(value,new Type(typeName));
												}
												if (!constantSet.contains(value)) {
													constantSet.add(value);
													result.add(constant);
												}
//												if (constant.belongs(result) == -1)
//														result.add(constant);
											}
										}// end while
									}//end if query.hasSolution()
									query.close();
								}//end if Fact
							}
						}
						
					}//end if p.name = {mode,constantList}
					
				}// end solution is compound
			}//end while
		
			Set<String> typeSet = new HashSet<String>();
			typeSet.add(typeName);
			Map<String, List<Constant>> constantSetAllowed = this.readConstantsAllowed(typeSet);
			for (String s: constantSetAllowed.keySet()) {
				List<Constant> list = constantSetAllowed.get(s);
				for (Constant c : list) {
					if (c.belongs(result) < 0)
						result.add(c);
				}
			}
		
		source_file.close();
		PrologUtilities.garbage_collector();
		
		return result;
	}
	
		
	private Map<String, List<Constant>> readConstantSet(Predicate predicate) {
		
		Map<String, List<Constant>> result = new HashMap<String, List<Constant>>();
		Set<String> types = new HashSet<String>();
		List<Type> predicateTypes = predicate.getArgsType();
		for (int i = 0; i < predicateTypes.size(); i++) {
			types.add(predicateTypes.get(i).getTypeName());
		}
		
		for(String t: types)
			result.put(t, this.readConstantSet(t));
		
		return result;
	}

	@Override
	protected void reload(String fileName) {
		if (this.reloadKnowledgeBase(fileName.toLowerCase())){
//		if (this.reloadKnowledgeBase(fileName)) {
			Variable.resetExistingVariableCounter(0);
			this.loadData();
		}
	}

	@Override
	public Map<String, Integer> getNameAndArityOfAllDefinedPredicates() {
		Map<String,Integer> result = new HashMap<String,Integer>();
		jpl.Variable Z = new jpl.Variable("Z");
		jpl.Atom atom = new Atom(this.fileName);
		jpl.Term[] args = new jpl.Term[]{Z, atom};
		
		Query query = new Query("source_file",args);
		if (query.hasSolution())
			while(query.hasMoreSolutions()) {
				Hashtable<?, ?> solution = query.nextSolution();
				if (solution.get("Z") instanceof Compound) {
					Compound c = (Compound) solution.get("Z");
						result.put(c.name(), c.arity());
				}
				solution = null;
			}
		query.close();
		return result;
	}

//	@Override
//	public Map<String, Set<String>> getConstantsList(Map<String,Set<String>> constantsList) {
//		Map<String,Set<String>> result = new HashMap<String,Set<String>>(constantsList);
//		Set<String> constantSet;
//		jpl.Variable typeName = new jpl.Variable("T");
//		jpl.Variable listOfConstants = new jpl.Variable("L");
//		jpl.Term[] args = new jpl.Term[]{typeName,listOfConstants};
//		
//		if (PrologUtilities.exists_Predicate(this.fileName,"constantsList",args)) {
//			Query query = new Query("constantsList",args);
//			if (query.hasSolution())
//				while(query.hasMoreSolutions()) {
//					Hashtable<?, ?> solution = query.nextSolution();
//					if (solution.get("T") instanceof Compound && solution.get("L") instanceof Compound) {
//						String type = ((Compound) solution.get("T")).name();
//						Compound list = (Compound) solution.get("L");
//						jpl.Term[] constantsArray = Util.listToTermArray(list);
//						if (result.containsKey(type))
//							constantSet = result.get(type);
//						else
//							constantSet = new HashSet<String>();
//						for (int i = 0; i < constantsArray.length; i++)
//							constantSet.add(constantsArray[i].toString());
//						result.put(type, constantSet);
//					}
//				}
//			query.close();
//		}
//		return result;
//	}

	@Override
	public Set<String> loadPositiveExamples(String predicateName, int arity) {
		Set<String> result = new HashSet<String>();
		Query query = null;
		StringBuffer instance;
		Hashtable<?,?> solution;
		
		if (!this.fileName.isEmpty()) {
			jpl.Variable[] vars = new jpl.Variable[arity];
			for (int i = 0; i < vars.length; i++) {
				jpl.Variable X = new jpl.Variable("X" + i);
				vars[i] = X; 
			}
			if (PrologUtilities.exists_Predicate(this.fileName, predicateName, vars)){
				query = new Query(predicateName, vars);
				while (query.hasMoreSolutions()) {
					solution = query.nextSolution();
					instance = new StringBuffer(predicateName + "(");
					for (int i = 0; i < vars.length; i++)
						instance.append(solution.get(vars[i].name) + ",");
					instance = new StringBuffer(instance.substring(0, instance.lastIndexOf(",")));
					instance.append(").");
					result.add(instance.toString());
				}
				query.close();
			}
		}
		return result;
	}

//	@Override
//	public Map<String, Integer> getModePredicates() {
//		Map<String,Integer> result = new HashMap<String,Integer>();
//		jpl.Variable number = new jpl.Variable("N");
//		jpl.Variable predicate = new jpl.Variable("P");
//		jpl.Term[] args = new jpl.Term[]{number,predicate};
//		
//		if (PrologUtilities.exists_Predicate(this.fileName,"mode",args)) {
//			Query query = new Query("mode",args);
//			if (query.hasSolution())
//				while(query.hasMoreSolutions()) {
//					Hashtable<?, ?> solution = query.nextSolution();
//					String n = solution.get("N").toString();
//					String predicateDefinition = "";
//					if (solution.get("P") instanceof Compound) {
//						Compound c = (Compound) solution.get("P");
//						predicateDefinition = c.name() + "(";
//						for(int i = 1; i <= c.arity(); i++) {
//							jpl.Term term = c.arg(i);
//							predicateDefinition += term.name() + (term.arity() == 0 ? "" : term.arg(1)) + ",";
//						}
//						predicateDefinition = predicateDefinition.substring(0,predicateDefinition.lastIndexOf(",")) + ")";
//					}
//					result.put(predicateDefinition, Integer.valueOf(n));
//				}
//			query.close();
//		}
//		return result;
//	}
	
	
//	private Map<String, List<Constant>> readConstantSet() {
//		
//		Map<String, List<Constant>> result = new HashMap<String, List<Constant>>();
//		
//		List<Constant> constantList = null;
//		Query source_file = null, query = null;
//		Constant constant = null;
//		String type = "", value = "", pname = "";
//		List<Type> typeList = null;
//		
//		jpl.Variable Z = new jpl.Variable("Z");
//		Atom file = new Atom(fileName);
////		Atom file = new Atom(fileName.toLowerCase());
//		jpl.Term[] args = new jpl.Term[]{Z, file};
//		
//		source_file = new Query("source_file",args);
//		
//		if (source_file.hasSolution())
//			while(source_file.hasMoreSolutions()) {
//				Hashtable<?, ?> solution = source_file.nextSolution();
//				if (solution.get("Z") instanceof Compound) {
//					Compound p = (Compound) solution.get("Z");
//					pname = p.name();
//					if (!pname.equals("mode") && !pname.equals("constantsList")) {
//						int i = 0;
//
//						//Getting the constants for each predicate
//						if (PrologUtilities.isFact(pname, p.arity(), this.fileName)) { //we have to identify if a specific predicate is defined intensionally or extensionally.
//							jpl.Term[] terms = new jpl.Variable[p.arity()];
//							while (i < p.arity()) {
//								jpl.Variable X = new jpl.Variable("New" + i);
//								terms[i] = X;
//								i++;
//							}
//							
//							query = new Query(pname,terms);
//							if (query.hasSolution()) {
//								if (pname.startsWith("not_"))
//									pname = pname.substring(pname.lastIndexOf("not_")+4);
//								typeList = predicateSet.get(pname).getArgsType();
//								while (query.hasMoreSolutions()) {
//									Hashtable<?,?> argList = query.nextSolution();
//									i = -1;
//									for (jpl.Term term : terms) {
//										i++;
//										value = "";
//										Object parameterType = argList.get(term.name()).getClass().getName();
//										type = typeList.get(i).getTypeName();
//										
//										//if the term is a Compound
//										if (parameterType.equals("jpl.Compound")) {
//											Compound constants = (Compound) argList.get(term.name());
//											constant = new Constant(PrologUtilities.compoundToString(constants), 
//													new Type(type));
//										}
//										else if (!(parameterType.equals("jpl.Variable"))) { // por qu� preguntamos si es variable??
//											value = argList.get(term.name()).toString();
//											constant = new Constant(value,new Type(type));
//										}
//											
//										if (result.containsKey(type)) {
//											constantList = result.get(type);
//											if (constant.belongs(constantList) == -1)
//												constantList.add(constant);
//										}
//										else  {
//											constantList = new ArrayList<Constant>();
//											constantList.add(constant);
//										}
//								
//										result.put(type, constantList);
//									} // end for (terms)
//								}
//							}//end if query.hasSolution()
//							query.close();
//						}//end if Fact
//					}//end if p.name = {mode,constantList}
//					
//				}// end solution is compound
//			}//end while
//
//		Map<String, List<Constant>> constantSetAllowed = this.readConstantsAllowed(result.keySet());
//		for (String s: constantSetAllowed.keySet()) {
//			List<Constant> list = constantSetAllowed.get(s);
//			for (Constant c : list) {
//				if (c.belongs(result.get(s)) < 0)
//					result.get(s).add(c);
//			}
//			
//		}
//		
//		source_file.close();
//		PrologUtilities.garbage_collector();
//		
////		Iterator<Entry<String, List<Constant>>> prueba = result.entrySet().iterator();
////		while (prueba.hasNext()) {
////			Entry<String, List<Constant>> e = prueba.next();
////			List<Constant> prueba2 = (List<Constant>) e.getValue();
////			System.out.println(e.getKey());
////			for (int j = 0; j < prueba2.size(); j++) {
////				System.out.print(prueba2.get(j).getValue()+ ", ") ;
////			}
////			System.out.println();
////		}
//		
//		return result;
//	}
}