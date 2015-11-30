package heuristics;

public class ConfusionMatrix {
	private int truePositives;
	private int falsePositives;
	private int falseNegatives;
	private int trueNegatives;
	
	public ConfusionMatrix() {
		this.truePositives = 0;
		this.falsePositives = 0;
		this.falseNegatives = 0;
		this.trueNegatives = 0;
	}
	
	public int getTruePositives() {
		return truePositives;
	}
	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}
	public int getFalsePositives() {
		return falsePositives;
	}
	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}
	public int getFalseNegatives() {
		return falseNegatives;
	}
	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}
	public int getTrueNegatives() {
		return trueNegatives;
	}
	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}
	
	public void incTruePositives() {
		this.truePositives++;
	}
	
	public void incTruePositives(int tp) {
		this.truePositives += tp;
	}
	
	public void incFalsePositives(int fp) {
		this.falsePositives += fp;
	}
	
	public void incFalseNegatives() {
		this.falseNegatives++;
	}

	public void incFalseNegatives(int fn) {
		this.falseNegatives+= fn;
	}
	
	public void incFalsePositives() {
		this.falsePositives++;
	}
	
	public void incTrueNegatives() {
		this.trueNegatives++;
	}
	
	public void incTrueNegatives(int tn) {
		this.trueNegatives += tn;
	}
	
	public int getN() {
		return this.truePositives + this.trueNegatives + this.falseNegatives + this.falsePositives;
	}
	
	public boolean equals (ConfusionMatrix cm) {
		boolean result = false;
		
		if (this.getTruePositives() == cm.getTruePositives() && 
				this.getTrueNegatives() == cm.getTrueNegatives() &&
				this.getFalsePositives() == cm.getFalsePositives() &&
				this.getFalseNegatives() == cm.getFalseNegatives())
			
			result = true;
		
		return result;
	}
}