package learners;

import helpers.DataSetHelper;

import java.util.List;

import rules.Rule;
import strings.NameFactory;
import utilities.Statistics;
import configuration.RollerConfiguration;
import datasets.ContextTuple;
import datasets.DataSet;

public class RollerLearner {

	private Rule rule;
	private Statistics statistics;
	
	public Rule getRule() {
		return rule;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}
	
	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	public void learn() {
		DataSet trainingData, testingData;
		Rule expansion;
		boolean keepSearching;
		
		try {		
			trainingData = RollerConfiguration.getTrainingSet();
			testingData = RollerConfiguration.getTrainingSet();
			
			System.out.printf("ANALISING CONTEXT %s%n", trainingData.getInstances().relationName());
			
			System.out.printf("LEARNING INITIAL RULE%n");
			
			rule = new Rule();
			rule.setTrainingDataset(trainingData);
			rule.setTestingDataset(testingData);
			rule.setStatistics(statistics);
			rule.learn();
			
			statistics.newNodeExplored();
			
			expansion = rule;
			keepSearching = !rule.isConsistent();
			while (keepSearching) {
				System.out.printf("FINDING EXPANSION%n");				
				expansion = findExpansion(rule, statistics);
				keepSearching = (expansion != rule && !expansion.isConsistent());
				rule = expansion;
			}
			
			System.out.printf("WINNER RULE%n%s%n", rule);			
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
	}
	
	protected Rule findExpansion(Rule previousRule, Statistics statistics) {
		Rule result;
		DataSet trainingDataset, testingDataset;
		List<String> relationNames;
		DataSet misclassifiedDataset;
		List<ContextTuple> contexts;
		ContextTuple context;
		String relation;
		DataSet trainingExpansion, testingExpansion;
		ContextTuple newContext;
		Rule expansion;
		GainFunction gainFunction;
				
		trainingDataset = previousRule.getTrainingDataset();
		testingDataset = previousRule.getTestingDataset();
		relationNames = RollerConfiguration.getRelationNames();
		misclassifiedDataset = previousRule.getMissclassified();
		contexts = trainingDataset.getContexts();		
		result = previousRule;
		gainFunction = RollerConfiguration.getGainFunction();
		
		for (int i = 0; i < contexts.size() && !result.isConsistent(); i++) {
			context = contexts.get(i);
			for (int j = 0; j < relationNames.size() && !result.isConsistent(); j++) {
				relation = relationNames.get(j);
				
				newContext = new ContextTuple();
				newContext.setTarget(NameFactory.freshVariable("node"));
				newContext.setRelation(relation);
				newContext.setSource(context.getTarget());
				
				trainingExpansion = DataSetHelper.computeExpansion(trainingDataset, misclassifiedDataset, newContext);
				testingExpansion = DataSetHelper.computeExpansion(testingDataset, newContext);
				
				expansion = new Rule();
				expansion.setTrainingDataset(trainingExpansion);
				expansion.setTestingDataset(testingExpansion);
				expansion.setStatistics(statistics);
				expansion.learn();
				
				statistics.newNodeExplored();
								 
				if (expansion.isConsistent() || gainFunction.compute(previousRule, expansion) > gainFunction.compute(previousRule, result))
					result = expansion;
			}
		}
		
		return result;
	}
	
}
