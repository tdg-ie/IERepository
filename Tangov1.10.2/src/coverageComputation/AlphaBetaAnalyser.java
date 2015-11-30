package coverageComputation;

import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.HeuristicStrategyContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import knowledgeBase.BindingsOperations;
import knowledgeBase.KnowledgeBaseFactory;
import knowledgeBase.SwiProlog;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.data.NodePair;
import branch_and_bound.data.NodeType;
import branch_and_bound.engine.BranchOperations;
import thresholds.Thresholds;
import utilities.Utilities;

import data.Literal;
import data.Sign;
import data.Term;
import dataCoverage.Bindings;
import dataCoverage.Coverage;
import dataCoverage.CoveragePair;

public class AlphaBetaAnalyser extends Analyser{
	
	private ConfusionMatrix origcmL = null;
	private ConfusionMatrix newcmL = null;
	private ConfusionMatrix nowcmL = null;
	
	private ConfusionMatrix origcmNL = null;
	private ConfusionMatrix newcmNL = null;
	private ConfusionMatrix nowcmNL = null;
	
	private ExtraCoverageData extra = null;
	
	public AlphaBetaAnalyser(HeuristicStrategyContext heuristicStrategyContext) {
		this.heuristic = heuristicStrategyContext.getHeuristicStrategy();
		extra = new ExtraCoverageData();
	}
	
