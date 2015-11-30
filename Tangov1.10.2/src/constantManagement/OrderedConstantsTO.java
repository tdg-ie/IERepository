package constantManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderedConstantsTO {

	private List<ConsistentPairs> consistentPairs;
	private Map<String, List<String>> consistentArguments = new HashMap<String, List<String>>();
	private Map<String,List<String>> finalOrders = new HashMap<String, List<String>>();

	public OrderedConstantsTO() {
		this.consistentPairs = new ArrayList<ConsistentPairs>();
	}
	
	public List<ConsistentPairs> getConsistentPairs() {
		return consistentPairs;
	}

	public void setConsistentPairs(List<ConsistentPairs> consistentPairs) {
		this.consistentPairs = consistentPairs;
	}
	
	public void addConsistentPair(ConsistentPairs cp) {
		this.consistentPairs.add(cp);
	}

	public Map<String, List<String>> getConsistentArguments() {
		return consistentArguments;
	}

	public void setConsistentArguments(Map<String, List<String>> consistentArguments) {
		this.consistentArguments = consistentArguments;
	}

	public Map<String, List<String>> getFinalOrders() {
		return finalOrders;
	}

	public void setFinalOrders(Map<String, List<String>> finalOrders) {
		this.finalOrders = finalOrders;
	}
}