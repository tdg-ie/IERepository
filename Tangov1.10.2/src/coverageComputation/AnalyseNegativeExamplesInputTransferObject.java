package coverageComputation;

import java.util.Map;

import thresholds.Thresholds;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.data.NodePair;
import data.Literal;
import data.Term;

public class AnalyseNegativeExamplesInputTransferObject {
	
	private Node<Literal, InformationContext> parent;
	private NodePair<Literal, InformationContext> nodePair;
	private Thresholds thresholds;
	private AlphaBetaPruning alphaBetaP;
	private PruneType pt = null;
	private Map<Term, Map<Term, Boolean>> duplicatedVars = null;
	
	
	public NodePair<Literal, InformationContext> getNodePair() {
		return nodePair;
	}
	public void setNodePair(NodePair<Literal, InformationContext> nodePair) {
		this.nodePair = nodePair;
	}
	
	public Thresholds getThresholds() {
		return thresholds;
	}
	public void setThresholds(Thresholds thresholds) {
		this.thresholds = thresholds;
	}
	public AlphaBetaPruning getAlphaBetaP() {
		return alphaBetaP;
	}
	public void setAlphaBetaP(AlphaBetaPruning alphaBetaP) {
		this.alphaBetaP = alphaBetaP;
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
	public Node<Literal, InformationContext> getParent() {
		return parent;
	}
	public void setParent(Node<Literal, InformationContext> parent) {
		this.parent = parent;
	}
}