	public AnalysePositiveExamplesOutputTransferObject analysePositiveExamples(AnalysePositiveExamplesInputTransferObject apeito){
		AnalysePositiveExamplesOutputTransferObject result = new AnalysePositiveExamplesOutputTransferObject();
		boolean end = false;
		int newFound = 0, nowFound = 0;
		Map<Term, Map<Term, Boolean>> possibleDuplicateVars = new HashMap<Term, Map<Term,Boolean>>();
		
		NodePair<Literal, InformationContext> nodePair = apeito.getNodePair();
		Node<Literal, InformationContext> node = nodePair.getNode();
		Node<Literal, InformationContext> negatedNode = nodePair.getNegatedNode();
		Node<Literal, InformationContext> parentNode = apeito.getParent();
		Literal rootLiteral = node.getRoot().getDataNode();
		
		Thresholds thresholds = apeito.getThresholds();
		
		Coverage currentCoverage = parentNode.getInformationContext().getCoverage();
		Set<Bindings> originalPositiveCoverage = parentNode.getInformationContext().getOrigCoverage().getPositiveBindings();
		int currentNOrigPos = originalPositiveCoverage.size();
		int currentNPos = currentCoverage.getPositiveBindings().size();
		int currentNNeg = currentCoverage.getNegativeBindings().size();
		thresholds.setCurrentNNeg(currentNNeg);
		
		Set<String> origPosCovered = new HashSet<String>();
		Set<String> origPosCoveredL = new HashSet<String>();
		Set<String> origPosCoveredNL = new HashSet<String>();
		Bindings currentBinding = null;
		String origBinding = null;
		
		KnowledgeBaseFactory knowledgeBase = KnowledgeBaseFactory.getInstance();
		BindingsOperations bop = knowledgeBase.getBindingsOperations();
		
		Literal literal = node.getDataNode();
		InformationContext icNode = node.getInformationContext();
		InformationContext icNegatedNode = negatedNode.getInformationContext();
	
		extra.setCurrentNOrigPos(currentNOrigPos);
		extra.setCurrentNPos(currentNPos);
		extra.setCurrentNNeg(currentNNeg);
		
		boolean determinateLiteral = literal.hasUnboundedVars();
		
		AlphaBetaPruning alphaBetaP = new AlphaBetaPruning(); 
		
		this.confusionMatrixListL = new ConfusionMatrixList();
		this.origcmL = new ConfusionMatrix();
		this.newcmL = new ConfusionMatrix();
		this.nowcmL = new ConfusionMatrix();
		
		this.confusionMatrixListNL = new ConfusionMatrixList();
		this.origcmNL = new ConfusionMatrix();
		this.newcmNL = new ConfusionMatrix();
		this.nowcmNL = new ConfusionMatrix();
		
		Set<Term> pathVars = new HashSet<Term>(BranchOperations.getVarsInPath(parentNode));
		pathVars.addAll(node.getDataNode().getVars());
		
		if (literal.hasUnboundedVars()) {
			possibleDuplicateVars = this.CheckPossibleDuplicateVars(pathVars, literal);
			possibleDuplicateVars = this.CheckPossibleEqualityVC(possibleDuplicateVars, literal);
		}
		
		/**	POSITIVES TUPLES **/
		extra.setTupleSign(Sign.POSITIVE);
		extra.setOrigPos(0);
		Set<Bindings> newPosBindings = null;
		
//		System.out.println(node.getDataNode().toString());
		Iterator<Bindings> currentBindingsIterator = parentNode.getInformationContext().getCoverage().getPositiveBindings().iterator();
		while (currentBindingsIterator.hasNext() && !end) {
			currentBinding = currentBindingsIterator.next();
//			System.out.println(currentBinding.toString());
			origBinding = BindingsOperations.getOrigBinding(currentBinding, rootLiteral.getArgs()).toString();
			
			newFound = possibleDuplicateVars.isEmpty() ? bop.nMatches(currentBinding, literal) : (newPosBindings = bop.match(currentBinding, literal)).size();  
			
			if (newFound > 0) { //positive binding covered by non negated literal
			
				nowFound = 1;
				nowcmL.incTruePositives(nowFound);
				newcmL.incTruePositives(newFound);
				nowcmNL.incFalseNegatives(nowFound);
				newcmNL.incFalseNegatives(newFound);
				
				determinateLiteral &= (newFound == 1);
				//determinateLiteral &= (rule.getBody().isEmpty() ? newFound == 1 : (newFound - nowFound) == 0);
				
				if (!possibleDuplicateVars.isEmpty())
					possibleDuplicateVars = this.CheckNewVars(possibleDuplicateVars, newPosBindings, Sign.POSITIVE);
			
				origPosCovered.add(origBinding);
				if (origPosCoveredL.add(origBinding) == true)
					origcmL.incTruePositives();
				
				extra.setLiteralSign(Sign.POSITIVE);
				extra.setDeterminate(determinateLiteral);
				extra.setOrigTruePosNegatedLit(origcmNL.getTruePositives());
				extra.setNowTruePosLit(nowcmL.getTruePositives());
				
				if (alphaBetaP.checkPrune(extra, thresholds)) {
					result.setPt(PruneType.ALPHABETAPRUNE);
					end = true;
				}
				extra.setOrigPos(origPosCovered.size());
			}
			else {
				nowFound = 1;
				determinateLiteral = false;
					
				nowcmNL.incTruePositives(nowFound);
				newcmNL.incTruePositives(nowFound);
				nowcmL.incFalseNegatives(nowFound);
				newcmL.incFalseNegatives(nowFound);
					
				origPosCovered.add(origBinding);
				if ((origPosCoveredNL.add(origBinding)) == true)
					origcmNL.incTruePositives();
					
				extra.setLiteralSign(Sign.NEGATIVE);
				extra.setDeterminate(false);
				extra.setOrigTruePosLit(origcmL.getTruePositives());
				extra.setNowTruePosNegatedLit(nowcmNL.getTruePositives());
				if (alphaBetaP.checkPrune(extra, thresholds)) {
					result.setPt(PruneType.ALPHABETAPRUNE);
					end = true;
//						System.out.println(node.getDataNode().toString() + "PODADO (LITNeg, Pos)");
				}
				extra.setOrigPos(origPosCovered.size());
			}
		}
		
		origcmL.setFalseNegatives(currentNOrigPos - origPosCoveredL.size());
		origcmNL.setFalseNegatives(currentNOrigPos - origPosCoveredNL.size());
		
		// updating confusion Matrix of the non negated node
		this.confusionMatrixListL.setOrigcm(origcmL);
		this.confusionMatrixListL.setNewcm(newcmL);
		this.confusionMatrixListL.setNowcm(nowcmL);
		icNode.setConfusionMatrixList(confusionMatrixListL);
		
		// updating confusion Matrix of the negated node
		this.confusionMatrixListNL.setNewcm(newcmNL);
		this.confusionMatrixListNL.setNowcm(nowcmNL);
		this.confusionMatrixListNL.setOrigcm(origcmNL);
		icNegatedNode.setConfusionMatrixList(confusionMatrixListNL);
		
		if (result.getPt().equals(PruneType.ALPHABETAPRUNE)) {
			Set<Bindings> remainingBindings = currentCoverage.getNegativeBindings();
			result.setPt(checkForPrune(literal, thresholds, parentNode, remainingBindings, new HashSet<String>()));
		}
		else if (result.getPt() == PruneType.NOTPRUNE) {
			if (possibleDuplicateVars.isEmpty())
				icNode.setType(determinateLiteral ? NodeType.DETERMINATE : NodeType.GAINFUL);
			else {
				if (icNode.getConfusionMatrixList().getOrigcm().getTruePositives() == 0)
					icNode.setType(NodeType.GAINFUL);
				else { 
					icNode.setType(determinateLiteral ? NodeType.DETERMINATE : NodeType.XGAINFUL);
					if (!alphaBetaP.isPossibleF()) // ????????????
						result.setPt(PruneType.DUPLICATEDVARSPRUNE);
				}
			}
		}
		else if(result.getPt() == PruneType.ALPHABETAPRUNE && icNode.isDeterminate()){
			icNode.setType(NodeType.GAINFUL);
		}
		
		icNegatedNode.setType(NodeType.GAINFUL);
		icNode.setScore(heuristic.score(node));
		icNegatedNode.setScore(heuristic.score(negatedNode));
	
		result.setAlphaBetaP(alphaBetaP);
		result.setNodePair(nodePair);
		result.setDuplicateVars(possibleDuplicateVars);
		
		return result;
	}

