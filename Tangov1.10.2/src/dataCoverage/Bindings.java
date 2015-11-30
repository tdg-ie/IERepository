package dataCoverage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import data.Constant;
import data.Term;
import data.Variable;

public class Bindings implements Cloneable {
	
	private Map<Variable, Constant> bindings; 
	
	public Bindings(){
		this.bindings = new HashMap<Variable, Constant>();
	}

	public Map<Variable, Constant> getBindings() {
		return Collections.unmodifiableMap(this.bindings);
	}
	
	public void setBindings(Map<Variable, Constant> bindings) {
		this.bindings = bindings;
	}

	/**
	 * Checks if the bindings contains a bindings subset indicated as parameter
	 * @param subSet set of bindings
	 * @return true if the subSet belongs to the bindings, false otherwise 
	 */
	public boolean containsBindings(Map<Variable, Constant> subSet) {
		boolean result = true;
		Iterator<Entry<Variable, Constant>> iterator = null;
		Entry<Variable, Constant> pair = null;

		iterator = subSet .entrySet().iterator();
		while( iterator. hasNext() && result){
			pair = iterator.next();
			Variable v = pair.getKey();
			Constant c = pair.getValue();
			if (!(this.bindings.containsKey(v) && (this.bindings.get(v).equals(c))))
				result = false;
		}
		
		return result;
	}
	
	/**
	 * searches for the value of a specific variable
	 * @param t variable to search for 
	 * @return the constant assigned to the variable t
	 */
	public Constant getConstant(Term t){
		Constant result = null;
		if (this.bindings.containsKey(t))
				result = this.bindings.get(t);
		return result;
	}
	
	/**
	 * Gets all variables contained in the binding 
	 * @return the set of variables belonging to the binding
	 */
	public Set<Variable> getVariables(){
		return Collections.unmodifiableSet(this.bindings.keySet());
	}
	
	/**
	 * Checks if a specific variable exists within the bindings
	 * @param key variable to search for in the bindings
	 * @return true if the variable exists, false otherwise
	 */
	public boolean containsVariable(Variable key) {
		boolean result = false;
		if (this.bindings.containsKey(key))
				result = true;
		return result;
	}
	
	/**
	 * Adds a binding to the bindings
	 * @param var variable to include in the bindings
	 * @param constant constant to include in the bindings
	 */
	public void addBinding(Variable var, Constant constant){
		if (var.getType().equals(constant.getType()) || !var.isBound())
			this.bindings.put(var, constant);
	}
	
	/**
	 * @return number of existing bindings
	 */
	public int size (){
		return bindings.size();
	}
	
	/**
	 * Check if two bindings are equals. Two bindings are equals if they have the same size
	 * and they contain the same variables and the each variable has assigned the same constant. 
	 * @param b binding to compare
	 * @return true, if the bindings are equals, false otherwise
	 */
	
	public boolean equals(Bindings b) {
		boolean result = true;
		Iterator<Variable> varIt = b.bindings.keySet().iterator();;
		Variable v = null;
		
		//the bindings have to have the same size to be equals
		if (this.bindings.size() == b.bindings.size()) {
			while (result && varIt.hasNext()) {
				v = varIt.next();
				if (!(this.bindings.containsKey(v) && this.bindings.get(v).equals(b.bindings.get(v))))
					result = false;
			}
		}
		else 
			result = false;
		
		return result;
	}
	
	
	public boolean equals (Object obj) {
		boolean result = true;
		Variable v = null;
		Iterator<Variable> varIt = null;
		
		if (obj instanceof Bindings) {
			Bindings b = (Bindings) obj;
			if (this.bindings.size() == b.bindings.size()) {
				varIt = b.bindings.keySet().iterator();
				while (result && varIt.hasNext()) {
					v = varIt.next();
					if (!(this.bindings.containsKey(v) && this.bindings.get(v).equals(b.bindings.get(v))))
						result = false;
				}
			}
			else 
				result = false;
		}
		return result;
	}



	/**
	 * Gets the all bindings as a string
	 * @return a string with all data of these bindings
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<");
		for (Variable v : bindings.keySet()) {
			result.append(v.getName() + " = " + bindings.get(v).getValue());
			result.append(",");
		}
		result.deleteCharAt(result.lastIndexOf(","));
		result.append(">");
//		result.append(">" + "\n");
		return result.toString();
	}
	
	@Override
	public Bindings clone() {
		Bindings result = new Bindings();
		result.setBindings(new HashMap<Variable, Constant>(this.bindings));
		return result;
	}
	
}


///**
//* Clone a binding
//* @param b bindings to clone
//*/
//public void cloneBinding(Bindings b) {
//	Iterator<Entry<Variable, Constant>> it = null;
//	Entry<Variable, Constant> mapEntry = null;
//	
//	it = b.bindings.entrySet().iterator();
//	while (it.hasNext()) {
//		mapEntry = it.next();
//		Variable key = mapEntry.getKey();
//		Constant value = mapEntry.getValue();
//		this.bindings.put(key, value);
//	}
//}

//@Override
//public Bindings clone() {
//	Bindings result = new Bindings();
//	Map<Variable, Constant> bindings = null;
//Iterator<Entry<Variable, Constant>> it = null;
//Entry<Variable, Constant> mapEntry = null;
//	
//	// opción 2
//	bindings = new HashMap<Variable, Constant>();
//	bindings.putAll(this.bindings);
	
//	//opción 3
//	bindings = new HashMap<Variable, Constant>();
//	it = this.bindings.entrySet().iterator();
//	while (it.hasNext()) {
//		mapEntry = it.next();
//		Variable key = mapEntry.getKey();
//		Constant value = mapEntry.getValue();
//		bindings.put(key, value);
//	}
//	result.setBindings(bindings);
//	return result;
//}