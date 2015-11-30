package knowledgeBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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
import data.Type;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;
import jpl.Compound;
import jpl.PrologException;
import jpl.Query;

/**
 * SwiProlog is a Singleton
 * @author patri
 *
 */
public class SwiProlog extends BindingsOperations {
	
	
	protected SwiProlog() {}
	
	/**
	 * Executes a prolog query
	 * @param parameters list of term of the query
	 * @param predicateName name of the predicate of the query
	 * @param isNegated sign of the query
	 * @return hashtable with the solutions
	 */
//	private Hashtable<?,?>[] query(List<Term> parameters, Predicate p, boolean isNegated) {
//		Hashtable<?,?>[] result = null;
//		Query query = PrologUtilities.query(parameters, p,isNegated);
//		result = query.allSolutions();
//		query.close();
//		query = null;
////		PrologUtilities.garbage_collector();
//		return result;
//	}
	
	/**
	 * Generates an argument list instantiated for the literal
	 * @param b bindings that contain the set of Variable = Value, to instantiate
	 * @param literalArgs set of variables that will be instantiated
	 * @return a term list with the constants obtained from the instantiation of variables
	 */
	// solo para literales sin constantes
	private static List<Term> inputParametersQuery(Bindings b, List<Term> literalArgs) {
		Variable var = null;
		List<Term> result = new ArrayList<Term>();
		for(Term t: literalArgs) {
			if (t instanceof Variable) {
				var = (Variable) t;
				if (b.containsVariable(var))
					result.add(b.getConstant(var));
				else
					result.add(t);				
			}
			else
				result.add(t);	
		}
		
		return result;
	}
	
	@Override
	public Coverage match(Coverage cr, Literal l) {
		Coverage result = new Coverage();
		Set<Bindings> coveredBindings = new HashSet<Bindings>();
		
		// Checking positive Bindings
		for (Bindings b : cr.getPositiveBindings()) {
			coveredBindings.addAll(match(b,l));
		}
		result.addPositiveBindingsSet(coveredBindings);
		
		coveredBindings.clear();
		// Checking positive Bindings
		for (Bindings b : cr.getNegativeBindings()) {
			coveredBindings.addAll(match(b,l));
		}
		result.addNegativeBindingsSet(coveredBindings);
		
		return result;
	}
	
	
	@Override
	public Set<Bindings> match(Bindings b, Literal l) {	
		Set<Bindings> result = new HashSet<Bindings>();
		String query = "";
		Literal aux;
		Query q;
		Hashtable<?,?> solution;
	
		aux = l.clone();
		aux.setArgs(inputParametersQuery(b, l.getArgs()));
		query = PrologUtilities.literalToPrologString(aux);
		q = new Query(query);
		while (q.hasMoreSolutions()) {
			solution = q.nextSolution();
			if (l.hasUnboundedVars())
				result.addAll(this.enlargingBinding(b, l, solution));
			else
				result.add(b.clone());
		}
		q.close();
		return result;
	}
	
	@Override
	public Coverage match(Coverage cr, Literal root, List<Literal> literalList) {
		Coverage result = new Coverage();
		Set<Bindings> coveredBindings = new HashSet<Bindings>();
		
		// Checking positive Bindings
		for (Bindings b : cr.getPositiveBindings()) {
			coveredBindings.addAll(match(b,root, literalList));
		}
		result.addPositiveBindingsSet(coveredBindings);
		
		coveredBindings.clear();
		// Checking positive Bindings
		for (Bindings b : cr.getNegativeBindings()) {
			coveredBindings.addAll(match(b,root, literalList));
		}
		result.addNegativeBindingsSet(coveredBindings);
		
		return result;
	}
	
	
	@Override
	public Set<Bindings> match(Bindings b, Literal root, List<Literal> literalList) {
		Set<Bindings> result = new HashSet<Bindings>();
		Hashtable<?,?> solution = null;
		Query q = null;
		StringBuffer query = new StringBuffer();
		String literal = null;
		Literal aux = null;
		
		for (Literal l : literalList) {
			aux = l.clone();
			aux.setArgs(inputParametersQuery(b, l.getArgs()));
			literal = PrologUtilities.literalToPrologString(aux);
			query.append(literal);
			query.append(",");
		}
		
		query = new StringBuffer((literalList.size() == 0 ? root.toString() : query.substring(0,query.lastIndexOf(","))));
		q = new Query(query.toString());
		while (q.hasMoreSolutions()) {
			solution = q.nextSolution();
			result.addAll(this.enlargingBinding(b, literalList, solution));
		}
		q.close();
		return result;
	}

