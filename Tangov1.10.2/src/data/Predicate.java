package data;

import java.util.ArrayList;
import java.util.List;

public class Predicate implements Cloneable {
	
	private String name;
	private PredicateType predicateType;
	private List<Type> argsType = null;
	private List<int[]> instantiations = null;

	public Predicate(){
		this.argsType = new ArrayList<Type>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getArity() {
		return argsType.size();
	}

	public boolean isPredefined(){
		return (predicateType.equals(PredicateType.PREDEFINED));
	}
	
	public boolean isTarget(){
		return (predicateType.equals(PredicateType.TARGET));
	}
	
	public boolean isSupport(){
		return (predicateType.equals(PredicateType.SUPPORT));
	}

	public void setPredicateType(PredicateType predicateType) {
		this.predicateType = predicateType;
	}

	public List<Type> getArgsType() {
		return argsType;
	}

	public void setArgsType(List<Type> argsType) {
		this.argsType = argsType;
	}
	
	/**
	 * Checks if two predicates are equals
	 * @param p predicate to compare
	 * @return true if both predicates are equals
	 */
	public boolean equals(Predicate p) {
		boolean result = false;
		boolean end = false;
		int i = 0;
		List<Type> types = p.getArgsType();
		if (this.name.equals(p.getName()) && this.predicateType.equals(p.predicateType)) {
			if (p.getArity() == this.getArity()) {
				while (i < this.argsType.size() && !end){
					if (!this.argsType.get(i).equals(types.get(i)))
						end = true;
					i++;
				}
				if (!end)
					result = true;
			}
		}
		return result;
	}

	public List<int[]> getInstantiations() {
		return instantiations;
	}

	public void setInstantiations(List<int[]> instantiations) {
		this.instantiations = instantiations;
	}
	
	public Predicate clone()
    {
		Predicate clone = new Predicate();
		String name = new String(this.name);
		PredicateType pt = this.predicateType;
		List<Type> argsType = new ArrayList<Type>(this.argsType);
		List<int[]> instantiations = null;
		if (this.instantiations != null)
			instantiations = new ArrayList<int[]>(this.instantiations);
		
		clone.setName(name);
		clone.setPredicateType(pt);
		clone.setArgsType(argsType);
		clone.setInstantiations(instantiations);
    
        return clone;
    }
}