	private PruneType checkForPrune(Literal literal, Thresholds thresholds, Node<Literal,InformationContext> parent, Set<Bindings> remainingNegativeBindings,
			Set<String> origNL) {
		PruneType result = PruneType.ALPHABETAPRUNE;
		boolean end = false;
		int falsePositivesL = this.confusionMatrixListL.getNowcm().getFalsePositives();
		int falsePositivesNL = this.confusionMatrixListNL.getNowcm().getFalsePositives();
		int origFalsePositivesNL = this.confusionMatrixListNL.getOrigcm().getFalsePositives();
		
		int remainingNegBindings = 0;
		double newThreshold = 0;
		InformationContext ic_parent = parent.getInformationContext();
		double parent_score = ic_parent.getScore();
		KnowledgeBaseFactory knowledgeBase = KnowledgeBaseFactory.getInstance();
		BindingsOperations bop = knowledgeBase.getBindingsOperations();
		Iterator<Bindings> it = remainingNegativeBindings.iterator();
		
			if (literal.hasUnboundedVars() && 
					this.extra.getCurrentNPos() - nowcmNL.getTruePositives() < thresholds.getMinPos()  /* this literal has insuff gain */) {
				// check for subsumed arguments
				newThreshold = heuristic.threshold(parent, parent_score, thresholds);
				remainingNegBindings = this.extra.getCurrentNNeg() - falsePositivesL - falsePositivesNL;

				while (it.hasNext() && !end) {
					Bindings currentBinding = it.next();
					List<Term> rootArgs = parent.getRoot().getDataNode().getArgs();
					if (bop.nMatches(currentBinding, literal) <= 0) {
						falsePositivesNL++;
						String origBinding = BindingsOperations.getOrigBinding(currentBinding, rootArgs).toString();
						if (origNL.add(origBinding) == true)
							origFalsePositivesNL++;
					}
					
					/*  See if have found enough  */
				    if ( falsePositivesNL > newThreshold ) 
				    	end = true;
				    
				    remainingNegBindings--;
				    
				    /*  See whether not enough left  */

					if ( falsePositivesNL + remainingNegBindings <= newThreshold ) 
						end = true;
				}
				
				this.confusionMatrixListNL.getNowcm().setFalsePositives(falsePositivesNL);
				this.confusionMatrixListNL.getNewcm().setFalsePositives(falsePositivesNL);
				this.confusionMatrixListNL.getOrigcm().setFalsePositives(origFalsePositivesNL);
				
				if (falsePositivesNL > newThreshold)
					result = PruneType.ALPHABETAPRUNE_SUBSUMED;
			}
			
			return result;
	}