	@Override
	public int nMatches(Bindings b, Literal l){
		int result = -1;
		Literal aux = null;
		Query q = null;
		jpl.Term finalCompound = null;
		 
		aux = l.clone();
		aux.setArgs(inputParametersQuery(b, l.getArgs()));
		finalCompound = PrologUtilities.literalToPrologCompound(aux);
		q = new Query (finalCompound);
	
		try {
			if (q.hasSolution()) {
				result = 0;
				while (q.hasMoreSolutions()){
					q.nextSolution();
					result++;
				}
			}
		}
		catch(PrologException e){}
		finally{
			q.close();
		}
	
		return result;
	}
	
	@Override
	public int nMatches(Bindings b, Node<Literal,InformationContext> root, List<Literal> body){
		int result = 0;
		Query q = null;
		Literal literalCopy;
		boolean goOn = true;
		
		jpl.Term[] bodyRule = new jpl.Term[body.size()];
		jpl.Term finalCompound = null;
		jpl.Term[] termList = null;
		
		int i = 0;
		for (Literal l: body) {
			literalCopy = l.clone();
			literalCopy.setArgs(inputParametersQuery(b, l.getArgs()));
			bodyRule[i] = PrologUtilities.literalToPrologCompound(literalCopy);
			i++;
		}

		if (bodyRule.length > 1) {
			finalCompound = bodyRule[bodyRule.length-1];
			for (int j = bodyRule.length - 2; j >= 0; j--) {
				termList = new jpl.Term[2];
				termList[0] = bodyRule[j];
				termList[1] = finalCompound;
				finalCompound = new jpl.Compound(",", termList);
			}
		}
		else if (bodyRule.length > 0)
			finalCompound = bodyRule[0];
		else {
//			literalCopy = root.clone();
//			literalCopy.setArgs(inputParametersQuery(b, literalCopy.getArgs()));
//			finalCompound = PrologUtilities.literalToPrologCompound(literalCopy);
			
			Coverage origHeadCov = root.getInformationContext().getOrigCoverage();
			result = origHeadCov.getPositiveBindings().size() + origHeadCov.getNegativeBindings().size();
			goOn = false;
		}
		
		if (goOn) {
			q = new jpl.Query(finalCompound);
			if (q.hasSolution()) {
//				Hashtable<?,?>[] solutions = q.allSolutions();
//				if (solutions != null && solutions.length > 0)
//					result = solutions.length;
				result = 0;
				while (q.hasMoreSolutions()) {
					result++;
					q.nextSolution();
				}
			}
			q.close();
		}
		return result;
	}

	@Override
	// tiene que pasarse siempre la cobertura original en cr
	// el nombre es confuso, en realidad este método lo que hace es no expandir bindings
	// devuelve los nowmatch
	public Coverage origmatch(Coverage cr, Literal l){
		Coverage result = new Coverage();
		Set<Bindings> coveredBindings = new HashSet<Bindings>();
		
		// Checking positive Bindings
		for (Bindings b : cr.getPositiveBindings()) {
			if (nMatches(b,l) > 0)
				coveredBindings.add(b);
		}
		result.addPositiveBindingsSet(coveredBindings);
		
		coveredBindings.clear();
		// Checking positive Bindings
		for (Bindings b : cr.getNegativeBindings()) {
			if (nMatches(b,l) > 0)
				coveredBindings.add(b);
		}
		result.addNegativeBindingsSet(coveredBindings);
		
		return result;
	}
	
	@Override
	// tiene que pasarse siempre la cobertura original en cr
	public Coverage origmatch(Coverage cr, Node<Literal,InformationContext> root, List<Literal> literalList){
		Coverage result = new Coverage();
		Set<Bindings> coveredBindings = new HashSet<Bindings>();
		
		// Checking positive Bindings
		for (Bindings b : cr.getPositiveBindings()) {
			if (nMatches(b,root, literalList) > 0)
				coveredBindings.add(b);
		}
		result.addPositiveBindingsSet(coveredBindings);
		
		coveredBindings.clear();
		// Checking positive Bindings
		for (Bindings b : cr.getNegativeBindings()) {
			if (nMatches(b,root, literalList) > 0)
				coveredBindings.add(b);
		}
		result.addNegativeBindingsSet(coveredBindings);
		
		return result;
	}
	
