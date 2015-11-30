package branch_and_bound.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Branch_and_Bound<N, O> {
	
	N currentNode = null;
	List<N> candidateNodes = null;
	N targetNode = null;
	N parent = null;
	O outputData = null;
	List<N> finalNodes = null;
	
	/**
	 * Executes the branch and bound algorithm
	 * @return the output data set
	 * @throws IOException 
	 */
	public O run() throws IOException {
		O result = null;
		
		
		boolean end = false;
		boolean solution = false;
		boolean stop = false;
		
		List<N> selectedNodeList;
		
		currentNode = targetNode;
		
		createTraceFile();
		
		finalNodes = new ArrayList<N>();
		printState();
		while (!end) {
			while (!solution && !end && (!(candidateNodes = branch()).isEmpty())){
				parent = currentNode;
				//iterating over the child nodes
				Iterator<N> it = candidateNodes.iterator();
				stop = false;
				while (it.hasNext() && !stop) {
					currentNode = it.next();
//					System.out.println("Nodo evaluado: " + this.print(currentNode));
					if (!bound()) { //if current node is not pruned, it is added as child nodes
						addNodeAsChild();
//						System.out.println(" - 	Nodo añadido");
//						System.out.println("Nodo añadido: " + this.print(currentNode));
					}
//					else 
//						System.out.println();
					stop = stop();
				}
				candidateNodes = new ArrayList<N>(this.getChildren(parent));
				if (!candidateNodes.isEmpty()) {
					selectedNodeList = selectNode(); //mejorar este método
					currentNode = selectedNodeList.get(selectedNodeList.size()-1);					
//					System.out.println("Nodo seleccionado: " + print(currentNode));
//					printBranch(currentNode);
					savePoints();
					removeUselessBranchesNodeLevel();
					solution = isSolution();
				}
				if (!solution && (this.candidateNodes.isEmpty() || this.forceCut()))
					end = true;
				else { 
					printState();
					this.reset();
				}
//				this.printTree(currentNode);
			}
			end = false;
			if (!solution) { // if there is not solution, we perform backtracking
				currentNode = restoreBestSavedPoint();
				if (currentNode == null)
					end = true;
				if (!end)
					solution = isSolution();
			}
			else {
				currentNode = replacementCheck(); // check if there is some saved rule better than those that we got
				currentNode = this.postProcessBranch(currentNode); //modify the tree --> it lasts longer that it should
				finalNodes.add(currentNode);
				end = checkState();//checks if all positive bindings are already covered by the node set (nodes)
//				this.printBranch(currentNode);
				if (!end) {
					update();
					resetSavedList();//Empty the list of saved points
					solution = false;
					this.printState();
				}
			}
		}
		result = postProcessing(finalNodes);
		return result;
	}
	
	/**
	 * checks for the best complete node found so far (between currentNode and the nodes in the saved point list)
	 * @return
	 * @throws IOException 
	 */
	protected abstract N replacementCheck() throws IOException;
	
	/**
	 * Prunes some nodes of the branch because they do not contribute to improve the solution.
	 * @param finalNode leaf node of the branch
	 * @return the leaf node with an improved path (to the root node) if possible
	 */
	public abstract N postProcessBranch(N finalNode);

	/**
	 * Cut and return to a savepoint if the path is not good enough
	 * @return
	 */
	protected abstract boolean forceCut();

	/**
	 * creates output data file
	 */
	protected abstract void createTraceFile();
	
	/**
	 * initialises data
	 */
	protected abstract void update();
	
	/**
	 * reset thresholds and coverage data. It used when the process of
	 * search for each rule start
	 */
	protected abstract void reset();

	/**
	 * check if all original positive Bingings has been already covered by nodelist
	 * @param nodes list of leaf nodes
	 * @return true if the list of nodes cover all original positive bindings 
	 * (i.e, if the ruleset is successful), false otherwise
	 */
	protected abstract boolean checkState();

	/**
	 * add a node as child of the parent node
	 */
	protected abstract void addNodeAsChild();
	
	/**
	 * Get all child nodes of a specific node
	 * @param node over which get the child node list
	 * @return a node list with its children
	 */
	protected abstract List<N> getChildren(N node);

	/**
	 * Get the parent node of a specific node 
	 * @param node child node
	 * @return parent node
	 */
	protected abstract N getParent(N node);

	/**
	 * evaluates currentNode, and returns if this node must be pruned.
	 * @return true if the node have to be pruned, false otherwise
	 * @throws IOException 
	 */
	protected abstract boolean bound() throws IOException;

	/**
	 * returns the literals set to evaluate in the next iteration 
	 * @return the list of candidates nodes to evaluate
	 * @throws IOException 
	 */
	protected abstract List<N> branch() throws IOException;
	
	/**
	 * Extracts the most promising Node from list candidates nodes
	 * @return the most promising Node
	 * @throws IOException 
	 */
	protected abstract List<N> selectNode() throws IOException;
	
	/**
	 * check if the extracted node (currentNode) is a leaf (solution)
	 * @return true if the current node has not children or if current node does not cover any negative Bindings (or 
	 * if it only covers a number that does not exceed a threshold --> not implemented yet), false otherwise
	 */
	protected abstract boolean isSolution();
	
	/**
	 * restore the best node saved so far
	 * @return the best saved node
	 * @throws IOException 
	 */
	protected abstract N restoreBestSavedPoint() throws IOException;
	
	/**
	 * saves all nodes belonging to currentGeneration whose information gain 
	 * is similar to the gain of the extracted node 
	 * @throws IOException 
	 */
	protected abstract void savePoints() throws IOException;
	
	/**
	 * Checks if there are more nodes to explore
	 * @param node last node visited so far. It checks if this node has more siblings
	 * @return true if there are more nodes to explore in this level, false otherwise
	 */
	protected abstract boolean hasNext(N node);
	
	/**
	 * Prints a specific node in a output file
	 * @param node node to print in the file
	 */
	protected abstract String print(N node);
	
	/**
	 * reset the list with the saved points and the list with the saved paths
	 * This method is used when we start the search for a solution from the target node
	 */
	protected abstract void resetSavedList();
	
	/**
	 * Stops the evaluation of nodes if some criteria is safisfaced
	 * @return true if the evaluation have to be stopped, false otherwise
	 */
	protected abstract boolean stop();
	
	/**
	 * Get the goal predicate
	 * @return goal predicate
	 */
	protected abstract String getGoalPredicate();
	
	/**
	 * reduces tree size or speeds up the tree removing all children 
	 * that are not going to be used in the final solution 
	 */
	protected abstract void removeUselessBranchesNodeLevel();

	/**
	 * Prints to the screen the current states (prints covered bindings, used bits, 
	 * current best path found, ...)
	 * @throws IOException 
	 */
	protected abstract void printState() throws IOException;

	/**
	 * Prunes some nodes of the solution because they do not contribute 
	 * to improve the solution (they do not add anything new). 
	 * @param nodes list of nodes to clean
	 * @return a node list where the useless nodes has been removed
	 * @throws IOException 
	 */
	protected abstract O postProcessing(List<N> finalNodes) throws IOException;
	
	protected abstract String printBranch(N node);
}

//import utilities.Memory;
//try {
//	Memory.getMemoryUsage();
//} catch (Exception e) {
//	e.printStackTrace();
//}