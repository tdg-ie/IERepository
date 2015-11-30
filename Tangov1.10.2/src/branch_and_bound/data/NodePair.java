package branch_and_bound.data;

public class NodePair<T,I> {

	private Node<T,I> node;
	private Node<T,I> negatedNode;
	
	public Node<T, I> getNode() {
		return node;
	}
	public void setNode(Node<T, I> node) {
		this.node = node;
	}
	public Node<T, I> getNegatedNode() {
		return negatedNode;
	}
	public void setNegatedNode(Node<T, I> negatedNode) {
		this.negatedNode = negatedNode;
	}
}