	/**
	 * Extends a bindings if the new literal has new variables
	 * @param currentBinding binding with the value of the existing variables in the literal
	 * @param literal literal to make the query
	 * @param solutions solutions of the previous query
	 * @return a set of bindings which are extended or not
	 */
	
	private Set<Bindings> enlargingBinding(Bindings b, Literal l, Hashtable<?,?> solution) {
		Set<Bindings> result = new HashSet<Bindings>();
		List<Term> existingArgs;
		Map<Term, Type> newVars = new HashMap<Term, Type>();
		List<Type> predicateTypes = null;
		predicateTypes = l.getPredicate().getArgsType();
		
		if (l.isPositive()) {
			predicateTypes = l.getPredicate().getArgsType();
			existingArgs = new ArrayList<Term>(l.getArgs());
			for (int i = 0; i < existingArgs.size(); i++) {
				Term t = existingArgs.get(i);
				if (t instanceof Variable && !b.getVariables().contains(t))
					newVars.put(t, predicateTypes.get(i));
			}
		}
		
		Bindings newBinding = null;
		
		newBinding = b.clone();
		Iterator<Entry<Term, Type>> it = newVars.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Term, Type> entry = it.next();
			Term t = entry.getKey();
			Type value = entry.getValue();
			
			String constantValue = "";
			if (solution.containsKey(t.getName())) {
				if (solution.get(t.getName()) instanceof Compound)
					constantValue = PrologUtilities.compoundToString((Compound) solution.get(t.getName()));
				else
					constantValue = solution.get(t.getName()).toString();
				
				Constant c = new Constant(constantValue, value);
				newBinding.addBinding((Variable) t, c);
			}
		}
		
		result.add(newBinding);
		
		return result;
	}
	
	
	private Set<Bindings> enlargingBinding(Bindings currentBinding,
			List<Literal> literalList, Hashtable<?,?> solution) {
		Set<Bindings> result = new HashSet<Bindings>();
		List<Term> existingArgs;
		Map<Term, Type> newVars = new HashMap<Term, Type>();
		List<Type> predicateTypes = null;
		
		for (Literal l : literalList) {
			if (l.isPositive() && !l.getPredicate().isPredefined()) {
				predicateTypes = l.getPredicate().getArgsType();
				existingArgs = new ArrayList<Term>(l.getArgs());
				for (int i = 0; i < existingArgs.size(); i++) {
					Term t = existingArgs.get(i);
					if (t instanceof Variable && !currentBinding.getVariables().contains(t))
						newVars.put(t, predicateTypes.get(i));
				}
			}
		}
		
		Bindings newBinding = null;
		
		newBinding = currentBinding.clone();
		Iterator<Entry<Term, Type>> it = newVars.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Term, Type> entry = it.next();
			Term t = entry.getKey();
			Type value = entry.getValue();
			
			String constantValue = "";
			if (solution.containsKey(t.getName())) {
				if (solution.get(t.getName()) instanceof Compound)
					constantValue = PrologUtilities.compoundToString((Compound) solution.get(t.getName()));
				else
					constantValue = solution.get(t.getName()).toString();
				
				Constant c = new Constant(constantValue, value);
				newBinding.addBinding((Variable) t, c);
			}
		}
		result.add(newBinding);
		
		return result;
	}

	
	@Override
	public int nMatches(Set<Bindings> bindingSet, Node<Literal,InformationContext> root, List<Literal> literalList) {
		int result = 0;
		for (Bindings b : bindingSet) {
			result += this.nMatches(b, root, literalList);
		}
		return result;
	}

	@Override
	// Comprueba que las variables nuevas están en sus posiciones, es decir,
	// cuando cogemos predicados definidos en prolog o definidos intensionalmente
	// hay variables que prolog no puede llegar a instanciar nunca (eso es lo que vamos a evitar)
	public boolean possibleLiteral(LiteralPair lp, Bindings b) {
		boolean result = true;
		Query q = null;
		Literal l = lp.getLiteral();
		List<Term> args = l.getArgs();
		Literal aux = l.clone();
		String query = null;
		jpl.Term t = null;
		
		aux.setArgs(inputParametersQuery(b, args));
		query = PrologUtilities.literalToPrologString(aux);
		
		q = new Query(query);
		if (q.hasSolution()){
			Hashtable<?, ?> solution = q.oneSolution();
			Iterator<?> it = solution.keySet().iterator();
			while (it.hasNext() && result) {
				t = (jpl.Term) solution.get(it.next());
				if (t.isVariable())
					result = false;
			}
		}
		q.close();
		return result;
	}
	
	// determina si una regla es esencial dentro de un conjunto de reglas
	public boolean essential(Set<Bindings> posBindings, Set<Bindings> restOfPosBindings) {
		boolean result = false;
		
		Iterator<Bindings> bindingsIt = posBindings.iterator();
		while (bindingsIt.hasNext() && !result) {
			Bindings b = bindingsIt.next();
			if (!belongs(b, restOfPosBindings))
				result = true;
		}
		return result;
	}

