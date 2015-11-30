package coverageComputation;

import thresholds.Thresholds;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.data.NodePair;
import data.Literal;

public class AnalyseInputTransferObject {

	private Node<Literal, InformationContext> parent;
	private NodePair<Literal, InformationContext> nodePair;
	private Thresholds thresholds; 
	private int nRelations;
	private boolean negatedLitAllowed;
	private int nTryArgs;
	private int allTuples;
	private int pathDepth;
	private int detLitNumber;
	
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
	public int getNRelations() {
		return nRelations;
	}
	public void setNRelations(int relations) {
		nRelations = relations;
	}
	public boolean isNegatedLitAllowed() {
		return negatedLitAllowed;
	}
	public void setNegatedLitAllowed(boolean negatedLitAllowed) {
		this.negatedLitAllowed = negatedLitAllowed;
	}
	public int getNTryArgs() {
		return nTryArgs;
	}
	public void setNTryArgs(int tryArgs) {
		nTryArgs = tryArgs;
	}
	public int getAllTuples() {
		return allTuples;
	}
	public void setAllTuples(int allTuples) {
		this.allTuples = allTuples;
	}
	public Node<Literal, InformationContext> getParent() {
		return parent;
	}
	public void setParent(Node<Literal, InformationContext> parent) {
		this.parent = parent;
	}
	public int getPathDepth() {
		return pathDepth;
	}
	public void setPathDepth(int pathDepth) {
		this.pathDepth = pathDepth;
	}
	public int getDetLitNumber() {
		return detLitNumber;
	}
	public void setDetLitNumber(int detLitNumber) {
		this.detLitNumber = detLitNumber;
	}
}
