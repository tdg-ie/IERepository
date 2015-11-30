package branch_and_bound.data;

import heuristics.ConfusionMatrixList;
import dataCoverage.Coverage;

public class InformationContext implements Cloneable{

	private Coverage coverage;
	private Coverage origCoverage;
	private double score;
	private double gain;
	private double bits;
	private double newRuleBits;
	private NodeType type;
	private ConfusionMatrixList confusionMatrixList;
	
	public InformationContext() {
		this.gain = 0.0;
		this.coverage = new Coverage();
		this.origCoverage = new Coverage();
		this.bits = 0;
		this.type = NodeType.GAINFUL;
		this.confusionMatrixList = new ConfusionMatrixList();
	}
	
	public double getNewRuleBits() {
		return newRuleBits;
	}

	public void setNewRuleBits(double newRuleBits) {
		this.newRuleBits = newRuleBits;
	}
	
	public Coverage getCoverage() {
		return coverage;
	}

	public void setCoverage(Coverage coverage) {
		this.coverage = coverage;
	}

	public Coverage getOrigCoverage() {
		return origCoverage;
	}

	public void setOrigCoverage(Coverage origCoverage) {
		this.origCoverage = origCoverage;
	}

	public double getBits() {
		return bits;
	}

	public void setBits(double bits) {
		this.bits = bits;
	}
	
	public boolean isDeterminate() {
		return this.type.equals(NodeType.DETERMINATE);
	}
	
	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}
	
	/**
	 * two informationContext objects are equals if they have the same
	 * original coverage and the same type
	 */
	public boolean equals(InformationContext ic) {
		
		boolean result = false;
		// es si sus matrices de confusión son las mismas...
		result = this.origCoverage.equals(ic.origCoverage) 
		&& this.type.compareTo(ic.type) == 0;
		
		return result;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public ConfusionMatrixList getConfusionMatrixList() {
		return confusionMatrixList;
	}

	public void setConfusionMatrixList(ConfusionMatrixList confusionMatrixList) {
		this.confusionMatrixList = confusionMatrixList;
	}
	
	public InformationContext clone() 
    {
		InformationContext clone = new InformationContext();
		
		// deep clone
		
		clone.setCoverage(this.coverage.clone());
		clone.setOrigCoverage(this.origCoverage.clone());
		clone.setScore(this.score);
		clone.setGain(this.gain);
		clone.setBits(this.bits);
		clone.setNewRuleBits(this.newRuleBits);
		clone.setType(this.type);
		clone.setConfusionMatrixList(this.confusionMatrixList.clone());
		 
        return clone;
    }
}