//	public Coverage matchRule(Literal root, List<Literal> literalList) {
//		Coverage result = new Coverage();
//		Set<Bindings> positiveBindings = new HashSet<Bindings>();
//		Set<Bindings> negativeBindings = new HashSet<Bindings>();
//		Bindings binding;
//		List<String> args = new ArrayList<String>();
//		List<String> body = new ArrayList<String>();
//		String head;
//		
//		Query q = null;	
//		jpl.Term v = new jpl.Variable("L");
//		String query;
//	
//		List<Term> rootArgs = root.getArgs();
//		List<Term> constants = new ArrayList<Term>();
//		for (int i = 0; i < rootArgs.size(); i++) {
//			Term t = rootArgs.get(i);
//			if (t instanceof Constant)
//				constants.add(t);
//			else if (t instanceof Variable)
//				args.add(rootArgs.get(i).getName());	
//		}
//		
//		for (int i = 0; i < literalList.size(); i++)
//			body.add(PrologUtilities.literalToPrologString(literalList.get(i)));
//		
//		// Getting positive Bindings
//		 head = PrologUtilities.literalToPrologString(root);
//		
//		String b = body.toString();
//		b = b.replace("[", "");
//		b = b.replace("]", "");
//		query = "findall(" + args.toString() + "," + "(" + head + (b.isEmpty() ? "":",") + b.toString() + ")," + v.name() + ")";
//		
//		q = new jpl.Query(query);
//		
//		String patri = "";
//		while(q.hasMoreSolutions()) {
//			Hashtable solution = q.nextSolution();
//			if (solution.get(v.name()) instanceof Compound)
//				patri = PrologUtilities.compoundToString((Compound) solution.get(v.name()));
//			else
//				patri = solution.get(v.name()).toString();
//			if (!patri.isEmpty()) {
//				binding = new Bindings();
//			}
//		}	
//		return result;
//	}

	@Override
	public Coverage branchOrigMatch(Coverage cr, Literal root,
			List<Literal> literalList) {
		Coverage result = new Coverage();
		Set<Bindings> coveredBindings = new HashSet<Bindings>();

		// Checking positive Bindings
		for (Bindings b : cr.getPositiveBindings()) {
			if (branchMatch(b,root, literalList))
				coveredBindings.add(b);
		}
		result.addPositiveBindingsSet(coveredBindings);
		
		coveredBindings.clear();
		// Checking positive Bindings
		for (Bindings b : cr.getNegativeBindings()) {
			if (branchMatch(b,root, literalList))
				coveredBindings.add(b);
		}
		result.addNegativeBindingsSet(coveredBindings);
		
		return result;
	}
	
	private boolean branchMatch(Bindings b, Literal root, List<Literal> body){
		boolean result = false;
		Query q = null;
		StringBuffer head = new StringBuffer();
		StringBuffer bodyString = new StringBuffer();
		
		head = new StringBuffer(root.toString() + ",");
		if (body.size() > 0) {
			for (Literal literal : body) {
				Literal l = literal.clone();
				l.setArgs(inputParametersQuery(b, literal.getArgs()));
				bodyString.append(PrologUtilities.literalToPrologString(l) + ",");
			}
			bodyString = bodyString.deleteCharAt(bodyString.lastIndexOf(","));
			q = new Query(bodyString.toString());
		}
		else 
			q = new Query(head.toString());
		
		if (q.hasSolution()) {
			result = true;
			List<Term> rootArgs = root.getArgs();
			List<Term> terms = new ArrayList<Term>(b.getVariables());
			Collections.sort(terms);
			for (int i = 0; i < terms.size() && result; i++) {
				Term arg = rootArgs.get(i);
				if (arg instanceof Constant) {
					if (!b.getConstant(terms.get(i)).equals(rootArgs.get(i)))
						result = false;
				}
				else if (arg instanceof Variable)
					if (!terms.get(i).equals(rootArgs.get(i)))
						result = false;
			}
		}
		q.close();
		return result;
	}
}