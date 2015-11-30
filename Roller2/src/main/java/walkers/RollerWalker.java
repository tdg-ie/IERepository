package walkers;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import learners.RollerLearner;
import persistence.ConfigurationPersistence;
import persistence.RulePersistence;
import rules.Rule;
import strings.Formatter;
import system.Memory;
import system.Watch;
import utilities.Statistics;
import validators.Validator;
import configuration.GlobalConfiguration;
import configuration.RollerConfiguration;

public class RollerWalker extends Walker {
	
	@Override
	public void walkPreaction(File root) {
		GlobalConfiguration.getOutput().printf("Site,Learning Time (segs.),Score,Nodes,Attempts,contexts,contexts',Attributes,Num.Rules,Num.Selectors,Testing Time (segs.),P,R,F1,ROC,Error Rate%n");
	}
	
	@Override
	public void sitePostAction(File root, File category, File site) {
		Statistics statistics;
		RollerLearner roller;
		Validator validator;
		Rule rule;

		try {

			// ///////////////////////////////////////////////////////////////////////////

			System.out.printf("===> ROLLER'S LEARNING FROM %s%n", site.getName());
			
			Watch.start();
			{
				ConfigurationPersistence.load(site);
				statistics = new Statistics();
				
				roller = new RollerLearner();
				roller.setStatistics(statistics);
				roller.learn();
				rule = roller.getRule();
			}
			Watch.stop();
			
			RulePersistence.save(site, rule);
			
			System.out.printf("<=== END LEARNING %s - %s - %s%n", 
					site.getName(), 
					Formatter.toTime(Watch.getTiming()),
					Formatter.toSize(Memory.getFreeMemory()));

			// Site, Learning Time (segs.), Score, Nodes, Attempts, contexts, contexts', Attributes, Num. Rules, Num. Selectors
			GlobalConfiguration.getOutput().printf("%s,%f,%f,%d,%d,%d,%d,%d,%d,%d,", 
					site.getName(), 
					Watch.getTiming() / 1000.0,
					RollerConfiguration.getScoringFunction().compute(rule), 
					roller.getStatistics().getNodesExplored(),
					roller.getStatistics().getNumberAttempts(),
					rule.getTrainingDataset().getContexts().size(),
					computecontextsx(rule),
					computeAttributes(rule),
					computeNumRules(rule),
					computeNumSelectors(rule));
			
			Memory.collectGarbage();
			
			// ///////////////////////////////////////////////////////////////////////////
			
			
			System.out.printf("===> ROLLER'S TESTING %s%n", site.getName());
			
			Watch.start();
			{
				validator = new Validator();
				validator.setRule(rule);
				validator.setDataSet(RollerConfiguration.getTestSet());
				validator.validate();
			}
			Watch.stop();
			
			System.out.printf("%s%n", validator.getPerformance().toSummaryString(true));			
			System.out.printf("%s%n", validator.getPerformance().toClassDetailsString());
			System.out.printf("%s%n", validator.getPerformance().toMatrixString());		

			System.out.printf("<=== END TESTING %s - %s - %s%n", 
					site.getName(), 
					Formatter.toTime(Watch.getTiming()),
					Formatter.toSize(Memory.getFreeMemory()));

			// Testing Time (segs.), P, R, F1, ROC, Error Rate
			GlobalConfiguration.getOutput().printf("%f,%f,%f,%f,%f,%f%n", 
					Watch.getTiming() / 1000.0,
					validator.getPerformance().weightedPrecision(),
					validator.getPerformance().weightedRecall(), 
					validator.getPerformance().weightedFMeasure(),
					validator.getPerformance().weightedAreaUnderROC(),
					validator.getPerformance().errorRate()
					);
			
			GlobalConfiguration.getOutput().flush();
			
			Memory.collectGarbage();
		} catch (Throwable oops) {
			System.out.printf("%n%n%nOOPS! SOMETHING WENT WRONG!%n%n%n");
			oops.printStackTrace(System.out);
			GlobalConfiguration.getOutput().println();
			System.out.printf("%n%n%n");
		}
		
	}
		
	protected int computecontextsx(Rule rule) {
		int result;
		String text;
		Pattern nodePattern;
		Scanner scanner;
		MatchResult match;
		Set<String> nodeBag;
		
		System.out.println("COMPUTING contextSX");
		text = rule.getClassifier().toString();
		nodePattern = Pattern.compile("(node[0-9]*)_[a-z0-9_]+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		nodeBag = new HashSet<String>();
		scanner = new Scanner(text);
		while (scanner.findWithinHorizon(nodePattern, text.length()) != null) {
			  match = scanner.match();
			 nodeBag.add(match.group(1));			 			 
		}
		
		System.out.println(nodeBag);
		result = nodeBag.size();
		
		return result;
	}
	
	protected int computeAttributes(Rule rule) {
		int result;
		String text;
		Pattern nodePattern;
		Scanner scanner;
		MatchResult match;
		Set<String> attributeBag;
		
		System.out.println("COMPUTING ATTRIBUTES");
		text = rule.toString();
		nodePattern = Pattern.compile("(node[0-9]*_[a-z0-9_]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		attributeBag = new HashSet<String>();
		scanner = new Scanner(text);
		while (scanner.findWithinHorizon(nodePattern, text.length()) != null) {
			  match = scanner.match();
			 attributeBag.add(match.group());			 			 
		}
		
		System.out.println(attributeBag);
		result = attributeBag.size();
		
		return result;
	}
	
	protected int computeNumRules(Rule rule) {
		int result;
		
		result = 0; 
		
		return result;
	}

	protected int computeNumSelectors(Rule rule) {
		int result;
		
		result = 0;
		
		return result;
	}

}
