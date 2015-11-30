package constantManagement;

import java.util.List;
import java.util.Set;


public class OrderedConstantsTransferObject {
	private boolean sorted = false;
	private boolean[][] sortedTable = null;
	private Set<String> consistentPredicates = null;
	private List<ConsistentPairs> consistentArgumentsList = null;

	public boolean isSorted() {
		return sorted;
	}

	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	public boolean[][] getOrderdlyTable() {
		return sortedTable;
	}

	public void setOrderdlyTable(boolean[][] sortedTable) {
		this.sortedTable = sortedTable;
	}

	public Set<String> getConsistentPredicates() {
		return consistentPredicates;
	}

	public void setConsistentPredicates(Set<String> consistentPredicates) {
		this.consistentPredicates = consistentPredicates;
	}
	
	public List<ConsistentPairs> getConsistentArgumentsList() {
		return consistentArgumentsList;
	}

	public void setConsistentArgumentsList(
			List<ConsistentPairs> consistentArgumentsList) {
		this.consistentArgumentsList = consistentArgumentsList;
	}
}
