package stopStrategies;

import properties.PropertiesStore;
import utilities.FoilException;
import branch_and_bound.data.InformationContext;

public class TheMostPromisingLiteral implements IStopStrategies {

	private float threshold;
	
	public TheMostPromisingLiteral() {
		try {
			threshold = Float.parseFloat(PropertiesStore.getProperty("threshold"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FoilException e) {
			e.printStackTrace();
		}
	}
	//The most promising literal is one that exceeds a threshold (default, 80% maxGain) 
	@Override
	public boolean stop(InformationContext icCurrentNode, double maxGain) {
		boolean result = false;
		if (icCurrentNode.getGain() >= (maxGain * this.threshold))
			result = true;
		return result;
	}
}
