package coverageComputation;

import knowledgeBase.KnowledgeBaseFactory;
import knowledgeBase.KnowledgeBaseOperations;
import utilities.Utilities;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.data.NodePair;
import data.Literal;
import dataCoverage.Coverage;
import dataCoverage.CoveragePair;

public class AnalyserWithMDL extends Analyser{

	private Analyser analyser = null;
	
	public AnalyserWithMDL(Analyser analyser) {
		this.analyser = analyser;
	}

	
	@Override
	public AnalyseOutputTransferObject analyse(AnalyseInputTransferObject aito) {
		AnalyseOutputTransferObject result = new AnalyseOutputTransferObject();
		boolean possibleT = true, possibleF = true;
		double ruleBits = 0, newRuleBits = 0, extraBits = 0;
		double litBits = Bits.ldiLiteral(aito.getNRelations(), aito.getNTryArgs(), aito.isNegatedLitAllowed());
		
		NodePair<Literal, InformationContext> nodePair = aito.getNodePair();
		Node<Literal, InformationContext> node = nodePair.getNode();
		Node<Literal, InformationContext> negatedNode = nodePair.getNegatedNode();
	
		InformationContext icNode = node.getInformationContext();
		InformationContext icNegatedNode = negatedNode.getInformationContext();
		
		icNode.setBits(litBits);
		icNegatedNode.setBits(litBits);
		
		Node<Literal, InformationContext> parentNode = aito.getParent();
		Coverage origBindingsSet = parentNode.getInformationContext().getOrigCoverage();
		
		MDL mdl = new MDL();
		
		KnowledgeBaseFactory kb = KnowledgeBaseFactory.getInstance();
		KnowledgeBaseOperations kBase = kb.getKnowledgeBaseOperations();

		int currentNOrigPos = origBindingsSet.getPositiveBindings().size();
		int currentNOrigTot = aito.getAllTuples();
		mdl.setAvailableBits(Utilities.encode(currentNOrigPos, currentNOrigTot) - parentNode.getInformationContext().getNewRuleBits());
		
		extraBits = litBits - Utilities.log2(aito.getPathDepth() - aito.getDetLitNumber() + 1+1E-3);
		if (!mdl.exploreLiteral(extraBits)) {
			result.setPt(PruneType.NOTENOUGHTBITS);
		}
		else {
			AnalysePositiveExamplesInputTransferObject apeito = new AnalysePositiveExamplesInputTransferObject();
			apeito.setNodePair(nodePair);
			apeito.setParent(parentNode);
			apeito.setThresholds(aito.getThresholds());
			
			AnalysePositiveExamplesOutputTransferObject apeoto = this.analysePositiveExamples(apeito);
			
			ruleBits = newRuleBits = Bits.ldiRule(parentNode.getPath());
			
			if (!apeoto.getNodePair().getNode().getInformationContext().isDeterminate()) {
				extraBits = litBits - Utilities.log2(aito.getPathDepth() - aito.getDetLitNumber() + 1+1E-3);
				newRuleBits = ruleBits + Utilities.max(0, extraBits);
			}
			
//			newRuleBits = ruleBits + Utilities.max(0, extraBits);
//			if (apeoto.getNodePair().getNode().getInformationContext().isDeterminate()) {
//				extraBits = 0 - Utilities.log2(aito.getPathDepth() - aito.getDetLitNumber() + 1+1E-3);
//				newRuleBits = ruleBits + Utilities.max(0, extraBits);
//			}
			 
				
			mdl.setNewRuleBits(newRuleBits);
		
			this.confusionMatrixListL = icNode.getConfusionMatrixList();
			this.confusionMatrixListNL = icNegatedNode.getConfusionMatrixList();
			
			int origNPosL = this.confusionMatrixListL.getOrigcm().getTruePositives() + this.confusionMatrixListL.getOrigcm().getFalsePositives();
			int origNPosNL = this.confusionMatrixListNL.getOrigcm().getTruePositives() + this.confusionMatrixListNL.getOrigcm().getFalsePositives();
		
			AlphaBetaPruning alphaBetaP = apeoto.getAlphaBetaP();
			possibleT = alphaBetaP.isPossibleT();
			possibleF = alphaBetaP.isPossibleF();
			
			if (!possibleT && !possibleF) {
				if (apeoto.getPt().equals(PruneType.ALPHABETAPRUNE_SUBSUMED))
					result.setPt(PruneType.ALPHABETAPRUNE_SUBSUMED);
				else
					result.setPt(PruneType.ALPHABETAPRUNE);
			}
			else {
				possibleT &= !mdl.checkMDLPrune(origNPosL, kBase.getOrigTuplesNumber());
				alphaBetaP.setPossibleT(possibleT);
//				extraBits += 1.0; //porque vamos a estudiar el literal negado
				possibleF &= !mdl.checkMDLPrune(origNPosNL, kBase.getOrigTuplesNumber());
				alphaBetaP.setPossibleF(possibleF);
				
				if (!possibleT && !possibleF)
			    	result.setPt(PruneType.MDLPRUNE);
			}
		
			if (result.getPt().equals(PruneType.NOTPRUNE)){
			
				AnalyseNegativeExamplesInputTransferObject aneito = new AnalyseNegativeExamplesInputTransferObject();
				aneito.setAlphaBetaP(apeoto.getAlphaBetaP());
				aneito.setNodePair(apeoto.getNodePair());
				aneito.setPt(apeoto.getPt());
				aneito.setParent(parentNode);
				aneito.setThresholds(aito.getThresholds());
				aneito.setDuplicatedVars(apeoto.getDuplicatedVars());
				result = this.analyseNegativeExamples(aneito);
		
				icNode = result.getNodePair().getNode().getInformationContext();
				icNegatedNode = result.getNodePair().getNegatedNode().getInformationContext();
				extraBits = litBits - Utilities.log2(aito.getPathDepth() - aito.getDetLitNumber() + 1+1E-3);
				newRuleBits = ruleBits + Utilities.max(0, extraBits);
				icNode.setNewRuleBits(icNode.isDeterminate() ? ruleBits: newRuleBits);
				icNegatedNode.setNewRuleBits(newRuleBits);
		    }
		}
		return result;
	}
	
	@Override
	protected AnalyseOutputTransferObject analyseNegativeExamples(
			AnalyseNegativeExamplesInputTransferObject aneito) {
		AnalyseOutputTransferObject result = null;
		if (this.analyser != null)
			result = this.analyser.analyseNegativeExamples(aneito);
		return result;
	}

	@Override
	protected AnalysePositiveExamplesOutputTransferObject analysePositiveExamples(
			AnalysePositiveExamplesInputTransferObject apeito) {
		
		AnalysePositiveExamplesOutputTransferObject result = null;
		if (this.analyser != null) 
			result = this.analyser.analysePositiveExamples(apeito);
		return result;
	}
	
	@Override
	public CoveragePair updateCoverage(Coverage ocr, Coverage cr, Literal l) {
		return this.analyser.updateCoverage(ocr, cr, l);
	}
}