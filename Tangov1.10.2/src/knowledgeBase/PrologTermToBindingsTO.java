package knowledgeBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.Constant;
import data.Term;
import dataCoverage.Bindings;

public class PrologTermToBindingsTO {
	private Set<Bindings> bindingsSet = null;
	private List<Constant> constants = null;
	private List<Term> variables = null;
	
	public PrologTermToBindingsTO(){
		constants = new ArrayList<Constant>();
		variables = new ArrayList<Term>();
		bindingsSet = new HashSet<Bindings>();
	}
	
	public Set<Bindings> getBindingsSet() {
		return bindingsSet;
	}
	
	public void addBinding(Bindings b) {
		this.bindingsSet.add(b);
	}
	public void setBindingsSet(Set<Bindings> bindingsSet) {
		this.bindingsSet = bindingsSet;
	}
	
	public List<Constant> getConstants() {
		return constants;
	}
	public void setConstants(List<Constant> constants) {
		this.constants = constants;
	}
	public List<Term> getVariables() {
		return variables;
	}
	public void setVariables(List<Term> variables) {
		this.variables = variables;
	}
}
