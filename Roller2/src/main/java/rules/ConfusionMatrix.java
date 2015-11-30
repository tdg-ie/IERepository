package rules;

public class ConfusionMatrix {
	
	private int tp;
	private int fp;
	private int tn;
	private int fn;
	
	public int getTruePositives() {
		return tp;
	}
	
	public void setTruePositives(int tp) {
		this.tp = tp;
	}
	
	public int getFalsePositives() {
		return fp;
	}
	
	public void setFalsePositives(int fp) {
		this.fp = fp;
	}
	
	public int getTrueNegatives() {
		return tn;
	}
	
	public void setTrueNegatives(int tn) {
		this.tn = tn;
	}
	
	public int getFalseNegatives() {
		return fn;
	}
	
	public void setFalseNegatives(int fn) {
		this.fn = fn;
	}
	
	public int getNumberOfSamples() {
		int result;
		
		result = tp + fp + tn + fn;
		
		return result;
	}

}
