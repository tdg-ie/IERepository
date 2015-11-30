package dataCoverage;

public class CoveragePair {
	Coverage originalCoverage = null;
	Coverage newCoverage = null;
	
	public Coverage getOriginalCoverage() {
		return originalCoverage;
	}
	public void setOriginalCoverage(Coverage originalCoverage) {
		this.originalCoverage = originalCoverage;
	}
	public Coverage getNewCoverage() {
		return newCoverage;
	}
	public void setNewCoverage(Coverage newCoverage) {
		this.newCoverage = newCoverage;
	}
}
