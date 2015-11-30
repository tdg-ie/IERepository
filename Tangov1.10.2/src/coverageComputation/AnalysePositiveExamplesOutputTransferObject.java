package coverageComputation;

import java.util.Map;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.NodePair;
import data.Literal;
import data.Term;

public class AnalysePositiveExamplesOutputTransferObject {
	private NodePair<Literal, InformationContext> nodePair = null;
	private PruneType pt = null;
	private AlphaBetaPruning alphaBetaP = null;
	private Map<Term, Map<Term, Boolean>> duplicatedVars = null;
	
	public AnalysePositiveExamplesOutputTransferObject() {
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

	public AlphaBetaPruning getAlphaBetaP() {
		return alphaBetaP;
	}

	public void setAlphaBetaP(AlphaBetaPruning alphaBetaP) {
		this.alphaBetaP = alphaBetaP;
	}

	public Map<Term, Map<Term, Boolean>> getDuplicatedVars() {
		return duplicatedVars;
	}

	public void setDuplicateVars(Map<Term, Map<Term, Boolean>> duplicatedVars) {
		this.duplicatedVars = duplicatedVars;
	}
}