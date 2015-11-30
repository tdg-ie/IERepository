package coverageComputation;

import java.util.Map;

import data.Literal;
import data.Term;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.NodePair;

public class AnalyseOutputTransferObject {

	private NodePair<Literal, InformationContext> nodePair = null;
	private PruneType pt = null;
	private Map<Term, Map<Term, Boolean>> duplicatedVars = null;
	
	public AnalyseOutputTransferObject() {
		pt = PruneType.NOTPRUNE;
	}

	public NodePair<Literal, InformationContext> getNodePair() {
		return nodePair;
	}

	public void setNodePair(NodePair<Literal, InformationContext> nodePair) {
		this.nodePair = nodePair;
	}
	
	public PruneType getPt() {
		return pt;
	}

	public void setPt(PruneType pt) {
		this.pt = pt;
	}

	public Map<Term, Map<Term, Boolean>> getDuplicatedVars() {
		return duplicatedVars;
	}

	public void setDuplicatedVars(Map<Term, Map<Term, Boolean>> duplicatedVars) {
		this.duplicatedVars = duplicatedVars;
	}
}