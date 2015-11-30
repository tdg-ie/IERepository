package knowledgeBase;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.FoilException;
import data.Constant;
import data.Literal;
import data.Predicate;
import data.Term;
import data.Variable;
import jpl.Atom;
import jpl.Compound;
import jpl.Query;
import jpl.Util;

public class PrologUtilities {
	
	/**
	 * Transforms a compound object to a string object
	 * @param term to transform
	 * @return the String with the value of the compound
	 */
	public static String compoundToString(jpl.Term term) {
		StringBuffer result = new StringBuffer();
		String list = "";
		jpl.Term data = null;
		result.append(term.toString());
		
		//checking if the compound object is a list
		if (term.name().equals(".")){
			if (term.arity() > 0) {
				data = term.arg(term.arity());
	//			try {data = term.arg(term.arity());}
	//			catch(Exception e){
	//				System.out.println(term.toString());
	//				System.out.println(term.arity());
	//			}
				if (data.isAtom()) {
					list = term.arg(term.arity()).toString();
					result = new StringBuffer(list);
				}
				else if (data.isCompound()) {
					result = new StringBuffer();
					result.append(compoundToString((Compound) data));
					result.insert(1, ",");
				}
				
				for (int z = 1; z < term.arity(); z++) {
					jpl.Term compoundArg = term.arg(z);
					//check all types of a compound object
					if (compoundArg.isInteger())
						result.insert(1, compoundArg.intValue());
					else if (term.arg(z).isFloat())
						result.insert(1, compoundArg.floatValue());
					else if (term.arg(z).isAtom())
						result.insert(1, compoundArg.toString());
					// there is more types but so is enough at the moment
				}
			}
		}
		else if (term.name().equals("[]"))
			result = new StringBuffer("[]");
		
		return result.toString();
	}
	
	/**
	 * Transforms a string into another different if it contains lists
	 * in order to write in the prolog knowledge base in the right way
	 * @param string to transform
	 * @return the String in prolog format
	 */
	public static String processString(String string) {
		String result = new String(string);
		String regex = "\\'\\.\\'\\(\\S*\\,\\s\\[\\]\\)";
		
		while (result.matches(".*" + regex + ".*")) {
//			Pattern pattern = Pattern.compile("[\\'\\.\\'\\(.*,\\[\\]\\)]");
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(result);
			if (matcher.find()) {
				String list = matcher.group();
				Pattern p = Pattern.compile("\\(.*\\,");
				Matcher firstArgMatcher = p.matcher(list);
				if (firstArgMatcher.find()) {
					String arg = list.substring(firstArgMatcher.start()+1, firstArgMatcher.end()-1);
					result = result.replaceFirst(regex, "[" + arg + "]");
				}
			}
		}
		return result;
	}
	
	
	public static String literalToPrologString(Literal l) {
		String result = "";
		String pTargetName = "";
		
		result = l.toString();
		if (result.startsWith("!")) {
			result = result.replace("!", "not(");
			result += ")";
		}
	
		if (l.getPredicate().isPredefined()) {
			String pName = l.getPredicate().getName();
			if (pName.toLowerCase().equals("equal"))
				pTargetName = "=";
			else if (pName.toLowerCase().equals("le"))
				pTargetName = "=<";
			else if (pName.toLowerCase().equals("lt"))
				pTargetName = "<";
			else if (pName.toLowerCase().equals("ge"))
				pTargetName = ">=";
			else if (pName.toLowerCase().equals("gt"))
				pTargetName = ">";
			
			result = result.replace(pName, pTargetName);
		}
		return result;
	}
	
	
	public static jpl.Compound literalToPrologCompound(Literal l) {
		jpl.Compound result = null;
		Predicate p = l.getPredicate();
		String pTargetName = p.getName();
		jpl.Term[] arguments = null;
	
		if (l.getPredicate().isPredefined()) {
			if (pTargetName.toLowerCase().equals("equal"))
				pTargetName = "=";
			else if (pTargetName.toLowerCase().equals("le"))
				pTargetName = "=<";
			else if (pTargetName.toLowerCase().equals("lt"))
				pTargetName = "<";
			else if (pTargetName.toLowerCase().equals("ge"))
				pTargetName = ">=";
			else if (pTargetName.toLowerCase().equals("gt"))
				pTargetName = ">";
		
		}
		
		arguments = new jpl.Term[l.getArgs().size()];
		for (int j = 0; j < l.getArgs().size(); j++) {
			jpl.Term v = null;
			Term arg = l.getArgs().get(j);
		
//			if (arg instanceof Variable) {
//				if (!p.isPredefined())
//					v = new jpl.Variable(l.isPositive() ? arg.getName() : "_"); // no s� si deber�amos asignar un n�mero (_1)
//				else 
//					v = new jpl.Variable(arg.getName());
//			}
			if (arg instanceof Variable) 
				v = new jpl.Variable(arg.getName());
			else
				v = Util.textToTerm(arg.getValue());
			
			arguments[j] = v;
		}
		
		result = new jpl.Compound(pTargetName, arguments);
		if (l.isNegated()){
			jpl.Term[] args = new jpl.Term[1];
			args[0] = result;
			result = new jpl.Compound("not", args);
		}
		
		return result;
	}
	
