package data;

import java.util.ArrayList;
import java.util.List;

import utilities.Numbers;

public class Literal implements Cloneable{
	
	private Predicate predicate;
	private List<Term> args = null;
	private Sign sign;

	public Literal(){
		this.sign = Sign.POSITIVE;
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	public List<Term> getArgs() {
		return args;
	}
	
	public List<Term> getVars() {
		List<Term> result = new ArrayList<Term>();
		for (Term t : this.args) {
			if (t instanceof Variable)
				result.add(t);
		}
		return result;
	}
	
	public void setArgs(List<Term> args) {
		this.args = args;
//		if (!incompatibleArgs(this.predicate,args))
//			this.args = args;
	}
	
	/**
	 * Gets the number of arguments of the literal
	 * @return the number of arguments of the literal
	 */
	public int size(){
		return args.size();
	}
	
	/**
	 * Checks wether the literal is unnegated
	 * @return true if the literal is unnegated, false otherwise
	 */
	public boolean isPositive() {
		return (sign.equals(Sign.POSITIVE));
	}
	
	/**
	 * Checks wether the literal is negated
	 * @return true if the literal is negated, false otherwise
	 */
	public boolean isNegated(){
		return (sign.equals(Sign.NEGATIVE));
	}

	public void setSign(Sign sign) {
		this.sign = sign;
	}
	
	/**
	 * Checks if the literals contains variables that do not have type (unbounded variables)
	 * @return true if the literal contains new unbounded variables, false otherwise
	 */
	public boolean hasUnboundedVars() {
		boolean result = false;
		
		for (int i = 0; i < this.args.size() && !result; i++) {
			if (!this.args.get(i).getType().hasType())
				result = true;
		}
		return result;
	}
	
	/**
	 * Gets a list with all variables that do not have type
	 * @return a list of unbounded variables
	 */
	public List<Term> getUnboundedVars() {
		List<Term> result = new ArrayList<Term>();
		for(Term t: args) {
			if (!t.getType().hasType())
				result.add(t);
		}
		return result;
	}
	
	
	/**
	 * Checks if the literals contains some constant
	 * @return true if the literal contains some constant, false otherwise
	 */
	public boolean hasConstants() {
		boolean result = false;
		
		for (int i = 0; i < this.args.size() && !result; i++) {
			if (this.args.get(i) instanceof Constant)
				result = true;
		}
		return result;
	}
	
	/**
	 * Checks if the literals contains new vars (unbounded vars)
	 * @return true if the literal contains new vars, false otherwise
	 */
//	public boolean hasNewVars() {
//		boolean result = false;
//		
//		for (int i = 0; i < this.args.size() && !result; i++) {
//			if (!this.args.get(i).getType().hasType())
//				result = true;
//		}
//		return result;
//	}
	
	/**
	 * Gets a list with all new variables. A variable is new if it does not have type
	 * @return a list of unbounded variables
	 */
//	public List<Term> getNewVars() {
//		List<Term> result = new ArrayList<Term>();
//		for(Term t: args) {
//			if (!t.getType().hasType())
//				result.add(t);
//		}
//		return result;
//	}
	
	/**
	 * Gets the last variable
	 * @return a list of unbounded vars
	 */
	public int getMaxVarIndex() {
		int result = 0;
		Term t = null, term = null;
		String maxString = null, string = null, name = null;
		
		int i = 0;
		while (!(this.args.get(i) instanceof Variable)) i++;
		if (i < this.args.size()) {
			t = this.args.get(i);
			name = t.getName();
			maxString = name.substring(name.indexOf(name.substring(0, 1))+1);
			result = Integer.parseInt(maxString);
		}
			
		for (int j = i+1; j < args.size(); j++) {
			term = args.get(j);
			if (term instanceof Variable) {
				string = term.getName().substring(term.getName().indexOf(term.getName().substring(0, 1))+1); 
				if (string.compareTo(maxString) > 0) {
					maxString = string;
					result = Integer.parseInt(maxString);
				}
			}
		}
		return result;
	}
	
	/**
	 * Makes a literal with all its data (predicate, arguments and sign)
	 * @param p predicate of the literal
	 * @param args arguments of the literal
	 * @param sign sign of the literal
	 * @return true if the literal has been successfully made correctly, false otherwise
	 */
	public boolean makeLiteral(Predicate p, List<Term> args, Sign sign) {
		boolean result = false;
		if (args.size() == p.getArity())
			if (result = !incompatibleArgs(p,args)) {
				this.predicate = p;
				this.args = args;
				this.sign = sign;
			}
		return result;
	}
	
	/**
	 * Checks if the arguments indicated as parameter are compatible (have the same type) 
	 * with the arguments of the predicate indicated as parameter
	 * @param p predicate that contains the list of arguments to compare
	 * @param arguments list of terms to compare
	 * @return true if the arguments are incompatible, false otherwise
	 */
	private boolean incompatibleArgs(Predicate p, List<Term> arguments) {
		boolean result = false;
		boolean containAnyVar = false;
		boolean ok = false;
		Term arg = null;
		List<Type> typeList = p.getArgsType();
		List<Term> newVariables = new ArrayList<Term>();
		
		
		if (p != null) {
			if (!p.isPredefined()) {
				containAnyVar = true;
				if (typeList.size() == arguments.size()) { //if the list of arguments has the same arity, go ahead
					int i = 0;
					//checking the arguments
					while (i < arguments.size()) { 
						arg = arguments.get(i);
						if (arg.isBound() && !(arg.hasType(typeList.get(i)))) //= arg.getType().equals(typeList.get(i));
							result = true;
						else if (!arg.getType().hasType()) 
							//a literal of type predicate(New_0,New_0, X1) is not allowed
							if (newVariables.contains(arg))
								result = true;
							else
								newVariables.add(arg);
						i++;
					}
					//checking the instantiations of the predicate p
					List<int[]> instatiations = p.getInstantiations();
					for (i = 0; i < instatiations.size() && !ok; i++) {
						ok = true;
						int[] js = instatiations.get(i);
						
						for (int j = 0; j < js.length && ok; j++) {
							if (js[j] == 1)
								if (!arguments.get(j).isBound())
									ok = false;
						}
					}
					if (!ok)
						result = true;
				}
			}
			else { //cheking predefined predicates. They always have two arguments
				Term t1 = arguments.get(0);
				Term t2 = arguments.get(1);
				String typeName1 = t1.getType().getTypeName();
				String typeName2 = t2.getType().getTypeName();
				
				if (t1 instanceof Variable)
					containAnyVar = true;

				if (!p.getName().equals("EQUAL")) { //for predicates that are not equal and plus // && !p.getName().equals("plus")
					if (!(typeName1.equals(typeName2)))
						result = true;
					else if (t1 instanceof Variable && t2 instanceof Variable && !(Numbers.isNumber(typeName1) || Numbers.isNumber(typeName2)))
						result = true;
					else if (!Numbers.isNumber(typeName1) && (t2 instanceof Constant) && !Numbers.isNumber(t2.value)){
							result = true; 
					}
				}
				if (!result) {
					for (int i=1; i < arguments.size(); i++) {
						if (arguments.get(i) instanceof Variable)
							containAnyVar = true;
						if (!arguments.get(i).sameType(t1))
							result = true;
					}
				}
			}
		}
		if (newVariables.size() == typeList.size())
			result = true;
		
		if (!containAnyVar)
			result = true;
		return result;
	}
	
	/**
	 * prints the literal on the screen
	 */
	public void printScreen() {
		String s = this.toString();
		System.out.print(s);
	}
	
	/**
	 * gets the literal as a String
	 * @return the string with the data of the literal
	 */
	public String toString() {
		
		StringBuffer result = new StringBuffer();
		result.append(this.isNegated() ? "!" :"");
		result.append(this.predicate.getName() + "(");
		
		for (Term t : this.args) {
			if (t instanceof Variable)
				result.append(t.getName() + ",");
			else if (t instanceof Constant)
				result.append(t.getValue() + ",");
		}
		result.deleteCharAt(result.lastIndexOf(","));
		result.append(")");
		
		return result.toString();
	} 
	
	/**
	 * Checks if two literals are equals. Two literals are equals if both have the same
	 * predicate, same sign and the same argument list
	 * @param l literal to compare
	 * @return true if both literals are equals, false otherwise
	 */
	public boolean equals(Literal l) {
		boolean result = false;
		boolean end = false;
		int i = 0;
		List<Term> litArgs = l.getArgs();
		if (this.size() == litArgs.size()) {
			while (i < this.args.size() && !end){
				if (!this.args.get(i).equals(litArgs.get(i)))
					end = true;
				i++;
			}
			if (!end) {
				if (this.predicate.equals(l.getPredicate()) && this.sign.equals(l.sign))
					result = true;
			}
		}
		return result;
	}
	
	public Literal clone()
    {
        Literal clone = new Literal();
        // Deep clone
        
    	clone.setPredicate(this.predicate.clone());
    	clone.setArgs(new ArrayList<Term>(this.args));
    	clone.setSign(sign);
        
        return clone;
    }
}

/**
 * Gets the number of constants that contains the literal
 * @return the number of constants
 */
//public int getNumberOfConstants() {
//	int result = 0;
//	for(Term t: args) {
//		if (t instanceof Constant)
//			result++;
//	}
//	return result;
//}

/**
 * Checks if the literal contains a specific term in its arguments list
 * @param term to search for
 * @return true if the literal contains the term as argument, false otherwise
 */
//public boolean contains(Term term){
//	boolean result = false;
//	for (int i = 0; i < this.args.size() && !result; i++) {
//		if (this.args.get(i).equals(term))
//			result = true;
//	}
//	return result;
//}