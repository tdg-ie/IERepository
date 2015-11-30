package data;

import java.util.List;

public abstract class Term implements Comparable<Term>{
	
	protected String name;
	protected String value;
	protected Type termType;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Type getType() {
		return termType;
	}
	
	/**
	 * Checks if two terms has the same type
	 * @param t term to compare
	 * @return true if the terms have the same type, false otherwise
	 */
	public boolean sameType(Term t){
		boolean result = false;
		if (this.termType.getTypeName().equals(t.getType().getTypeName()))
			result = true;
		return result;
	}
	
	/**
	 * Check if this term has the type which is passed as parameter 
	 * @param type to compare
	 * @return true if the type of this term is the same that the one passed as parameter, 
	 * false otherwise
	 */
	public boolean hasType(Type type){
		boolean result = false;
		if (this.termType.getTypeName().equals(type.getTypeName()))
			result = true;
		return result;
	}
	
	/**
	 * Checks wether a term has a type or is unbound
	 * @return true if the term has a type (bounded term), false otherwise
	 */
	public boolean isBound() {
		return this.termType.hasType();
	}
	
	/**
	 * Compares wether two terms have the same name
	 * @param t term to compare
	 * @return true if the terms are the same (equals), false otherwise
	 */
	public int sameName(Term t) {
		int result = 0;
		result = t.getName().compareTo(this.getName());
		return result;
	}
	
	/**
	 * Checks wether a term is contained in a list of terms
	 * @param termList list of terms where we search for
	 * @return the index where this term appears in the list of terms passed as parameter
	 */
	public int belongs(List<? extends Term> termList){
		int result = -1;
		Term t = null;
		int i = 0;
		while ((result == -1) && i < termList.size()) {
			t = termList.get(i);
			if (this.equals(t))
				result = i;
			i++;
		}
		return result;
	}
	/**
	 * Checks if two terms are equals. Two terms are equals if they have the 
	 * same type and if they has the same value (for constants) or name (for variables)  
	 * @param t term to compare
	 * @return true if the terms are equals, false otherwise
	 */
	public abstract boolean equals(Term t);
	
	/**
	 * Bound a term
	 * @param type type to bound the term
	 */
	public abstract void setType(Type type);
	
	public abstract int compareTo(Term t);
}