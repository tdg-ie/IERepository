package learners;

import configuration.RollerConfiguration;
import helpers.InstanceHelper;
import helpers.InstancesHelper;
import datasets.DataSet;
import rules.Rule;
import rules.ScoringFunction;
import weka.core.Instance;
import weka.core.Instances;

public class InformationGainFunction implements GainFunction {

	public double compute(Rule current, Rule expansion) {
		double result;
		int kept;
		Instances wellClassifiedCurrent, wellClassifiedNew;
		DataSet wellClassifiedNewDataSet;
		Instance instance;
		String node;
		ScoringFunction scorer;
		
		wellClassifiedCurrent = InstancesHelper.computeWellclassified(current.getClassifier(), current.getTestingDataset().getInstances());
		wellClassifiedNew = InstancesHelper.computeWellclassified(expansion.getClassifier(), expansion.getTestingDataset().getInstances());
		
		wellClassifiedNewDataSet = new DataSet();
		wellClassifiedNewDataSet.setInstances(wellClassifiedNew);
		wellClassifiedNewDataSet.setContexts(expansion.getTrainingDataset().getContexts());
		
		kept = 0;
		for (int i = 0; i < wellClassifiedCurrent.numInstances(); i++) {
			instance = wellClassifiedCurrent.instance(i);
			node = InstanceHelper.getNode(instance);
			if (wellClassifiedNewDataSet.getInstance(node) != null)
				kept++;
		}
		
		scorer = RollerConfiguration.getScoringFunction();
		result = kept * (scorer.compute(current) - scorer.compute(expansion));
		
		return result;
	}

}
