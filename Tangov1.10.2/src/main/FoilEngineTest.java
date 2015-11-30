package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import measures.EvaluationMeasures;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import data.Literal;

import output.FoilOutputData;
import output.OutputDataFile;
import output.TestEvaluation;
import properties.PropertiesStore;
import utilities.FoilException;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.engine.FoilEngine;

public class FoilEngineTest {

	private static String trainingFileName;
	private static String testFileName;
	private static String outputDir;
	private static String goalPredicate;
	private static String stopCriteria;
	private static String analystType;
	private static int chunk = 0;
	private static String scoringFunction;
	private static String[] predicates = null;
	private static String propertiesFilePath;
	private static String recursionAllowed; // to look for an order by exploring the constant values that the variables take
	private static String tryOrder; // to look for an order between the arguments
	private static String bestCandidatesSelectionStrategy;


	/**
	 * @param args
	 * 
	 * 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws IOException {
		
//		args = new String[12];
//		args[0] = "E:/Analysis12-Opc2-taken-from-file/trainingInputFiles/Books/1-crossValidation_www.abebooks.com/author/fold1/trainingFile.pl";
//		args[1] = "";
//		args[2] = "E:/Analysis12-Opc2-taken-from-file";
//		args[3] = "author";
//		args[4] = "DEFAULTSTOPCRITERIA";
//		args[5] = "ALPHABETA_ANALYSER";
//		args[6] = "0";
//		args[7] = "INFORMATIONGAIN";
//		args[8] = "E:/Analysis12-Opc2-taken-from-file/configuration.properties";
//		args[9] = "false"; // true: recursion allowed, false: recursion not allowed
//		args[10] = "false"; // true: try order between parameters, false: do not try
//		args[11] = "FOILHEURISTIC"; //FOILHEURISTIC, MAXIMUMGAIN


		//Reading configuration parameters
		if (args.length > 11) {
			//deberíamos hacerlo con todos los parámetros
			trainingFileName = args[0];
			testFileName = args[1];
			outputDir = args[2] ;
			goalPredicate = args[3];
			stopCriteria = args[4];
			analystType = args[5];
			chunk = Integer.parseInt(args[6]);
			scoringFunction = args[7];
			propertiesFilePath = args[8];
			recursionAllowed = args[9];
			tryOrder = args[10];
			bestCandidatesSelectionStrategy = args[11];
			
			if (args.length > 12) {
				predicates = new String [args.length - 11];
				int j = 0;
				for(int index = 11; index < args.length; index++) {
					predicates[j] = args[index];
					j++;
				}
				updateCwaFile();
			}
			updatePropertiesFile(trainingFileName);
		}
		
		FoilEngine fE = new FoilEngine(outputDir);
		FoilOutputData result = fE.run();
		
		if (!args[1].isEmpty()) {
			List<List<Node<Literal, InformationContext>>> branches = result.getBranches();
			if (branches.isEmpty())
				System.out.println("\nThere are no rules");
			else {
				
				//apply the ruleset in the test set
				//extract data and get the accuracy
//				updatePropertiesFile(testFileName); // need to be re-implemented
				TestEvaluation test = new TestEvaluation(testFileName);
				EvaluationMeasures ev = test.getEvaluationResults(branches);
				
				File f = new File(trainingFileName).getParentFile();
				File output = new File (outputDir + "/" + f.getParentFile().getName() + "_" + scoringFunction + "_" + f.getName() + "_evaluationResults.txt");
				if (output.exists())
					FileUtils.forceDelete(output);
				OutputDataFile outputMeasures = new OutputDataFile(output);
				outputMeasures.writeToFile("FMeasure = " + ev.getfMeasure() + "\n");
				outputMeasures.writeToFile("Accuracy = " + ev.getAccuracy() + "\n");
				outputMeasures.writeToFile("Precision = " + ev.getPrecision() + "\n");
				outputMeasures.writeToFile("Recall = " + ev.getRecall() + "\n");
				outputMeasures.writeToFile("Extraction Time = " + ev.getExtractionTime() + "\n");
				outputMeasures.closeFile();
			}
		}
	}
		
	
	private static void updatePropertiesFile(String knowledgeBase) {
		List<String> linesToWrite = new ArrayList<String>();
		String line = "";
		File propertiesFile = new File(propertiesFilePath);
		File tempFile = null;
		
		try {
			tempFile = File.createTempFile("configurationProperties", ".tmp");
			if (tempFile.exists()) {
				FileUtils.copyFile(propertiesFile, tempFile);
				FileUtils.forceDelete(propertiesFile);
				propertiesFile = new File(propertiesFilePath);
				
				LineIterator lineIt = FileUtils.lineIterator(tempFile);
				while (lineIt.hasNext()) {
					line = lineIt.nextLine();
					
					if (line.startsWith("knowledgeBase = "))
						line = "knowledgeBase = " + knowledgeBase;
					
					else if (line.startsWith("goalPredicate = "))
						line = "goalPredicate = " + goalPredicate;
					
					else if (line.startsWith("stopCriteriaType = "))
						line = "stopCriteriaType = " + stopCriteria;
					
					else if (line.startsWith("coverageAnalysType = "))
						line = "coverageAnalysType = " + analystType;
					
					else if (line.startsWith("CHUNK = "))
						line = "CHUNK = " + chunk;
					
					else if (line.startsWith("heuristicType = "))
						line = "heuristicType = " + scoringFunction;
					
					else if (line.startsWith("recursion = "))
						line = "recursion = " + recursionAllowed;
					
					else if (line.startsWith("tryOrder = "))
						line = "tryOrder = " + tryOrder;
					
					else if (line.startsWith("candidateSelectionStrategy = "))
						line = "candidateSelectionStrategy = " + bestCandidatesSelectionStrategy;
					
					linesToWrite.add(line);
				}
				lineIt.close();
				FileUtils.writeLines(propertiesFile, linesToWrite);
				FileUtils.forceDelete(tempFile);
				PropertiesStore.refreshFile(propertiesFilePath);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void updateCwaFile() {
		List<String> linesToWrite = new ArrayList<String>();
		String line = "", cwaFoilFileName = "";
		String userDir = System.getProperty("user.dir");
		userDir = userDir.replaceAll("\\\\", "/");
//		System.out.println(System.getProperty("user.dir"));
		try {
			cwaFoilFileName = PropertiesStore.getProperty("configurationArgsFile");
		} catch (FoilException e1) {
			e1.printStackTrace();
		}
		cwaFoilFileName = cwaFoilFileName.replaceFirst(".", userDir);
//		cwaFoilFileName = userDir + "/input/cwaFoil.pl"; 
		File cwaFoilFile = new File(cwaFoilFileName);
		File tempFile = null;
		
		try {
			tempFile = File.createTempFile("cwaFoil", ".tmp");
			if (tempFile.exists()) {
				FileUtils.copyFile(cwaFoilFile, tempFile);
				FileUtils.forceDelete(cwaFoilFile);
				cwaFoilFile = new File(cwaFoilFileName);
				
				LineIterator lineIt = FileUtils.lineIterator(tempFile);
				while (lineIt.hasNext()) {
					line = lineIt.nextLine();
					
					if (line.startsWith("foil_predicates([")) {
						line = "foil_predicates([";
						for (int i = 0; i < predicates.length; i++) {
							line += predicates[i] + ", ";
						}
						line = line.substring(0, line.lastIndexOf(", ")) + "]).";
					}	
					linesToWrite.add(line);
				}
				lineIt.close();
				FileUtils.writeLines(cwaFoilFile, linesToWrite);
				FileUtils.forceDelete(tempFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//para obtener el consumo de memoria
//try {
//	Memory.getMemoryUsage();
//} catch (Exception e) {
//	e.printStackTrace();
//}