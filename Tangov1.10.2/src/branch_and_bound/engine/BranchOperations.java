package branch_and_bound.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;

import data.Literal;
import data.Term;
import data.Variable;

public class BranchOperations {

	/**
	 * Returns the number of determinate literals that the rule contains.
	 */
	public static int getDetLitNumber(List<Node<Literal, InformationContext>> path) {
		int result = 0;
		for(Node<Literal, InformationContext> n: path) {
			if (n.getInformationContext().isDeterminate())
				result++;
		}
		return result;
	}
	
	/**
	 * Returns the number of new vars that the current path contains.
	 */
	public static int getNumberOfVars(List<Node<Literal, InformationContext>> branch) {
		int result = 0;
		Literal l;
		Literal headLiteral;
		List<Term> terms;
	
		if (branch.size() > 0) {
			headLiteral = branch.get(0).getParent().getDataNode();
			terms = new ArrayList<Term>(headLiteral.getArgs());
			
			for(Node<Literal, InformationContext> n: branch) {
				l = n.getDataNode();
					for(Term t: l.getArgs()) {
						if (t instanceof Variable && t.belongs(terms) < 0)
							terms.add(t);
					}
			}
			result = terms.size();
		}
		return result;
	}
	
	/**
	 * Gets the set of new variables added by the last literal in the rule. 
	 * They are variables that do not belong to the list of arguments of the previous literals 
	 * in the body of the rule and the head literal.
	 * @return a set with all last new variables
	 */
	public static Set<Term> getLastNewVariables(Node<Literal,InformationContext> head, List<Node<Literal, InformationContext>> body) {
		Set<Term> result = new HashSet<Term>();
		Set<Term> existingArgs = new HashSet<Term>();
		
		if (body.size() > 0) {
			existingArgs.addAll(head.getDataNode().getArgs());
			for (int i = 0; i < body.size()-1; i++) {
				Literal l = body.get(i).getDataNode();
				for(Term t: l.getArgs()) {
					if (t instanceof Variable && !existingArgs.contains(t))
						existingArgs.add(t);
				}
			}
			
			Literal l = body.get(body.size()-1).getDataNode();
			for(Term t: l.getArgs()) {
				if ( t instanceof Variable && !existingArgs.contains(t))
					result.add(t);
			}
		}
		
		return result;
	}
	
	public static String pathToString(Node<Literal, InformationContext> head, List<Node<Literal, InformationContext>> body) {
		StringBuffer result = new StringBuffer();
		result.append(head.getDataNode().toString() + ":-");
		for (Node<Literal, InformationContext> n : body) {
			result.append(n.getDataNode().toString());
			result.append(",");
		}
		char lastChar = result.charAt(result.length()-1);
		if (lastChar == ',')
			result = new StringBuffer(result.substring(0, result.lastIndexOf(",")) + ".");
		else if (lastChar == '-')
			result = new StringBuffer(result.substring(0, result.lastIndexOf(":-")) + ".");
		
		return result.toString();
	}
	
	public static Set<Term> getVarsInPath(Node<Literal,InformationContext> leaf) {
		Set<Term> result = new HashSet<Term>();
		Node<Literal,InformationContext> n = leaf;
		while(n != null) {
			for (Term term : n.getDataNode().getArgs()) {
				if (term instanceof Variable)
					result.add(term);
			}
			n = n.getParent();
		}
		return result;
	}

	public static int getMaxVarIndex(
			List<Node<Literal, InformationContext>> currentPath) {
		int result = 0;
		Literal l;
	
		for(Node<Literal, InformationContext> n: currentPath) {
			l = n.getDataNode();
			int index = l.getMaxVarIndex();
			if (result < index)
				result = index;
		}
		
		return result;
	}
}
