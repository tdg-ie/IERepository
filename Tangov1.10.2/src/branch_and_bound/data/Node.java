package branch_and_bound.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node<T,I> implements Cloneable {

	private Node<T,I> parent = null;
	private List<Node<T,I>> children;
	private T dataNode;
	private I informationContext;
	
	public Node() {
		this.children = new ArrayList<Node<T,I>>();
	}
	
	public Node<T,I> getParent() {
		return parent;
	}
	public void setParent(Node<T,I> node) {
		this.parent = node;
	}
	public List<Node<T,I>> getChildren() {
		return children;
	}
	public void setChildren(List<Node<T,I>> children) {
		this.children = children;
	}
	
	public T getDataNode() {
		return dataNode;
	}
	public void setDataNode(T dataNode) {
		this.dataNode = dataNode;
	}
	
	public I getInformationContext() {
		return informationContext;
	}

	public void setInformationContext(I informationContext) {
		this.informationContext = informationContext;
	}
	
	public void addChild(Node<T,I> currentNode) {
		this.children.add(currentNode);
		currentNode.setParent(this);
	}
	
//	public boolean removeChild(Node<T,I> node) {
//		return this.children.remove(node);
//	}
	
	public boolean hasChildren() {
		return !this.children.isEmpty();
	}
	
//	public int hasChild(Node<T,I> node) {
//		int result = -1;
//		for (int i = 0; (result== -1) && i < this.children.size(); i++) {
//			T prueba1 = this.children.get(i).getDataNode();
//			T prueba2 = node.getDataNode();
//			System.out.println(prueba1.toString());
//			System.out.println(prueba2.toString());
//			System.out.println(this.children.get(i).equals(node));
//			if (this.children.get(i).getDataNode().equals(node.getDataNode()))
//				result = i;
//		}
//		return result;
//	}
	
	public List<Node<T,I>> getPath() {
		List<Node<T,I>> result = new ArrayList<Node<T,I>>();
		Node<T,I> predecessor = this;
		while (predecessor != null) {
			result.add(predecessor);
			predecessor = predecessor.getParent();
		}
		result.remove(result.size()-1);
		Collections.reverse(result);
		return result;
	}
	
	public List<Node<T,I>> getFullPath() {
		List<Node<T,I>> result = new ArrayList<Node<T,I>>();
		Node<T,I> predecessor = this;
		while (predecessor != null) {
			result.add(predecessor);
			predecessor = predecessor.getParent();
		}
		Collections.reverse(result);
		return result;
	}
	
	public Node<T,I> getRoot() {
		Node<T, I> node = this;
		while(node.parent != null)
			node = node.parent;
		return node;
	}
	
	public Node<T,I> clone()
    {
		Node<T,I> clone = new Node<T,I>();
		
		// Deep clone
		clone.setDataNode(dataNode);
		clone.setParent(parent);
		clone.setInformationContext(informationContext);
        clone.setChildren(new ArrayList<Node<T,I>>(children));
        
        return clone;
    }
}