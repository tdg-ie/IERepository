package knowledgeBase;

import java.util.List;
import java.util.Map;
import java.util.Set;

import data.Constant;
import data.Predicate;
import data.Term;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

public class PrologTransferObject {
	private Coverage coverage = null;
	private Predicate predicate = null;
	private Map<String, List<Constant>> constantSet = null;
	private List<Term> variables = null;
	
	public PrologTransferObject() {
		coverage = new Coverage();
		predicate = new Predicate();
//		constantSet = new HashMap<String, List<Constant>>();
//		variables = new ArrayList<Term>();
	}
	
	
	public Set<Bindings> getPositiveBindings() {
		return coverage.getPositiveBindings();
	}
	
	public Set<Bindings> getNegativeBindings() {
		return coverage.getNegativeBindings();
	}
	
	public void setPositiveBindings(Set<Bindings> positiveBindings) {
		coverage.setPositiveBindings(positiveBindings);
	}
	
	public void setNegativeBindings(Set<Bindings> negativeBindings) {
		coverage.setNegativeBindings(negativeBindings);
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	public String getPredicateName() {
		return predicate.getName();
	}
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}
	
	public Map<String, List<Constant>> getConstantSet() {
		return constantSet;
	}

	public void setConstantSet(Map<String, List<Constant>> constantSet) {
		this.constantSet = constantSet;
	}
	
	public List<Term> getVariables() {
		return variables;
	}
	public void setVariables(List<Term> variables) {
		this.variables = variables;
	}
}