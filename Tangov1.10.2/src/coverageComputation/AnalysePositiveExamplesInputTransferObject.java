package coverageComputation;
import thresholds.Thresholds;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.data.NodePair;
import data.Literal;

public class AnalysePositiveExamplesInputTransferObject {
	
	private Node<Literal,InformationContext> parent;
	private NodePair<Literal, InformationContext> nodePair;
	private Thresholds thresholds;
	
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
	public Node<Literal, InformationContext> getParent() {
		return parent;
	}
	public void setParent(Node<Literal, InformationContext> parent) {
		this.parent = parent;
	}
}
