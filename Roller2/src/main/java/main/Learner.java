package main;

import java.io.File;

import learners.GainFunction;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import rules.ScoringFunction;
import utilities.Utilities;
import walkers.RollerWalker;
import walkers.Walker;
import weka.classifiers.Classifier;
import configuration.Constants;
import configuration.GlobalConfiguration;
import configuration.RollerConfiguration;

public class Learner {


	public static void main(String[] args) {
		File root, results;
		String resultsFileName;
		Walker walker;
		
		analyseCommandLine(args);
		
		root = new File("./datasets");
		resultsFileName = Utilities.buildRollerFileName(root, Constants.RESULTS_FILE_BASENAME, Constants.RESULTS_FILE_EXTENSION);;		
		
		results = new File(resultsFileName);
		GlobalConfiguration.setOutput(results);
		
		walker = new RollerWalker();
		walker.setRoot(root);
		walker.run();
	}

	@SuppressWarnings("unchecked")
	private static void analyseCommandLine(String[] args) {
		Options options; 
		CommandLineParser parser;
		CommandLine commandLine;
		String datasetsFolder, learnerName, scoringFunctionName, gainFunctionName;
		ClassLoader clazzLoader;
		Class<Classifier> learnerClazz;
		Class<ScoringFunction> scoringFunctionClazz;
		Class<GainFunction> gainFunctionClazz;
		ScoringFunction scoringFunction;
		GainFunction gainFunction;
		

		try {
			options = new Options();
			options.addOption("d", true, "Datasets folder");
			options.addOption("l", true, "Learner class");
			options.addOption("s", true, "Scoring function class");
			options.addOption("g", true, "Gain function class");
			
			parser = new PosixParser();
			commandLine = parser.parse( options, args);
						
			clazzLoader = Learner.class.getClassLoader();
			
			datasetsFolder = commandLine.getOptionValue("d", "datasets");
			GlobalConfiguration.setDatasetsFolder(datasetsFolder);
			
			learnerName = commandLine.getOptionValue("l", "weka.classifiers.rules.PART");
			learnerClazz = (Class<Classifier>) clazzLoader.loadClass(learnerName);
			RollerConfiguration.setLearnerClazz(learnerClazz);
			
			scoringFunctionName = commandLine.getOptionValue("s", "rules.InformationContentFunction");
			scoringFunctionClazz = (Class<ScoringFunction>) clazzLoader.loadClass(scoringFunctionName);
			scoringFunction = scoringFunctionClazz.newInstance();
			RollerConfiguration.setScoringFunction(scoringFunction);
			
			gainFunctionName = commandLine.getOptionValue("g", "learners.InformationGainFunction");
			gainFunctionClazz = (Class<GainFunction>) clazzLoader.loadClass(gainFunctionName);
			gainFunction = gainFunctionClazz.newInstance();
			RollerConfiguration.setGainFunction(gainFunction);			
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
		
	}

}


