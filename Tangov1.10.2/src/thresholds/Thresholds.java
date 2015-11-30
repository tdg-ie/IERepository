package thresholds;

import properties.PropertiesStore;
import utilities.FoilException;

public class Thresholds {
	private double minPos;
	private double minUsefulGain;
	private double maxPossibleGain;
	private int minSaveableCover;
	private int bestCover;
	private double bestLitGain;
	private double percentajeThreshold;
	private double newTNegThresh; 
	private double nowFNegThresh;
	private int currentNNeg;
	
	public Thresholds() {
		this.resetThresholds();
		this.newTNegThresh = 0;
		this.nowFNegThresh = 0;
		this.currentNNeg = 0;
		this.bestCover = 0;
		try {
			this.percentajeThreshold = Double.parseDouble(PropertiesStore.getProperty("threshold"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FoilException e) {
			e.printStackTrace();
		} 
	}
	
	public void resetThresholds() {
		this.minUsefulGain = 0;
		this.bestLitGain = - Double.MAX_VALUE;
		this.minPos = -0.001;
		this.minSaveableCover = this.bestCover + 1;
	}
	
	public double getMinPos() {
		return minPos;
	}
	public int getMinSaveableCover() {
		return minSaveableCover;
	}
	public double getMinUsefulGain() {
		return minUsefulGain;
	}
	public double getMaxPossibleGain() {
		return maxPossibleGain;
	}
	public int getBestCover() {
		return bestCover;
	}
	public double getBestLitGain() {
		return bestLitGain;
	}
	public double getPercentajeThreshold() {
		return percentajeThreshold;
	}
	public double getNewTNegThresh() {
		return newTNegThresh;
	}
	public double getNowFNegThresh() {
		return nowFNegThresh;
	}

	public void updateMinPos(double currentScore) {
		this.minPos = this.minUsefulGain / currentScore - 0.001;
	}
	
	public void updateMinSaveableCover() {
		this.minSaveableCover = this.bestCover + 1;
	}
	
	public void updateMinUsefulGain() {
		double minGain  = this.percentajeThreshold * this.bestLitGain;
		if (minGain > this.minUsefulGain)
			this.minUsefulGain = minGain;
	}
	
	public void updateMinUsefulGainToMax() {
		this.minUsefulGain = this.percentajeThreshold * this.maxPossibleGain;
	}
	
	public void updateBestLitGain(double bestLitGain){
		if (bestLitGain > this.bestLitGain)
			this.bestLitGain = bestLitGain;
	}
	
	public void updateMaxPossibleGain(double maxPossibleGain) {
		this.maxPossibleGain = maxPossibleGain;
	}
	
	public void updateBestCover(int bestCover) {
		if (bestCover > this.bestCover)
			this.bestCover = bestCover;
	}
	
	public void setNewTNegThresh(double newTNegThresh){
		this.newTNegThresh = newTNegThresh;
	}
	public void setNowFNegThresh(double nowFNegThresh){
		this.nowFNegThresh = nowFNegThresh;
	}
	
	public int getCurrentNNeg() {
		return this.currentNNeg;
	}

	public void setCurrentNNeg(int size) {
		this.currentNNeg = size;
	}

	public void resetBestCover() {
		this.bestCover = 0;
	}
}