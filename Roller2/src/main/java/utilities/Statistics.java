package utilities;

public class Statistics {

	private int nodesExplored;
	private int numberAttempts;

	public int getNodesExplored() {
		return nodesExplored;
	}

	public void newNodeExplored() {
		nodesExplored++;		
	}
	
	public int getNumberAttempts() {
		return numberAttempts;
	}

	public double getAverageNumberAttempts() {
		double result;
		
		result = (double)numberAttempts / (double)nodesExplored;
		
		return result;
	}
	
	public void newAttempt() {
		numberAttempts++;		
	}
	
}
