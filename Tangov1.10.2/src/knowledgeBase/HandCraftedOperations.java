package knowledgeBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import properties.PropertiesStore;
import utilities.FoilException;

import data.Constant;
import data.Literal;
import data.Predicate;
import data.Sign;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

public class HandCraftedOperations extends KnowledgeBaseOperations {

	private Map<String,Coverage> predicateInstances = new HashMap<String, Coverage>();
	private static HandCraftedOperations instance = null; // Singleton
	  
    protected HandCraftedOperations() {
    	this.loadKnowledgeBase();
        this.loadPredicateBindings(instance.targetLiteral.getPredicate().getName());
    }
 

	private void loadKnowledgeBase() {
		instance.targetLiteral = new Literal();
		Predicate targetPredicate = new Predicate();
		try {
			this.fileName = PropertiesStore.getProperty("knowledgeBase");
			targetPredicate.setName(PropertiesStore.getProperty("goalPredicate"));
			targetLiteral.setPredicate(targetPredicate);
		} catch (FoilException e) {
			e.printStackTrace();
		}
	}

	private void loadPredicateBindings(String predicateName) {
		Coverage cov = new Coverage(); 
		cov.addPositiveBindingsSet(this.getPosTuples(predicateName));
		cov.addNegativeBindingsSet(this.getNegTuples(predicateName));
		predicateInstances.put(predicateName, cov);
		
//		PrologTransferObject pTO = loadPositiveBindings(predicateName);
//		cov.addPositiveBindingsSet(pTO.getPositiveBindings());
//		cov.addNegativeBindingsSet(HandCraftedOperations.cwa(pTO));
	}
	
	public static Set<Bindings> cwa(PrologTransferObject pTO) {
		Set<Bindings> result = null;
		result = ClosedWorldAssumption.generateNegativeExamples(pTO);
		return result;
	}

	@Override
	public int getnRelations() {
		return this.predicateInstances.size();
	}
	
	private Set<Bindings> getNegTuples(String predicateName) {
		Set<Bindings> result = null;
		if (this.predicateInstances.containsKey(predicateName))
			result = this.predicateInstances.get(predicateName).getNegativeBindings();
		return result;
	}
	
	private Set<Bindings> getPosTuples(String predicateName) {
		Set<Bindings> result = null;
		if (this.predicateInstances.containsKey(predicateName))
			result = this.predicateInstances.get(predicateName).getPositiveBindings();
		return result;
	}
	
	@Override
	public List<List<Constant>> consultFacts(String predicateName, Sign sign){
		// TODO Auto-generated method stub
		return null;
	}
//	@Override
//	public Coverage loadTestCoverage() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	public void loadData() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<Predicate> getDefinedPredicates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<Constant>> getPartialConstantSet() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, List<Constant>> getConstantSet(Predicate p) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<List<Constant>> consultTuplesFromRules(Predicate pred, Sign sign) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<Constant> getConstantSet(String type) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, List<Constant>> getFullConstantSet() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void reload(String fileName) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Map<String, Integer> getNameAndArityOfAllDefinedPredicates() {
		// TODO Auto-generated method stub
		return null;
	}


//	@Override
//	public Map<String, Set<String>> getConstantsList(
//			Map<String, Set<String>> constantsList) {
//		// TODO Auto-generated method stub
//		return null;
//	}


	@Override
	public Set<String> loadPositiveExamples(String predicateName, int arity) {
		// TODO Auto-generated method stub
		return null;
	}


//	@Override
//	public Map<String, Integer> getModePredicates() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	
	//No está implementado aún
//	private PrologTransferObject loadPositiveBindings(
//			String predicateName) {
//		PrologTransferObject pTO = new PrologTransferObject();
//		// Falta implementar este método. Es una lectura al fichero
//		// donde se almacena la base de conocimiento
//		return pTO;
//	}
}