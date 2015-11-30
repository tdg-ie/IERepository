package heuristics;

import thresholds.Thresholds;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;

public abstract class Heuristic {

	
	public double evaluate(double currentScore, Node<Literal,InformationContext> node, int NewVariables) {
		double result = -1;
		double gain;
		
		ConfusionMatrixList cml = node.getInformationContext().getConfusionMatrixList();
		ConfusionMatrix cm = cml.getOrigcm();
		int tp = cm.getTruePositives();

		double G = score(node) - currentScore;
		gain = tp * G;
		
		if ( (G < 1E-3) && (tp > 0))
			result = 0.0009 + (NewVariables * 0.0001);  /* very small notional gain */
		else
			result = gain;
		
		return result;
	}
	public abstract double score(Node<Literal,InformationContext> node);
	public abstract double threshold(Node<Literal,InformationContext> node, double currentBaseInfo, Thresholds thresholds);
	public abstract double getMaxPossibleGain(Node<Literal,InformationContext> currentNode);
	
	
	public int getMaxNumberOfFalsePositivesforL(ConfusionMatrixList confusionMatrixListL) {
		ConfusionMatrix cml = confusionMatrixListL.getOrigcm();
		return cml.getFalsePositives();
	}
	public int getMaxNumberOfFalsePositivesforNL(ConfusionMatrixList confusionMatrixListNL) {
		ConfusionMatrix cmnl = confusionMatrixListNL.getOrigcm();
		return cmnl.getFalsePositives();
	}
	
	public boolean isInfinity(double number) {
		boolean result = false;
		if (Double.compare(number, Double.NaN) == 0
				|| Double.compare(number, Double.POSITIVE_INFINITY) == 0
				|| Double.compare(number, Double.NEGATIVE_INFINITY) == 0)
			result = true;
		return result;
	}
}