	public AnalyseOutputTransferObject analyseNegativeExamples(AnalyseNegativeExamplesInputTransferObject aneito){
		AnalyseOutputTransferObject result = new AnalyseOutputTransferObject();
		Boolean end = false;
		int newFound = 0, nowFound = 0;
		Map<Term, Map<Term, Boolean>> possibleDuplicateVars = new HashMap<Term, Map<Term,Boolean>>();
		
		Node<Literal, InformationContext> parentNode = aneito.getParent();
		Set<Bindings> originalNegativeCoverage = parentNode.getInformationContext().getOrigCoverage().getNegativeBindings();
		int currentNOrigNeg = originalNegativeCoverage.size();
		Thresholds thresholds = aneito.getThresholds();
		NodePair<Literal, InformationContext> nodePair = aneito.getNodePair();
		Node<Literal, InformationContext> node = nodePair.getNode();
		Node<Literal, InformationContext> negatedNode = nodePair.getNegatedNode();
		Literal literal = node.getDataNode();
		Literal rootLiteral = node.getRoot().getDataNode();
		InformationContext icNode = node.getInformationContext();
		InformationContext icNegatedNode = negatedNode.getInformationContext();
		
		Set<String> origNegCoveredL = new HashSet<String>();
		Set<String> origNegCoveredNL = new HashSet<String>();
		Bindings currentBinding = null;
		String origBinding = null;
		
		AlphaBetaPruning alphaBetaP = aneito.getAlphaBetaP();
		
    	double score = parentNode.getInformationContext().getScore();
    	
		// Since all pos tuples appear first in the tuple sets, thresholds for NewTNeg
    	// and NowFNeg can be set when the first neg tuple is encountered.
    	thresholds.setNewTNegThresh(heuristic.threshold(node, score, thresholds));
    	thresholds.setNowFNegThresh(heuristic.threshold(negatedNode, score, thresholds));
    	
//    	System.out.println("Threshold for NewTNeg " + thresholds.getNewTNegThresh());
//    	System.out.println("Threshold for NowFNeg " + thresholds.getNowFNegThresh());
		
    	KnowledgeBaseFactory knowledgeBase = KnowledgeBaseFactory.getInstance();
		BindingsOperations bop = knowledgeBase.getBindingsOperations();
		
		boolean determinateLiteral = icNode.isDeterminate();
		
		confusionMatrixListL = icNode.getConfusionMatrixList();
		this.origcmL = confusionMatrixListL.getOrigcm();
		this.newcmL = confusionMatrixListL.getNewcm();
		this.nowcmL = confusionMatrixListL.getNowcm();
		
		confusionMatrixListNL = icNegatedNode.getConfusionMatrixList();
		this.origcmNL = confusionMatrixListNL.getOrigcm();
		this.newcmNL = confusionMatrixListNL.getNewcm();
		this.nowcmNL = confusionMatrixListNL.getNowcm();
		
		possibleDuplicateVars  = aneito.getDuplicatedVars();
		
	    /**	NEGATIVE TUPLES **/
		extra.setTupleSign(Sign.NEGATIVE);
		Set<Bindings> newNegBindings = null;
		
		Iterator<Bindings> currentBindingsIterator = parentNode.getInformationContext().getCoverage().getNegativeBindings().iterator();
		while (currentBindingsIterator.hasNext() && !end) {
			currentBinding = currentBindingsIterator.next();
			origBinding = BindingsOperations.getOrigBinding(currentBinding, rootLiteral.getArgs()).toString();
//			System.out.println(currentBinding.toString());
			
			newFound = possibleDuplicateVars.isEmpty() ? bop.nMatches(currentBinding, literal) : (newNegBindings = bop.match(currentBinding, literal)).size();
			
			if (newFound > 0) {//caso negativo cubierto por el literal no negado
				
				nowFound = 1;
				
				newcmL.incFalsePositives(newFound);
				newcmNL.incTrueNegatives(newFound);
				nowcmL.incFalsePositives(nowFound);
				nowcmNL.incTrueNegatives(nowFound);
				
				determinateLiteral &= (newFound == 1);
				
				if (!possibleDuplicateVars.isEmpty())
					possibleDuplicateVars = this.CheckNewVars(possibleDuplicateVars, newNegBindings, Sign.NEGATIVE);
				
				extra.setLiteralSign(Sign.POSITIVE);
				extra.setDeterminate(determinateLiteral);
				
				extra.setNew_nowFalsePosLit(this.heuristic.getMaxNumberOfFalsePositivesforL(confusionMatrixListL));
				
				if (alphaBetaP.checkPrune(extra, thresholds)) { 
					result.setPt(PruneType.ALPHABETAPRUNE);
					end = true;
//					System.out.println(node.getDataNode().toString() + "PODADO (LITPos, Neg)");
				}
				
				if (origNegCoveredL.add(origBinding) == true)
					origcmL.incFalsePositives();
				
			}
			else {
				nowFound = 1;
				
				nowcmNL.incFalsePositives(nowFound);
				newcmNL.incFalsePositives(nowFound);
				nowcmL.incTrueNegatives(nowFound);
				newcmL.incTrueNegatives(nowFound);
				
//				determinateNegatedLiteral &= newNegBindings.size() <= 1;
				
				extra.setLiteralSign(Sign.NEGATIVE);
				extra.setDeterminate(false);
				extra.setNowFalsePosNegatedLit(this.heuristic.getMaxNumberOfFalsePositivesforNL(confusionMatrixListNL));

				if (alphaBetaP.checkPrune(extra, thresholds)) {
					result.setPt(PruneType.ALPHABETAPRUNE);
					end = true;
//						System.out.println(node.getDataNode().toString() + "PODADO (LITNeg, Neg)");
				}
				
				if (origNegCoveredNL.add(origBinding) == true)
					origcmNL.incFalsePositives();
			}
		}
		
		origcmL.setTrueNegatives(currentNOrigNeg - origNegCoveredL.size());
		origcmNL.setTrueNegatives(currentNOrigNeg - origNegCoveredNL.size());
		
		if (result.getPt().equals(PruneType.ALPHABETAPRUNE)) {
			Set<Bindings> remainingBindings = new HashSet<Bindings>();
			while (currentBindingsIterator.hasNext())
				remainingBindings.add(currentBindingsIterator.next());
			result.setPt(checkForPrune(literal, thresholds, parentNode, remainingBindings, origNegCoveredNL));
		}
		
		if (result.getPt() == PruneType.NOTPRUNE) {
			if (!icNode.getType().equals(NodeType.XDET) && !icNode.getType().equals(NodeType.XGAINFUL)) {
				if (possibleDuplicateVars.isEmpty())
					icNode.setType(determinateLiteral ? NodeType.DETERMINATE : NodeType.GAINFUL);
				else {
					if (icNode.getConfusionMatrixList().getOrigcm().getTruePositives() == 0) // ???
						icNode.setType(NodeType.GAINFUL);
					else { 
//						icNode.setType(icNode.getType().equals(NodeType.XDET) ? NodeType.XDET : NodeType.XGAINFUL);
						icNode.setType(determinateLiteral ? NodeType.XDET : NodeType.XGAINFUL);
						if (!alphaBetaP.isPossibleF())
							result.setPt(PruneType.DUPLICATEDVARSPRUNE);
					}
				}
			}	
//			else if (!possibleDuplicateVars.isEmpty())
//				result.setPt(PruneType.DUPLICATEDVARSPRUNE);
		}
		else if(result.getPt() == PruneType.ALPHABETAPRUNE && icNode.isDeterminate()){
			icNode.setType(NodeType.GAINFUL);
		}
		
		icNegatedNode.setType(NodeType.GAINFUL);
		icNode.setScore(heuristic.score(node));
		icNegatedNode.setScore(heuristic.score(negatedNode));
		
		NodePair<Literal, InformationContext> newNodePair = new NodePair<Literal, InformationContext>();

//		if (icNode.isDeterminate()){
//			icNode.setBits(0);
//			icNode.setNewRuleBits(rule.getLast().getInformationContext().getNewRuleBits());
//		}
//		else 
//			icNode.setNewRuleBits(mdl.getNewRuleBits());
		
		newNodePair.setNode(node);
		newNodePair.setNegatedNode(negatedNode);
		result.setNodePair(newNodePair);
		result.setDuplicatedVars(possibleDuplicateVars);
		return result;
	}

