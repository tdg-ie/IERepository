package heuristics;

public class ConfusionMatrixList implements Cloneable{
	private ConfusionMatrix origcm = null;
	private ConfusionMatrix newcm = null;
	private ConfusionMatrix nowcm = null;
	
	public ConfusionMatrixList () {
		this.origcm = new ConfusionMatrix();
		this.newcm = new ConfusionMatrix();
		this.nowcm = new ConfusionMatrix();
	}

	public ConfusionMatrix getOrigcm() {
		return origcm;
	}

	public void setOrigcm(ConfusionMatrix origcm) {
		this.origcm = origcm;
	}

	public ConfusionMatrix getNewcm() {
		return newcm;
	}

	public void setNewcm(ConfusionMatrix newcm) {
		this.newcm = newcm;
	}

	public ConfusionMatrix getNowcm() {
		return nowcm;
	}

	public void setNowcm(ConfusionMatrix nowcm) {
		this.nowcm = nowcm;
	}
	
	public boolean equals (ConfusionMatrixList cml) {
		boolean result = false;
		
		if (this.getOrigcm().equals(cml.getOrigcm()) && this.getNowcm().equals(cml.getNowcm()) && this.getNewcm().equals(cml.getNewcm()))
				result = true;
		return result;
	}
	
	public ConfusionMatrixList clone() 
    {
		ConfusionMatrixList clone = new ConfusionMatrixList();
		
		// deep clone
		
		ConfusionMatrix cOrig = new ConfusionMatrix();
		cOrig.setTruePositives(this.origcm.getTruePositives());
		cOrig.setFalsePositives(this.origcm.getFalsePositives());
		cOrig.setFalseNegatives(this.origcm.getFalseNegatives());
		cOrig.setTrueNegatives(this.origcm.getTrueNegatives());
		
		ConfusionMatrix cNew = new ConfusionMatrix();
		cNew.setTruePositives(this.newcm.getTruePositives());
		cNew.setFalsePositives(this.newcm.getFalsePositives());
		cNew.setFalseNegatives(this.newcm.getFalseNegatives());
		cNew.setTrueNegatives(this.newcm.getTrueNegatives());
		
		ConfusionMatrix cNow = new ConfusionMatrix();
		cNow.setTruePositives(this.nowcm.getTruePositives());
		cNow.setFalsePositives(this.nowcm.getFalsePositives());
		cNow.setFalseNegatives(this.nowcm.getFalseNegatives());
		cNow.setTrueNegatives(this.nowcm.getTrueNegatives());
		
		clone.setNewcm(cNew);
		clone.setNowcm(cNow);
		clone.setOrigcm(cOrig);
		
        return clone;
    }
}
