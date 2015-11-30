package knowledgeBase;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import properties.PropertiesStore;
import utilities.FoilException;

import data.Constant;
import data.Literal;
import data.Predicate;
import data.PredicateType;
import data.Sign;
import data.Term;
import data.Type;
import dataCoverage.Coverage;

public abstract class KnowledgeBaseOperations {
	
	protected String fileName = null; //knowledgeBase fileName
	protected Literal targetLiteral = null;
	protected Coverage origCoverage = null;
	protected Map<String, List<List<Constant>>> tuples = null;
	protected List<Term> targetVars = null;
	protected int nRelations = 0;
	protected Map<String, List<Constant>> partialconstantSet = null;
	protected Map<String, List<Constant>> fullconstantSet = null;
	protected Map<String, boolean[]> argNotEqualTable = null;
	protected Set<String> typeSet = null;
	protected Predicate targetPredicate = null;
	protected Hashtable<String, Predicate> predicateSet = null;
	
	protected KnowledgeBaseOperations() {
		try {
//			fileName = PropertiesStore.getProperty("knowledgeBase"); // for MAC
			fileName = PropertiesStore.getProperty("knowledgeBase").toLowerCase();
		} catch (FoilException e) {
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Literal getTargetLiteral() {
		return this.targetLiteral;
	}
	public Coverage getOrigCoverage() {
		return this.origCoverage;
	}
	
	public Map<String, boolean[]> getArgNotEqualTable() {
		return argNotEqualTable;
	}

	/**
	 * @return the number of relations defined in the knowledge base
	 */
	public int getnRelations(){
		return this.nRelations;
	}
	
	/**
	 * Gets the number of original positive and negative tuples
	 * @return the total number of tuples defined in the knowledge base
	 */
	public int getOrigTuplesNumber() {
		return origCoverage.getNegativeBindings().size() + 
			origCoverage.getPositiveBindings().size();
	}
	

	public Hashtable<String, Predicate> getPredicateSet() {
		return predicateSet;
	}

	/**
	 * 
	 * @return the list of predicates available in the knowledge base (support and target predicate)
	 */
	public abstract List<Predicate> getDefinedPredicates();
	/**
	 * Gets all the predicate names stated in the knowledge base
	 * @return predicate names list
	 */
	public abstract Map<String, Integer> getNameAndArityOfAllDefinedPredicates();
	
	/**
	 * @return all predicates defined in the knowledge base
	 */
	public List<Predicate> getPredefinedPredicates() {
		List<Predicate> result = new ArrayList<Predicate>();
		Predicate p = null;
		List<Type> argsType = null;
		Type type = null;
		
		/**
		 * Equal predicate
		 */
		p = new Predicate();
		p.setName("EQUAL");
		argsType = new ArrayList<Type>();
		type = new Type();
		argsType.add(type);
		argsType.add(type);
		p.setArgsType(argsType);
		p.setPredicateType(PredicateType.PREDEFINED);
		result.add(p);
		
		/**
		 * <= Predicate
		 */
		
		p = new Predicate();
		p.setName("LE");
		argsType = new ArrayList<Type>();
		type = new Type("Number");
		argsType.add(type);
		argsType.add(type);
		p.setArgsType(argsType);
		p.setPredicateType(PredicateType.PREDEFINED);
		result.add(p);
		
		/**
		 * < Predicate
		 */
		
		p = new Predicate();
		p.setName("LT");
		argsType = new ArrayList<Type>();
		type = new Type("Number");
		argsType.add(type);
		argsType.add(type);
		p.setArgsType(argsType);
		p.setPredicateType(PredicateType.PREDEFINED);
		result.add(p);
		
		/**
		 * >= Predicate
		 */
		
		p = new Predicate();
		p.setName("GE");
		argsType = new ArrayList<Type>();
		type = new Type("Number");
		argsType.add(type);
		argsType.add(type);
		p.setArgsType(argsType);
		p.setPredicateType(PredicateType.PREDEFINED);
		result.add(p);
		
		/**
		 * > Predicate
		 */
		
		p = new Predicate();
		p.setName("GT");
		argsType = new ArrayList<Type>();
		type = new Type("Number");
		argsType.add(type);
		argsType.add(type);
		p.setArgsType(argsType);
		p.setPredicateType(PredicateType.PREDEFINED);
		result.add(p);
		
		return result;
	}
	
	/**
	 * @return the set of constants grouped by its type
	 */
	public abstract Map<String, List<Constant>> getConstantSet(Predicate p);
	public abstract List<Constant> getConstantSet(String type);
	
	public Set<String> getTypeSet() {
		return typeSet;
	}

	/**
	 * Gets the tuples list of a specific predicate
	 * @param predicateName name of the predicate to consult
	 * @param sign sign of the tuple that it wants to get
	 * @return a list of tuples got from the knowledge base
	 */
	public abstract List<List<Constant>> consultFacts(String predicateName, Sign sign);
	public abstract List<List<Constant>> consultTuplesFromRules(Predicate pred, Sign sign);
	/**
	 * Consults the test file got from apply cross-validation in the knowledge base
	 * @return a test coverage
	 */
//	public abstract Coverage loadTestCoverage();
	
	/**
	 * Loads Negatives and Positives Bindings belonging to predicateName
	 * and makes the target literal with all required data
	 */
//	public abstract void loadData();

	public abstract Map<String, List<Constant>> getPartialConstantSet();
	public abstract Map<String, List<Constant>> getFullConstantSet();
	
	/**
	 * Gets all the constants sorted by type defined in the knowledgebase by means of constantsList predicate
	 * @return Map, whose key is the type of the constant and the value is a set of constants defined for that type
	 * i.e., constantsList(number, [0,1]). 
	 */
//	public abstract Map<String,Set<String>> getConstantsList(Map<String,Set<String>> constantsList);

	
	/**
	 * Given the name of a predicate, it gets all existing
	 * positives tuples/examples. 
	 * @param predicateName: name of the predicate to search for
	 * @return Set<String> containing the positive examples of the predicateName 
	 */
	public abstract Set<String> loadPositiveExamples(String predicateName, int arity);
	
	/**
	 * Gets all the mode declarations defined in the knowledgebase
	 * @return Map whose key is the predicateDefinition and the value is the number of fixed arguments in that predicate
	 * mode(1, member(element, list)), where 1 is the value and member(element, list) is the key. The value indicates the 
	 * number of arguments that has to be instantiated
	 */
//	public abstract Map<String,Integer> getModePredicates();
	
	protected abstract void reload(String fileName);

}