	@Override
	public AnalyseOutputTransferObject analyse(AnalyseInputTransferObject aito) {
		
		AnalyseOutputTransferObject result = null;
		double ruleBits = 0;
		double extraBits = 0;
		double newRuleBits = 0;
		
		double litBits = Bits.ldiLiteral(aito.getNRelations(), aito.getNTryArgs(), aito.isNegatedLitAllowed());
		
		AnalysePositiveExamplesInputTransferObject apeito = new AnalysePositiveExamplesInputTransferObject();
		
		Node<Literal, InformationContext> parentNode = aito.getParent();
		NodePair<Literal, InformationContext> nodePair = aito.getNodePair();
		Node<Literal, InformationContext> negatedNode = nodePair.getNegatedNode();
		Node<Literal, InformationContext> node = nodePair.getNode();
		node.getInformationContext().setBits(litBits);
		negatedNode.getInformationContext().setBits(litBits);
		
		apeito.setNodePair(nodePair);
		apeito.setParent(parentNode);
		apeito.setThresholds(aito.getThresholds());
		
		AnalysePositiveExamplesOutputTransferObject apeoto = this.analysePositiveExamples(apeito);
		
		AnalyseNegativeExamplesInputTransferObject aneito = new AnalyseNegativeExamplesInputTransferObject();
		aneito.setAlphaBetaP(apeoto.getAlphaBetaP());
		aneito.setNodePair(apeoto.getNodePair());
		aneito.setPt(apeoto.getPt());
		aneito.setThresholds(aito.getThresholds());
		aneito.setDuplicatedVars(apeoto.getDuplicatedVars());
		aneito.setParent(apeito.getParent());
		
		result = this.analyseNegativeExamples(aneito);
		
		InformationContext icNode = result.getNodePair().getNode().getInformationContext();
		
		ruleBits = Bits.ldiRule(parentNode.getPath());
		if (icNode.isDeterminate()) {
			litBits = 0;
			extraBits = litBits - Utilities.log2(aito.getPathDepth() - aito.getDetLitNumber() + 1+1E-3);
			newRuleBits = ruleBits + Utilities.max(0, extraBits);
			icNode.setNewRuleBits(newRuleBits);
			InformationContext icNegatedNode = result.getNodePair().getNegatedNode().getInformationContext();
			icNegatedNode.setNewRuleBits(newRuleBits);
		}
		else {
			extraBits = litBits - Utilities.log2(aito.getPathDepth() - aito.getDetLitNumber() + 1+1E-3);
			newRuleBits = ruleBits + Utilities.max(0, extraBits);
			icNode.setNewRuleBits(newRuleBits);
			InformationContext icNegatedNode = result.getNodePair().getNegatedNode().getInformationContext();
			icNegatedNode.setNewRuleBits(newRuleBits);
		}
		
		return result;
	}
	