	public static Query query(String s) {
	
		Query result = null, garbage_collect = null;
		garbage_collect = new Query("garbage_collect");
		if (garbage_collect.hasSolution())
			garbage_collect.close();
		
		result = new Query(s);
		return result;
	}
	
	/**
	 * Generates a jpl.Query from a parameters list and a predicate
	 * @param parameters list of parameters to bound the query
	 * @param predicateName the name of the predicate of the query
	 * @return a jpl.Query ready to execute
	 */
	public static Query query(List<Term> parameters, Predicate p , boolean isNegated) {
	
		Query result = null, garbage_collect = null;
		Term parameter = null;
		jpl.Term[] term = null;
		String pName = null;
		
		term = new jpl.Term[parameters.size()];
		pName = p.getName();
		
		// instantiating the parameters of the query
		for (int i = 0; i < parameters.size(); i++) {
			parameter = parameters.get(i);			
			if (parameter instanceof Constant)
				term[i] = Util.textToTerm(parameter.getValue());
			else if (parameter instanceof Variable)
				term[i] = new jpl.Variable(parameter.getName());
		}
		
		// translating our predefined predicates to swi-prolog nomenclature
		if (p.isPredefined()) {
			if (pName.toLowerCase().equals("equal"))
				pName = "=";
			else if (pName.toLowerCase().equals("le"))
				pName = "=<";
			else if (pName.toLowerCase().equals("lt"))
				pName = "<";
			else if (pName.toLowerCase().equals("ge"))
				pName = ">=";
			else if (pName.toLowerCase().equals("gt"))
				pName = ">";
		}
		
		if (isNegated)
			result = new Query(Util.textToTerm("\\+(" + new Query(pName, term) +")"));
		else 
			result = new Query(pName, term);
	
		term = null;
		garbage_collect = new Query("garbage_collect");
		if (garbage_collect.hasSolution())
			garbage_collect.close();
		return result;
	}
	
	/**
	 * Call to the prolog garbage collector
	 */
	public static void garbage_collector(){
		
		Query garbage_collect = new Query("garbage_collect");
		
		if (!garbage_collect.hasSolution())
			try {
				throw new FoilException("garbage collect call failed");
			} catch (FoilException e) {
				e.printStackTrace();
			}
			
		garbage_collect.close();
	}
	
	public static boolean exists_Predicate(String fileName, String predicateName, jpl.Term[] vars) {
		boolean result = false;
		Query query = null;
	
		jpl.Compound compound = new jpl.Compound(predicateName, vars);
//		jpl.Atom atom = new Atom(fileName); // for MAC
		jpl.Atom atom = new Atom(fileName.toLowerCase());
		jpl.Term[] args = new jpl.Term[]{compound,atom};
		query = new Query("source_file", args);
		if (query.hasSolution()) {
			result = true;
		}
		query.close();
		return result;
	}
	
	// Find out if a predicate is defined by a set of rules (intensionally) or by a set of facts (extensionally).
	public static boolean isFact(String predicateName, int predicateArity, String fileName) {
		boolean result = true, end = false;
		jpl.Term[] args = null;
		jpl.Term[] terms = null;
		int i = 0;

		terms = new jpl.Variable[predicateArity];
		args = new jpl.Term[2];
		i = 0;
		while (i < predicateArity) {
			jpl.Variable X = new jpl.Variable("New" + i);
			terms[i] = X;
			i++;
		}
		args[0] = new Compound(predicateName, terms);
		args[1] = new jpl.Variable("Z");
		
		jpl.Term atom = new Atom(fileName.toLowerCase());
//		jpl.Term atom = new Atom(fileName);  // for MAC
		jpl.Term[] args_sourcefile = new jpl.Term[]{args[0],atom};
		Query sourcefile = new Query("source_file", args_sourcefile);
		if (sourcefile.hasSolution()) {
			Query rule_or_fact = new Query("clause", args);
			if (rule_or_fact.hasSolution()) {
				Hashtable<?,?>[] table = rule_or_fact.allSolutions();
				for (int j = 0; j < table.length && !end; j++) {
					jpl.Term isFact = (jpl.Term) table[j].get("Z");
					if (!isFact.isAtom() || (isFact.isAtom() && !isFact.name().equals("true"))) {
						result = false;
						end = true;
					}
				}
			}
			rule_or_fact.close();
		}
		else
			result = false;
		
		sourcefile.close();
		return result;
	}
	
