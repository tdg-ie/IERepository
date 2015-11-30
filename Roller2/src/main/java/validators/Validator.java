package validators;

import helpers.DataSetHelper;
import helpers.InstancesHelper;

import java.util.List;

import rules.Rule;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import datasets.ContextTuple;
import datasets.DataSet;

public class Validator {

	private Rule rule;
	private DataSet dataSet;
	private Evaluation performance;	

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = expand(dataSet, rule);
	}

	public void validate() {
		Instances instances;
		
		try {
			instances = dataSet.getInstances();
			InstancesHelper.balance(instances);
			instances = InstancesHelper.filter(dataSet.getFilter(), instances);
			
			//InstancesPersistence.save(instances, new File ("c:/temp/instances.csv"));
			
//			Attribute clazz = dataSet.getAttributeDefinition("node_clazz");
//			for (int i = 0; i < clazz.numValues(); i++)
//				System.out.printf("%d --> %s%n", i, clazz.value(i));

			performance = new Evaluation(instances);
			performance.evaluateModel(rule.getClassifier(), instances);
			
//			Instances misclassified;
//			misclassified = InstancesHelper.computeMissclassified(rule.getClassifier(), instances);
			//InstancesPersistence.save(misclassified, new File("c:/temp/misclassified.csv"));
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
	}

	public Evaluation getPerformance() {
		return performance;
	}

	private DataSet expand(DataSet dataSet, Rule rule) {
		DataSet result;
		List<ContextTuple> contexts;

		result = new DataSet();
		result.setInstances(dataSet.getInstances());
		result.setContexts(dataSet.getContexts());

		contexts = rule.getTrainingDataset().getContexts();
		for (ContextTuple context : contexts) {
			System.out.printf("BINDING %s%n", context);

			if (!context.isDefault())
				result = DataSetHelper.computeExpansion(result, context);
		}

		return result;
	}

}