	@Override
	public CoveragePair updateCoverage(Coverage ocr, Coverage cr, Literal l) {
		CoveragePair result = new CoveragePair();
		
		KnowledgeBaseFactory knowledgeBase = KnowledgeBaseFactory.getInstance();
		BindingsOperations bop = knowledgeBase.getBindingsOperations();
		
		Coverage result_ocr = new Coverage();
		
		Set<Bindings> positiveBindings = new HashSet<Bindings>();
		Set<Bindings> negativeBindings = new HashSet<Bindings>();
		Coverage result_ncr = new Coverage();
		
//		ocr.toString();
		
		for (Bindings b : cr.getPositiveBindings()) {
//			positiveBindings.addAll(bop.match(b, l, kBase));
			positiveBindings.addAll(bop.match(b, l));
		}
		
		for (Bindings b : cr.getNegativeBindings()) {
//			negativeBindings.addAll(bop.match(b, l, kBase));
			negativeBindings.addAll(bop.match(b, l));
		}
		
		result_ncr.setPositiveBindings(positiveBindings);
		result_ncr.setNegativeBindings(negativeBindings);
		
		result.setNewCoverage(result_ncr);
		
		result_ocr.setPositiveBindings(SwiProlog.getOriginalBindingsSetCovered(positiveBindings, ocr.getPositiveBindings()));
		result_ocr.setNegativeBindings(SwiProlog.getOriginalBindingsSetCovered(negativeBindings, ocr.getNegativeBindings()));
		
		result.setOriginalCoverage(result_ocr);
		
		return result;
	}
	
//	private Node<Literal, InformationContext> cloneNode(Node<Literal, InformationContext> nodeToClone) {
//		Node<Literal, InformationContext> result = new Node<Literal, InformationContext>();
//		List<Node<Literal, InformationContext>> children = new ArrayList<Node<Literal,InformationContext>>(nodeToClone.getChildren());
//		
//		Literal l = nodeToClone.getDataNode().clone();
//		InformationContext ic = nodeToClone.getInformationContext().clone();
//		
//		result.setDataNode(l);
//		result.setInformationContext(ic);
//		result.setParent(nodeToClone.getParent());
//		result.setChildren(children);
//		
//		return result;
//	}
}