	public static Query setofQuery (Predicate p, Set<Integer> existingVarsPositions) {
		Query result = null;
		String predicateName = p.getName();
		int arity = p.getArity();
		jpl.Term[] predicateVars = new jpl.Term[arity];
		jpl.Term[] termList = null;
		Set<jpl.Term> fixedVars = new HashSet<jpl.Term>();
		jpl.Term predicateCompound = null, equalPredicate = null;
		jpl.Term firstArg = null, secondArg = null;
		
		for (int i = 0; i < arity; i++) {
			jpl.Variable v = new jpl.Variable("X" + i);
			predicateVars[i] = v;
			
			if (existingVarsPositions.contains(i))
				fixedVars.add(v);
		}
		
		// predicate to check
		predicateCompound = new jpl.Compound(predicateName, predicateVars);
				
		// equal predicate
		termList = new jpl.Term[2];
		Iterator<jpl.Term> it = fixedVars.iterator();
		if (it.hasNext())
			termList[0] = firstArg = it.next();
		if (it.hasNext())
			termList[1] = it.next();
		equalPredicate = new jpl.Compound("==", termList);
		
		// setof query
		
		termList = new jpl.Term[2];
		termList[0] = predicateCompound;
		termList[1] = equalPredicate;
		secondArg = new jpl.Compound(",", termList);
				
		termList = new jpl.Term[2];
		
		for (int i = 0; i < predicateVars.length; i++) {
			if (!existingVarsPositions.contains(i)) {
				termList[1] = secondArg;
				termList[0] = predicateVars[i];
				secondArg = new jpl.Compound("^", termList);
				termList = new jpl.Term[2];
			}		
		}
				
		termList = new jpl.Term[3];
		termList[0] = firstArg;
		termList[1] = secondArg;
		termList[2] = new jpl.Variable("L"); // result
		result = new jpl.Query("setof", termList);
						
		return result;
	}
	
	
	/**
	 * Generates a jpl.Query from a parameters list and a predicate
	 * @param parameters list of parameters to bound the query
	 * @param predicateName the name of the predicate of the query
	 * @return a jpl.Query ready to execute
	 */
	/*
	public static Query query(PrologRule r) {
		
		Query result = null, garbage_collect = null;
		jpl.Term[] partialQuery = new jpl.Term[r.getBody().size()];
		garbage_collect = new Query("garbage_collect");
		if (garbage_collect.hasSolution())
			garbage_collect.close();
		
		Predicate p = null;
		Literal l = null;
		List<Term> args = null;
		jpl.Term[] term = null;
		Term parameter = null;
		String pName = null;
		
		int index = 0;
		
		for (Node<Literal, InformationContext> n : r.getBody()) {
			l = n.getDataNode();
			p = l.getPredicate();
			pName = p.getName();
			args = l.getArgs();
			term = new jpl.Term[p.getArity()];
			
			// instantiating the parameters of the query
			for (int i = 0; i < args.size(); i++) {
				parameter = args.get(i);			
				if (parameter instanceof Constant)
					term[i] = Util.textToTerm(parameter.getValue());
				else if (parameter instanceof Variable)
					term[i] = new jpl.Variable(parameter.getName());
			}
			
			// translating our predefined predicates to swi-prolog nomenclature
			if (p.isPredefined()) {
				if (pName.toLowerCase().equals("equal"))
					pName = "=";
				else if (pName.toLowerCase().equals("le"))
					pName = "=<";
				else if (pName.toLowerCase().equals("lt"))
					pName = "<";
				else if (pName.toLowerCase().equals("ge"))
					pName = ">=";
				else if (pName.toLowerCase().equals("gt"))
					pName = ">";
			}
			
			if (l.isNegated())
				partialQuery[index] = Util.textToTerm("\\+(" + new Query(pName, term) +")");
			else 
				partialQuery[index] = new jpl.Compound(pName, term);
			
			index++;
		}
		
		jpl.Term termino = Util.termArrayToList(partialQuery);
		result = new Query(termino);
		System.out.print(result);
		return result;
	} */
}