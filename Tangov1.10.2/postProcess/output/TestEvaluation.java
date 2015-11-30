package output;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plFiles.FileUtilities;

import utilities.ThreadMonitor;

import measures.EvaluationMeasures;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.engine.BranchOperations;

import knowledgeBase.BindingsOperations;
import knowledgeBase.KnowledgeBaseFactory;
import knowledgeBase.KnowledgeBaseOperations;
import knowledgeBase.PrologUtilities;
import data.Literal;
import data.Predicate;
import data.Term;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

public class TestEvaluation {
	
	private KnowledgeBaseFactory kbFactory;
	private File tmpFile;
	private OutputDataFile tmpOutput;
	
	
	public TestEvaluation(String fileName) throws IOException {
		this.tmpFile = File.createTempFile("tmp", ".pl");
		this.tmpOutput = new OutputDataFile(tmpFile);
		
		this.kbFactory = KnowledgeBaseFactory.getInstance(); 
		copyTargetPredicateInstances(this.tmpOutput);
		copySupportPredicatesInstances(this.tmpOutput);
		
		this.kbFactory = KnowledgeBaseFactory.reload(fileName);
		copySupportPredicatesInstances(this.tmpOutput);
		
//		writeConstantList(constantsList, this.tmpOutput);
		
	}

	public EvaluationMeasures getEvaluationResults(List<List<Node<Literal,InformationContext>>> branches) throws IOException {
		EvaluationMeasures result = new EvaluationMeasures();
		long startTime, endTime;
		
		startTime = ThreadMonitor.getCpuTime();
		KnowledgeBaseOperations kBase = kbFactory.getKnowledgeBaseOperations();
		Coverage origCoverage = kBase.getOrigCoverage();
		
		this.tmpOutput.flush();
		this.kbFactory = KnowledgeBaseFactory.reload(this.tmpFile.getAbsolutePath());
		
		Coverage coverageObtained = new Coverage();
		Set<Bindings> positiveBindings = new HashSet<Bindings>();
		Set<Bindings> negativeBindings = new HashSet<Bindings>();
		BindingsOperations operations = kbFactory.getBindingsOperations();
			
		for (List<Node<Literal, InformationContext>> branch : branches) {
			Literal root = branch.get(0).getDataNode();
			Literal rootClone = root.clone();
			List<Term> existingVarsToBody = new ArrayList<Term>(root.getArgs());
			existingVarsToBody.removeAll(BranchOperations.getVarsInPath(branch.get(branch.size()-1)));
			List<Literal> literalList = new ArrayList<Literal>();
			for (int i = 1; i < branch.size(); i++) {
				literalList.add(branch.get(i).getDataNode());
			}
			
			coverageObtained = operations.branchOrigMatch(origCoverage, rootClone, literalList);
			positiveBindings.addAll(coverageObtained.getPositiveBindings());
			negativeBindings.addAll(coverageObtained.getNegativeBindings());
		}
		
		endTime = ThreadMonitor.getCpuTime();
		
		coverageObtained = new Coverage();
		coverageObtained.setPositiveBindings(positiveBindings);
		coverageObtained.setNegativeBindings(negativeBindings);
		result.computePrecision(coverageObtained);
		result.computeRecall(origCoverage, coverageObtained);
		result.computeAccuracy(origCoverage, coverageObtained);
		result.computefMeasure(origCoverage, coverageObtained);
		result.setExtractionTime(endTime - startTime);
		
		tmpOutput.closeFile();
		this.tmpFile.deleteOnExit();
		
		
		return result;
	}
	
	private void copyTargetPredicateInstances(OutputDataFile output) throws IOException {
		KnowledgeBaseOperations kb = this.kbFactory.getKnowledgeBaseOperations();
		Literal targetLit = kb.getTargetLiteral();
		Predicate targetPred = targetLit.getPredicate();
		Set<String> instByPred = kb.loadPositiveExamples(targetPred.getName(), targetPred.getArity());
		
		String instancesToText;
		for (String string : instByPred) {
			string = PrologUtilities.processString(string);
			instancesToText = string + "\n";
			output.writeToFile(instancesToText);
		}
		instByPred = kb.loadPositiveExamples("not_" + targetPred.getName(), targetPred.getArity());
		
		for (String string : instByPred) {
			string = PrologUtilities.processString(string);
			instancesToText = string + "\n";
			output.writeToFile(instancesToText);
		}
	}

	private void copySupportPredicatesInstances(OutputDataFile output) throws IOException {
		KnowledgeBaseOperations kb = this.kbFactory.getKnowledgeBaseOperations();
		String targetPredicate;
		
		Map<String,Integer> predicates;
		predicates = kb.getNameAndArityOfAllDefinedPredicates();
		targetPredicate = kb.getTargetLiteral().getPredicate().getName();
		predicates.remove(targetPredicate);
		predicates.remove("not_" + targetPredicate);
		predicates.remove("mode");
		predicates.remove("constantsList");
		for (String pname : predicates.keySet()) {
			int arity = predicates.get(pname);
			Set<String> instByPred = kb.loadPositiveExamples(pname, arity);
			String instancesToText;
			for (String string : instByPred) {
				string = PrologUtilities.processString(string);
				instancesToText = string + "\n";
				output.writeToFile(instancesToText);
			}
		}
	}

//	private static void writeConstantList(
//			Map<String,Set<String>> constantsList, OutputDataFile output) throws IOException {
//	
//		for(String typeName: constantsList.keySet()) {
//			String instancesToText =  "constantsList(" + typeName +"," + constantsList.get(typeName).toString() + ").\n";
//			output.writeToFile(instancesToText);
//		}
//	}
}