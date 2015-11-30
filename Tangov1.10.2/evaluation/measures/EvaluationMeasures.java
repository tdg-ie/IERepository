package measures;

import dataCoverage.Coverage;

public class EvaluationMeasures {
	private double fMeasure;
	private double precision;
	private double recall;
	private double accuracy;
	private long extractionTime;
	
	public EvaluationMeasures() {
		this.fMeasure = 0.0;
		this.precision = 0.0;
		this.recall = 0.0;
		this.accuracy = 0.0;
		this.extractionTime = 0;
	}

	public double getfMeasure() {
		return fMeasure;
	}

	public void computefMeasure(Coverage origCoverage, Coverage cr) {
		computePrecision(cr);
		computeRecall(origCoverage, cr);
		if (this.precision == 0 || this.recall == 0)
			this.fMeasure = 0;
		else 
			this.fMeasure = 2 * ((precision * recall)/(precision + recall));
	}

	public double getPrecision() {
		return precision;
	}

	public void computePrecision(Coverage cr) {
		double tp = cr.getPositiveBindings().size();
		double fp = cr.getNegativeBindings().size();
		if (tp == 0)
			this.precision = 0;
		else
			this.precision = tp / (tp + fp);
	}

	public double getRecall() {
		return recall;
	}

	public void computeRecall(Coverage origCoverage, Coverage cr) {
		double tp = cr.getPositiveBindings().size();
		double fn = origCoverage.getPositiveBindings().size() - tp;
		if (tp == 0)
			this.recall = 0;
		else
			this.recall = tp / (tp + fn);
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void computeAccuracy(Coverage origcr, Coverage cr) {
		double tp = cr.getPositiveBindings().size();
		double fp = cr.getNegativeBindings().size();
		double tn = origcr.getNegativeBindings().size() - cr.getNegativeBindings().size();
		double fn = origcr.getPositiveBindings().size() - cr.getPositiveBindings().size();
		double addition = tp + fp + tn + fn;
		if (addition == 0)
			this.accuracy = 0;
		else 
			this.accuracy = (tp + tn) / addition;
	}

	public long getExtractionTime() {
		return extractionTime;
	}

	public void setExtractionTime(long extractionTime) {
		this.extractionTime = extractionTime;
	}
}