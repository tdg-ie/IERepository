package branch_and_bound.engine;

import heuristics.ConfusionMatrix;
import heuristics.ConfusionMatrixList;
import heuristics.HeuristicStrategyContext;
import heuristics.HeuristicType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.data.NodePair;
import branch_and_bound.data.NodeType;
import branch_and_bound.selectBestCandidatesStrategies.BestCandidateSelectionType;
import branch_and_bound.selectBestCandidatesStrategies.BestCandidatesComparator;
import branch_and_bound.selectBestCandidatesStrategies.SelectBestCandidateStrategyContext;

import knowledgeBase.BindingsOperations;
import knowledgeBase.KnowledgeBaseFactory;
import knowledgeBase.KnowledgeBaseOperations;

import literalFactory.LiteralsFactory;
import literalFactory.LiteralsFactoryV6;
import literalsFactoryStrategies.IPredicateSortStrategies;
import literalsFactoryStrategies.PredefinedTargetSupportStrategy;


import output.FoilOutputData;
import output.OutputDataFile;
import properties.PropertiesStore;
import stopStrategies.StopCriteriaType;
import stopStrategies.StopStrategyContext;
import thresholds.Thresholds;
import utilities.FoilException;
import utilities.Utilities;

import combinatorial.CombinationGeneration;
import combinatorial.CombinationsWithoutRepetition;
import combinatorial.ICombinatorial;

import constantManagement.ConsistentPairs;
import constantManagement.OrderedConstants;
import constantManagement.OrderedConstantsTO;
import constantManagement.VarPair;
import coverageComputation.AnalyseInputTransferObject;
import coverageComputation.AnalyseOutputTransferObject;
import coverageComputation.Analyser;
import coverageComputation.AnalyserStrategyContext;
import coverageComputation.AnalyserType;
import coverageComputation.PruneType;
import data.Constant;
import data.Literal;
import data.LiteralPair;
import data.Predicate;
import data.Term;
import data.Type;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;

public class FoilEngine extends Branch_and_Bound<Node<Literal,InformationContext>, FoilOutputData>{
	
	private Map<Integer, LiteralsFactory> nodeFactories = null; //integer is the hash code of the node that contains a specific literal 
	private Map<Integer, Node<Literal,InformationContext>> negatedNodes;//links a node that contains a negated literal with a node that contains a not negated literal
	private Map<String, List<Constant>> partialConstantSet = null; //constant sort by its type
	private List<Predicate> predicateList = null;
	private List<Term> variableList = null;
	private List<ConsistentPairs> orderedConstant = null;
	private OrderedConstantsTO constantsData;
	private List<VarPair> orderedVars = null;
	private List<Node<Literal, InformationContext>> exploredDetLitList = null;
	
	private int chunk = 0;
	private int nDet = 0;
	private int determinancyDepth = 0, maxDepth = 0;
	private int maxCheckPoints = 0;
	private int backtrackingNumber = 0;
	private int allTuples = 0;
	private boolean negatedLiteralsAllowed = true;
	private boolean exploredDetLit = false;
	private String goalPredicate = "";
	private String outputDir = "";
	private boolean recursion;
	private boolean tryOrder;
	private OutputDataFile trace = null;
	private int constantOrderDirection = 0;

	List<Node<Literal,InformationContext>> savePoints = new ArrayList<Node<Literal,InformationContext>>();
	List<Node<Literal,InformationContext>> savePointsVisited = new ArrayList<Node<Literal,InformationContext>>();
	List<Node<Literal,InformationContext>> mostGeneralNodesPruned = null;
	
	private List<Node<Literal,InformationContext>> currentPath; 
	
	private LiteralsFactory lf = null;
	private List<Term> newVars = null;
	
	private Analyser analyser = null;
	private AnalyseInputTransferObject aito = null;

	private HeuristicStrategyContext heuristic = null;
	private IPredicateSortStrategies strategy = null;
	private StopStrategyContext stopStrategy = null;
	
	private BestCandidatesComparator fc = null;
	
	private Thresholds thresholds = null;
	private KnowledgeBaseFactory knowledgeBase = null;
	private KnowledgeBaseOperations kbo = null;
	private BindingsOperations bop = null;
	
	public FoilEngine(String outputDir) throws IOException {
		String heuristicType = "";
		String stopCriteriaType = "";
		String coverageAnalystType = "";
		String candidateSelectionStrategy = "";
		int chunk = 0;
		this.outputDir = outputDir;
		this.outputData = new FoilOutputData();
		boolean negatedLiteralsAllowed = true;
		
		try {
			negatedLiteralsAllowed = Boolean.parseBoolean(PropertiesStore.getProperty("negatedLiteralsAllowed"));
			heuristicType = PropertiesStore.getProperty("heuristicType");
			stopCriteriaType = PropertiesStore.getProperty("stopCriteriaType");
			chunk = Integer.parseInt(PropertiesStore.getProperty("CHUNK"));
			maxCheckPoints = Integer.parseInt(PropertiesStore.getProperty("checkPointsNumber"));
			coverageAnalystType = PropertiesStore.getProperty("coverageAnalysType");
			goalPredicate = PropertiesStore.getProperty("goalPredicate");
			determinancyDepth = Integer.parseInt(PropertiesStore.getProperty("determinacyDepth"));
			recursion = Boolean.parseBoolean(PropertiesStore.getProperty("recursion"));
			tryOrder = Boolean.parseBoolean(PropertiesStore.getProperty("tryOrder"));
			candidateSelectionStrategy = PropertiesStore.getProperty("candidateSelectionStrategy");
		} catch (FoilException e) {
			e.printStackTrace();
		}
		
		HeuristicType ht = HeuristicType.valueOf(heuristicType);
		this.heuristic = new HeuristicStrategyContext(ht);
		
		//Loading data...
		
		this.knowledgeBase = KnowledgeBaseFactory.getInstance();
		this.kbo = knowledgeBase.getKnowledgeBaseOperations();
		this.bop = knowledgeBase.getBindingsOperations();
		
		thresholds = new Thresholds();
		this.strategy = new PredefinedTargetSupportStrategy();
		
		SelectBestCandidateStrategyContext  bestCandidateStrategy = new SelectBestCandidateStrategyContext(BestCandidateSelectionType.valueOf(candidateSelectionStrategy));
		this.fc = bestCandidateStrategy.getStrategy();
		
		StopCriteriaType sct = StopCriteriaType.valueOf(stopCriteriaType);
		this.stopStrategy  = new StopStrategyContext(sct);
	
		Coverage origCoverage = kbo.getOrigCoverage();
		InformationContext ic = new InformationContext();
		ic.setOrigCoverage(origCoverage);
		ic.setCoverage(origCoverage);
		
		//Loading targetNode...
		this.targetNode = new Node<Literal, InformationContext>();
		this.targetNode.setDataNode(kbo.getTargetLiteral());
		this.targetNode.setInformationContext(ic);
		
		this.predicateList = kbo.getDefinedPredicates();
		this.predicateList.addAll(kbo.getPredefinedPredicates());
		this.partialConstantSet = kbo.getPartialConstantSet();
		if (tryOrder)
			constantsData = OrderedConstants.putConstantInOrder();
		else
			this.constantsData = new OrderedConstantsTO();
		
		this.orderedConstant = constantsData.getConsistentPairs();
		
		this.chunk = chunk;
		this.negatedLiteralsAllowed = negatedLiteralsAllowed;
		
		AnalyserStrategyContext analyserStrategyContext = new AnalyserStrategyContext(AnalyserType.valueOf(coverageAnalystType), heuristic);
		analyser = analyserStrategyContext.getAnalyser();
	
		aito = new AnalyseInputTransferObject();
		aito.setNegatedLitAllowed(negatedLiteralsAllowed);
		
		Coverage origCoverageTargetNode = this.targetNode.getInformationContext().getOrigCoverage();
		allTuples = origCoverageTargetNode.getPositiveBindings().size() + origCoverageTargetNode.getNegativeBindings().size();
		aito.setAllTuples(allTuples);
		
		update();
		
		this.resetSavedList();
	
		nodeFactories = new HashMap<Integer, LiteralsFactory>();
		if (negatedLiteralsAllowed)
			negatedNodes = new HashMap<Integer, Node<Literal, InformationContext>>();
	
	}
	
	@Override
	protected void update() {
		
		this.targetNode.getChildren().clear();
		this.currentNode = this.targetNode;
		
		currentPath = new ArrayList<Node<Literal,InformationContext>>();
		aito.setParent(this.targetNode);
		
		variableList = new ArrayList<Term>();
		for (Term term : targetNode.getDataNode().getArgs()) {
			if (!variableList.contains(term))
				variableList.add(term);
		}
		Variable.resetExistingVariableCounter(variableList.size());
		
		Coverage coverage = this.currentNode.getInformationContext().getCoverage();
		int p = coverage.getPositiveBindings().size();
		int n = coverage.getNegativeBindings().size();
		
		ConfusionMatrixList cml = new ConfusionMatrixList();
		ConfusionMatrix origcm = new ConfusionMatrix();
		ConfusionMatrix newcm = new ConfusionMatrix();
		ConfusionMatrix nowcm = new ConfusionMatrix();
		
		origcm.setTruePositives(p);
		origcm.setFalsePositives(n);
		nowcm.setTruePositives(p);
		nowcm.setFalsePositives(n);
		newcm.setTruePositives(p);
		newcm.setFalsePositives(n);
		
		cml.setNewcm(newcm);
		cml.setNowcm(nowcm);
		cml.setOrigcm(origcm);
		
		this.currentNode.getInformationContext().setConfusionMatrixList(cml);
		
		double targetScore = heuristic.getHeuristicStrategy().score(currentNode);
		targetNode.getInformationContext().setScore(targetScore);
		
		this.reset();
		exploredDetLitList = new ArrayList<Node<Literal,InformationContext>>();
		orderedVars = new ArrayList<VarPair>();
		this.maxDepth = 0;
		
//		this.updateOrderedVars(this.targetNode.getDataNode());

	}
	
	protected void resetSavedList() {
		this.savePoints = new ArrayList<Node<Literal,InformationContext>>();
		this.savePointsVisited = new ArrayList<Node<Literal,InformationContext>>();
		this.backtrackingNumber = 0;
	}
	
	@Override
	protected void reset() {
		exploredDetLit = false;
		thresholds.resetBestCover();
		thresholds.resetThresholds();
		thresholds.updateMaxPossibleGain(this.heuristic.getHeuristicStrategy().getMaxPossibleGain(currentNode));
		aito.setThresholds(thresholds);
		this.nDet = 0;
		this.mostGeneralNodesPruned = new ArrayList<Node<Literal,InformationContext>>();
		
	}

	@Override
	protected boolean bound() throws IOException {
		boolean result = false;
		Literal mostGeneralLiteral = null;
		StringBuffer buffer = new StringBuffer();
		String duplicatedVarsString = "";
		InformationContext icNode = null;
		InformationContext icNegatedNode = null;
		Node<Literal, InformationContext> currentNegatedNode = this.negatedNodes.get(currentNode.hashCode());
		
		LiteralPair lp = new LiteralPair();
		lp.setLiteral(currentNode.getDataNode());
		lp.setNegatedLiteral(currentNegatedNode.getDataNode());
		mostGeneralLiteral = isAnEquivalentLiteral(lp, false);
		 
		if (mostGeneralLiteral == null) {
			icNode = currentNode.getInformationContext();
			icNegatedNode = currentNegatedNode.getInformationContext();
			
			LiteralsFactory nodeFactory = this.nodeFactories.get(parent.hashCode());
			aito.setNRelations(nodeFactory.getPossiblePredNumber());
			aito.setNTryArgs(nodeFactory.getPossibleCombNumber(currentNode.getDataNode()));
			AnalyseOutputTransferObject aoto = null;
				
			NodePair<Literal, InformationContext> nodePair = new NodePair<Literal, InformationContext>();
			nodePair.setNode(currentNode);
			nodePair.setNegatedNode(currentNegatedNode);
			aito.setNodePair(nodePair);
			aito.setParent(parent);
			aito.setPathDepth(parent.getPath().size());
			
			aoto = analyser.analyse(aito);
			
			if (aoto.getPt().equals(PruneType.DUPLICATEDVARSPRUNE) || aoto.getPt().equals(PruneType.NOTPRUNE))
				if (icNode.getType().equals(NodeType.XDET) || icNode.getType().equals(NodeType.XGAINFUL)) {
					Map<Term, Map<Term, Boolean>> duplicatedVars = aoto.getDuplicatedVars();
					Iterator<Term> it = duplicatedVars.keySet().iterator();
					Iterator<Term> it2 = null;
					while (it.hasNext()) {
						Term t1 = it.next();
						it2 = duplicatedVars.get(t1).keySet().iterator();
						while (it2.hasNext()) {
							Term t2 = it2.next();
							if (t2 instanceof Variable)
								duplicatedVarsString += t2.getName() + (duplicatedVars.get(t1).get(t2)? "=+" : "=") + t1.getName() + "  ";
							else {
								duplicatedVarsString += t1.getName() + (duplicatedVars.get(t1).get(t2)? "=+" : "=") + t2.getValue() + "  ";
							}
							 
						}
					}
				}
			
			if (aoto.getPt() != PruneType.NOTENOUGHTBITS)
				this.outputData.incEvaluatedNodesNumber();
			
			if (aoto.getPt() == PruneType.NOTPRUNE) {
				
				nodePair = aoto.getNodePair();
				currentNode = nodePair.getNode();
				currentNegatedNode = nodePair.getNegatedNode();

//				if (icNegatedNode.isDeterminate()){
//					if (aoto.getDuplicatedVars().isEmpty()){
//						this.nDet++;
//						exploredDetLitList.add(currentNegatedNode);
//					}
//				}
				
				if (icNode.isDeterminate()) {
					if (aoto.getDuplicatedVars().isEmpty()){
						if (this.maxDepth < this.determinancyDepth) {
							this.nDet++;
							exploredDetLitList.add(currentNode);
						}
					}
				}
				
				if (this.maxDepth >= this.determinancyDepth)
					exploredDetLit = false;
				else
					exploredDetLit |= icNode.isDeterminate();
					
				icNode = currentNode.getInformationContext();
				icNegatedNode = currentNegatedNode.getInformationContext();
				
				double currentScore = aito.getParent().getInformationContext().getScore(); //currentNode
				
				icNode.setGain(assess(currentNode, currentScore));
				icNegatedNode.setGain(assess(currentNegatedNode, currentScore));
				
			}
			else if (aoto.getPt() != PruneType.NOTENOUGHTBITS)
				this.outputData.incPruneNodesNumber();
				
			if (aoto.getPt() == PruneType.ALPHABETAPRUNE_SUBSUMED) {
				this.mostGeneralNodesPruned.add(0,currentNode);
				aoto.setPt(PruneType.ALPHABETAPRUNE);
			}
			
			result = aoto.getPt()!= PruneType.NOTPRUNE;
			
			buffer.append(this.dataNodeToBuffer(currentNode, duplicatedVarsString));
			buffer.append(this.dataNodeToBuffer(currentNegatedNode, duplicatedVarsString));
			if (!aoto.getPt().equals(PruneType.NOTPRUNE))
				buffer.append(aoto.getPt().toString() + "\n");
		}
		else
			buffer.append(currentNode.getDataNode().toString() + "--> subsumed by " + mostGeneralLiteral.toString() + "\n");
	
		buffer.append("----------------------------------------------------------------\n");
		this.trace.writeToFile(buffer.toString());
		
		return result;
	}
	
	private StringBuffer dataNodeToBuffer(Node<Literal, InformationContext> node, String duplicatedVarsString) {
		StringBuffer result = new StringBuffer();
		//printing data of the recent evaluated node
		
		result.append(node.getDataNode().toString() + "\t");
		
		// printing all confusion matrices
		ConfusionMatrixList cmlL = node.getInformationContext().getConfusionMatrixList();
		InformationContext icNode = node.getInformationContext();
		
		// original confusion matrix
		result.append("[" + cmlL.getOrigcm().getTruePositives() +"/");
		result.append((cmlL.getOrigcm().getFalsePositives() + cmlL.getOrigcm().getTruePositives()) + "]\t");
		// now confusion matrix
		result.append("[" + cmlL.getNowcm().getTruePositives() +"/");
		result.append((cmlL.getNowcm().getFalsePositives() + cmlL.getNowcm().getTruePositives()) + "]\t");
		// new confusion matrix
		result.append("[" + cmlL.getNewcm().getTruePositives() + "/");
		result.append((cmlL.getNewcm().getFalsePositives() + cmlL.getNewcm().getTruePositives())+ "]\t");
		
		// if there exists duplicated variables
		if (!duplicatedVarsString.isEmpty() && node.getDataNode().isPositive())
			result.append(duplicatedVarsString + "\t");
		result.append("gain: " + Utilities.round(icNode.getGain()) + "\t" + icNode.getType());
		result.append((icNode.getType().equals(NodeType.XDET) || icNode.getType().equals(NodeType.XGAINFUL) ? " #" : "") + "\n");
		result.append("consumed bits: " + icNode.getBits() + "\tnewRuleBits: " + icNode.getNewRuleBits() + "\n");
		
		return result;
	}

	/**
	 * calculates the information gain and updates the thresholds
	 * @param confusionMatrixList
	 * @param l
	 * @return
	 */

	private double assess(Node<Literal, InformationContext> node, double currentScore) {
		double result = 0;
		Literal l = node.getDataNode();
		InformationContext ic = node.getInformationContext();
		ConfusionMatrix newCM = ic.getConfusionMatrixList().getNewcm();
		ConfusionMatrix origCM = ic.getConfusionMatrixList().getOrigcm();
		
		result = this.heuristic.getHeuristicStrategy().evaluate(currentScore, node, l.getUnboundedVars().size());
		
		if (result > thresholds.getBestLitGain()) {
			thresholds.updateBestLitGain(result);
			
			if (exploredDetLit) //debería de ejecutarse una sola vez...
				thresholds.updateMinUsefulGainToMax();
			else 
				thresholds.updateMinUsefulGain();
			
		}
		else 
			if (this.nDet > 0 && (thresholds.getMinUsefulGain() < thresholds.getPercentajeThreshold() * thresholds.getMaxPossibleGain() )) {
				thresholds.updateMinUsefulGainToMax();
			}
		
		thresholds.updateMinPos(currentScore);
		
		if (newCM.getFalsePositives() == 0 && newCM.getTruePositives() > 0) // if any evaluated literal completes the current rule, update bestCover
			thresholds.updateBestCover(origCM.getTruePositives());
//			thresholds.updateBestCover(ic.getCoverage().getPositiveBindings().size());
		
		thresholds.updateMinSaveableCover();
		
		return result;
	}

	@Override
	protected void addNodeAsChild() {
		parent.addChild(currentNode);
		parent.addChild(this.negatedNodes.get(currentNode.hashCode()));
		//sometimes we should not add negated literal if this node has information Gain <= 0.0
		currentNode.setParent(parent);
		this.negatedNodes.get(currentNode.hashCode()).setParent(parent);
	}

	@Override
	/**
	 * returns the not negated literals to evaluate in the next iteration, 
	 * but if the negated literals generation is allowed, this method creates 
	 * a hash map which contains the negated literals linked to the unnegated 
	 * literal by its hash code
	 */
	protected List<Node<Literal, InformationContext>> branch() throws IOException {
		List<Node<Literal, InformationContext>> result = null;
		List<LiteralPair> literalPairList = null;
		StringBuffer buffer = new StringBuffer("\nBranching node: ");
		buffer.append(this.print(currentNode) + "\n");
		buffer.append("\n----------------------------------------------------------------\n");
		this.trace.writeToFile(buffer.toString());
		
		if (nodeFactories.containsKey(currentNode))
			lf = nodeFactories.get(currentNode);
		else {
			lf = new LiteralsFactoryV6(strategy, predicateList, chunk, currentPath, this.constantOrderDirection);
			lf.setConstantSet(this.partialConstantSet);
			lf.setVariableList(variableList);
			if (this.newVars != null)
				lf.setNewGeneratedVars(this.newVars);
			else
				lf.resetNewVars();
		}
		
		if (currentPath.size() == 0)
			lf.resetNewVars();
		
		lf.setOrderedVars(orderedVars);
		literalPairList = lf.getNext();
		
		if (negatedLiteralsAllowed)
			result = this.createAllNodes(literalPairList);
		else 
			result = this.createNoNegatedNodes(literalPairList);
		
		this.nodeFactories.put(currentNode.hashCode(), lf);
		this.newVars = lf.getNewGeneratedVars();
		return result;
	}
	
	/**
	 * 
	 * @param literalList
	 * @return
	 */
	private List<Node<Literal, InformationContext>> createNoNegatedNodes(List<LiteralPair> literalList) {
		List<Node<Literal, InformationContext>> result = new ArrayList<Node<Literal, InformationContext>>();
		Node<Literal, InformationContext> n = null;
		for (LiteralPair lp: literalList) {
			n = new Node<Literal, InformationContext>();
			n.setDataNode(lp.getLiteral());
			n.setInformationContext(new InformationContext());
			n.setParent(this.currentNode);
			result.add(n);
		}
		return result;
	}
	
	private List<Node<Literal, InformationContext>> createAllNodes(List<LiteralPair> literalList) {
		List<Node<Literal, InformationContext>> result = new ArrayList<Node<Literal, InformationContext>>();
		Node<Literal, InformationContext> n = null;
		int hashCode = 0;
		Predicate p = null;
		Node<Literal, InformationContext> nparent = parent == null ? this.targetNode : this.parent;
		Bindings b = nparent.getInformationContext().getCoverage().getFirstPositiveBinding();
		
		for (LiteralPair lp: literalList) {	
//			System.out.println(lp.getLiteral().toString());
			p = lp.getLiteral().getPredicate();
			if ((p.isPredefined() && this.isAnEquivalentLiteral(lp, true) == null) || (this.AllowedTheseRepetitiousOldVars(lp) && this.isAnEquivalentLiteral(lp, true) == null)
					&& bop.possibleLiteral(lp, b)) {
				//Creating node that contains not negated literal
				n = new Node<Literal, InformationContext>();
				n.setDataNode(lp.getLiteral());
				n.setInformationContext(new InformationContext());
				n.setParent(this.currentNode);
				result.add(n); //negated literal is not added to the results list.
				hashCode = n.hashCode();
				
				//Creating node that contains the negated literal
				n = new Node<Literal, InformationContext>();
				n.setDataNode(lp.getNegatedLiteral());
				n.setInformationContext(new InformationContext());
				n.setParent(this.currentNode);

				//adds the node that contains the negated literal and it is linked with 
				//node that contains the same literal unless this is not negated
				negatedNodes.put(hashCode, n);
			}
		}
		return result;
	}

	/**
	 * It avoids evaluating literals that it is known beforehand that they will not cover any positive examples.
	 * For instance, if the literal predicate1(X0,X0,X1) is a candidate literal to be evaluated but we have 
	 * found out that the first and second arguments of predicate1 are not equals in any case, we cut it and do not evaluate.
	 * @param lp
	 * @return
	 */
	private boolean AllowedTheseRepetitiousOldVars(LiteralPair lp) {
		boolean result = true;
		Term t = null;
		List<Integer> positions = null;
		Map<Term, List<Integer>> allVarsMap = new HashMap<Term, List<Integer>>();
		Map<Term, List<Integer>> repetitiousVarsMap = new HashMap<Term, List<Integer>>();
		CombinationGeneration<Integer> comb = null;
		
		
		List<Term> args = lp.getLiteral().getArgs();
		// get the positions of the existing vars
		for (int i = 0; i < args.size(); i++) {
			t = args.get(i);
			if (t.isBound()) {
				if (allVarsMap.containsKey(t))
					positions = allVarsMap.get(t);
				else
					positions = new ArrayList<Integer>();
				positions.add(i);
				allVarsMap.put(t, positions);
			}
		}
		
		// remove non-duplicate vars
		for (Term term : allVarsMap.keySet()) {
			positions = allVarsMap.get(term);
			if (positions.size() > 1)
				repetitiousVarsMap.put(term, positions);
		}
		
		
		if (repetitiousVarsMap.size() > 0) {
			boolean[] argNotEqualTable = kbo.getArgNotEqualTable().get(lp.getLiteral().getPredicate().getName());
			// chech if duplicated vars are possible
			
			for (Iterator<Term> it = repetitiousVarsMap.keySet().iterator(); it.hasNext() && result;) {
				Term term = it.next();
				positions = repetitiousVarsMap.get(term);
				comb = new CombinationsWithoutRepetition<Integer>(2, positions.size());
				while (comb.hasNext() && result) {
					int[] combination = comb.next();
					int index = Utilities.argPair(combination[1], combination[0]);
					if (argNotEqualTable[index])
						result = false;
				}
			}
//			Arg[W] == V && R->ArgNotEq[ ArgPair(This,W) ];
		}
		
		return result;
	}

	/**
	 * Checks if a specific literal determines a new order between variables 
	 * that is to say, if arg1 < arg2 in a specific predicate p, a literal as p(A,B) that uses
	 * this predicate p, have to set the same order between its variables (A < B).
	 * @param bestLit candidate literal to add to the rule
	 */
	private void updateOrderedVars(Literal bestLit) {
		int i = 0;
		while (i < this.orderedConstant.size()) {
			ConsistentPairs consistentArgs = this.orderedConstant.get(i);
			if (consistentArgs.getPredicateName().equals(bestLit.isNegated()? "~" : "" + bestLit.getPredicate().getName())) {
				int firstArg = consistentArgs.getFirstArg();
				int secondArg = consistentArgs.getSecondArg();
				int sign = consistentArgs.getSign();
				Variable var1 = (Variable) bestLit.getArgs().get(firstArg);
				Variable var2 = (Variable) bestLit.getArgs().get(secondArg);
				VarPair vp = new VarPair(var1, var2, sign);
				if (!vp.belongs(orderedVars))
					this.orderedVars.add(vp);
			}
			i++;
		}
		if (this.orderedConstant.size() > 0)
			this.transitiveClosure();
	}
	
	/**
	 * Checks if new orders between variables can be found
	 */
	private void transitiveClosure() {
		int length = this.orderedVars.size();
		int i = 0, j = 0, sign1, sign2;
		Variable v1 = null, v2 = null;
		while (i < length) {
			VarPair vp1 = this.orderedVars.get(i);
			v1 = vp1.getVar1();
			v2 = vp1.getVar2();
			sign1 = vp1.getSign();
			j = 0;
			while (j < length) {
				if (i != j) {
					VarPair vp2 = this.orderedVars.get(j);
					sign2 = vp2.getSign();
					if (v1.equals(vp2.getVar2()) && sign1 == sign2){
						VarPair vpAux = new VarPair(vp2.getVar1(),v2, sign1);
						if (!vpAux.belongs(orderedVars) && !vp2.getVar1().equals(v2))
							this.orderedVars.add(vpAux);
					}
				}
				j++;
			}
			i++;
		}
	}

	
	@Override
	protected List<Node<Literal, InformationContext>> selectNode() throws IOException { //we can define other selection node strategies
		List<Node<Literal, InformationContext>> result = new ArrayList<Node<Literal, InformationContext>>();
		Node<Literal, InformationContext> selectedNode = null;
		Node<Literal,InformationContext> candidateHead, previousNode;
		List<Node<Literal,InformationContext>> candidateBody;
		List<Literal> candidateLiterals;
		String line = "";
//		boolean regrowRule = false;
		Coverage origCoverage = null;
		StringBuffer buffer = new StringBuffer();
		
		/**
		 * STEP 1: Sort the nodes generated from the best one to the worst one 
		 * according to several criteria as its Information Gain, if the node is Determinate, 
		 * if it adds new variables, and so on.
		 */
//		System.out.println(thresholds.getMaxPossibleGain());
		fc.setMaxGain(thresholds.getMaxPossibleGain());
		Collections.sort(this.candidateNodes, fc);
		
		/**
		 * STEP 2: Selecting the best one or the best ones if they are determinate literals. The selected nodes will take part 
		 * of the rule in production
		 */
		
		result = this.selectBestLiterals();
		selectedNode = result.get(result.size()-1);
		if (selectedNode.getDataNode().hasUnboundedVars()) this.maxDepth++; 
		
		/**
		 * STEP 3: Updating data coverage of the selected nodes and checking if we can regrow the rule
		 */
		
		if (result.size() == 1) {
			Literal selectedNodeLit;
			if ((selectedNodeLit = selectedNode.getDataNode()).getPredicate().isTarget()) {
				reverseConstantOrderIfNecessary(this.orderedVars, selectedNodeLit);
				this.lf.setConstantOrderDirection(this.constantOrderDirection);
			}
			
//			List<Node<Literal, InformationContext>> newBody = checkRegrowRule(selectedNode);
//			if (this.currentPath.size() > newBody.size()) {
//				regrowRule = true;
//				this.currentPath = newBody;
//			}
			
			this.updateCoverage(selectedNode, true);
			// adding the selected node to the rule
			this.currentPath.add(selectedNode);
			
			// if the new body changed in relation to the current body means there are literals that have been removed
//			if (regrowRule) {
//				//reset some values (existing variables, new variables and the order among the variables)
//				this.variableList = this.getUnboundedVars(this.targetNode, this.currentPath);
//				Variable.resetExistingVariableCounter(BranchOperations.getMaxVarIndex(this.currentPath)+1);
//				this.newVars.clear();
//				this.orderedVars.clear();
//				this.updateOrderedVars(this.targetNode.getDataNode());
//				if (recursion) 
//					this.checkNewOrdersInVars(selectedNode);
//				this.maxDepth = 0;
//				boolean previousDet = false;
//				for(Node<Literal, InformationContext> n : this.currentPath) {
//					this.updateOrderedVars(n.getDataNode());
//					if (recursion) 
//						this.checkNewOrdersInVars(selectedNode);
//					
//					//computing the depth of determinate literals
//					if (selectedNode.getDataNode().hasUnboundedVars() && !previousDet) this.maxDepth++;
//					if(selectedNode.getInformationContext().isDeterminate())
//						previousDet = true;
//					else 
//						previousDet = false;
//				}
//			}
//			else {
				this.boundNewVars(selectedNode.getDataNode());
				this.updateOrderedVars(selectedNode.getDataNode());
				if (recursion) 
					this.checkNewOrdersInVars(selectedNode);
				//bounding free vars and adding to variableSet
				
//			}
			// TENEMOS QUE ACTUALIZAR LOS BITS??
			result.clear();
			result.add(selectedNode);
			
		}
		// if we add more than one node (or literal) to the rule in production (because the selected nodes are DET)
		else {
			boolean addRecursiveDetLiteral;
			Literal selectedNodeLit;
			int currentPathSize = this.currentPath.size();
			Node<Literal, InformationContext> lastAddedNode = currentPathSize > 0 ? this.currentPath.get(this.currentPath.size()-1) : this.targetNode;
			List<Node<Literal, InformationContext>> result_copy = new ArrayList<Node<Literal, InformationContext>>(result);
			List<Term> newVarList = new ArrayList<Term>();
			List<Term> currentArgs = null;
			rewriteNodes(result);
			
			candidateHead = this.cloneNode(this.targetNode);
			candidateBody = new ArrayList<Node<Literal,InformationContext>>(this.currentPath);
			candidateLiterals = new ArrayList<Literal>();
			for (Node<Literal, InformationContext> n : this.currentPath)
				candidateLiterals.add(n.getDataNode());
			
			origCoverage = this.targetNode.getInformationContext().getOrigCoverage();
			
			addRecursiveDetLiteral = true;
			previousNode = result.get(0);
			if ((selectedNodeLit = previousNode.getDataNode()).getPredicate().isTarget()) {
				addRecursiveDetLiteral = reverseConstantOrderIfNecessary(this.orderedVars, selectedNodeLit);
				this.lf.setConstantOrderDirection(this.constantOrderDirection);
			}
			
			if (addRecursiveDetLiteral) {
				this.updateCoverage(previousNode, true);
				newVarList.addAll(this.boundNewVars(previousNode.getDataNode())); //bounding free vars and adding to variableSet
				this.updateOrderedVars(previousNode.getDataNode());
				if (recursion) 
					this.checkNewOrdersInVars(previousNode);
				candidateLiterals.add(previousNode.getDataNode());
				candidateBody.add(previousNode);
			}
			
			for (int i = 1; i < result.size(); i++) {
				addRecursiveDetLiteral = true;
				selectedNode = result.get(i);
				if ((selectedNodeLit = selectedNode.getDataNode()).getPredicate().isTarget()) {
					addRecursiveDetLiteral = reverseConstantOrderIfNecessary(this.orderedVars, selectedNodeLit);
					this.lf.setConstantOrderDirection(this.constantOrderDirection);
				}
				if (addRecursiveDetLiteral) {
					this.updateCoverage(selectedNode, true);	
					newVarList.addAll(this.boundNewVars(selectedNode.getDataNode())); //bounding free vars and adding to variableSet
					this.updateOrderedVars(selectedNode.getDataNode());
					candidateLiterals.add(selectedNode.getDataNode());
					candidateBody.add(selectedNode);
					
					Map<Term, Variable> repeatedVars = this.removeRepetitiousVars(newVarList, selectedNode);
					if (repeatedVars.size() > 0) {
						currentArgs = selectedNode.getDataNode().getArgs();
						boolean ok = false;
						for (Term term : currentArgs) {
							if (term.belongs(newVarList)!= -1 && !repeatedVars.containsKey(term))
								ok = true;
							else if (term.belongs(newVarList) != -1 && repeatedVars.containsKey(term)){
								term.setType(new Type());
								this.variableList.remove(term);
								this.orderedVars = this.replaceVarInOrderedVars(this.orderedVars, term,repeatedVars.get(term));
							}
						}
						if (!ok) {// don't add the determinate literal to the resulting list of nodes.
							result_copy.remove(selectedNode);
							previousNode.getChildren().remove(selectedNode);
							for (Node<Literal, InformationContext> child: selectedNode.getChildren()) {
								child.setParent(previousNode);
								previousNode.addChild(child);
							}
							selectedNode.setParent(lastAddedNode); //we add again to the exploration tree
							lastAddedNode.addChild(selectedNode);
							candidateBody.remove(selectedNode);
							candidateLiterals.remove(selectedNode.getDataNode());
							selectedNode = previousNode;
						}
						else {// CHEQUEAR ESTA PARTE DETENIDAMENTE
							updateDeterminateLiteral(selectedNode, repeatedVars);
							
							selectedNode.getInformationContext().setCoverage(bop.match(origCoverage, candidateHead.getDataNode(), candidateLiterals));
							selectedNode.getInformationContext().setOrigCoverage(bop.origmatch(origCoverage, candidateHead, candidateLiterals));
							
							selectedNode.getInformationContext().setConfusionMatrixList(updateConfusionMatrixList(candidateHead, candidateBody));
							selectedNode.getInformationContext().setScore(heuristic.getHeuristicStrategy().score(selectedNode));
							selectedNode.getInformationContext().setGain(this.assess(selectedNode, previousNode.getInformationContext().getScore()));
							
							if (recursion) 
								this.checkNewOrdersInVars(selectedNode);
						}
						
						for (Iterator<Term> iterator = repeatedVars.keySet().iterator(); iterator.hasNext();) {
							Term term = iterator.next();
							newVarList.remove(term);	
						}
					}
					else {
						updateDeterminateLiteral(selectedNode, repeatedVars);
						if (recursion) 
							this.checkNewOrdersInVars(selectedNode);
					}
					previousNode = selectedNode;
				}
				
			}
			result = result_copy;
			selectedNode = result.get(result.size()-1);
			this.currentPath.addAll(result); 
		}
		
		// writting selected literals in the output file
		buffer.append("\n***************************************************************\n");
		buffer.append("\tBEST LITERAL:    ");
		line = "";
		for (Node<Literal, InformationContext> n : result)
			line += this.print(n) + ", ";
		line = line.substring(0, line.lastIndexOf(", ")) + "\n";
		buffer.append(line);
		buffer.append("***************************************************************\n");
		this.trace.writeToFile(buffer.toString());
		
		if (!this.orderedVars.isEmpty()) {
			StringBuffer orderInVars = new StringBuffer("Note ");
			for (VarPair vp: this.orderedVars) {
				orderInVars.append(vp.getVar1().getName());
				int sign = vp.getSign();
				if (sign == 0)
					orderInVars.append("=");
				else if (sign == 1)
					orderInVars.append("<");
				else if (sign == 2)
					orderInVars.append(">");
				
				orderInVars.append(vp.getVar2().getName() + "  ");
			}
			this.trace.writeToFile(orderInVars + "\n");
		}
		
		return result;
	}
	
	private List<VarPair> replaceVarInOrderedVars(List<VarPair> vpList, Term term, Variable variable) {
		List<VarPair> result = new ArrayList<VarPair>();
		Iterator<VarPair> it = vpList.iterator();
		VarPair newvp;
		while (it.hasNext()) {
			VarPair vp = it.next();
			if (vp.getVar1().equals(term)){
				newvp = new VarPair(variable,vp.getVar2(),vp.getSign());
			}
			else if (vp.getVar2().equals(term)){
				newvp = new VarPair(vp.getVar1(), variable,vp.getSign());
			}
			else
				newvp = new VarPair(vp.getVar1(),vp.getVar2(),vp.getSign());
			result.add(newvp);
		}
		return result;
	}

	// solo se llama para literales cuyo predicado es el mismo que el predicado objetivo 
	// para evitar problemas de recursion
	private boolean reverseConstantOrderIfNecessary(List<VarPair> varsPairs, Literal l) {
		boolean result = true;
		List<VarPair> newOrder = new ArrayList<VarPair>(varsPairs);
		boolean order = false;
		boolean reverse = false;
		VarPair vp;
		List<Term> arguments = l.getArgs();
		List<Term> rootArgs = this.targetNode.getDataNode().getArgs();
		if (this.constantOrderDirection == 0) {
			int i = 0;
			while (i < arguments.size() && !order) {
				Variable var1 = (Variable) rootArgs.get(i);
				Variable var2 = (Variable) arguments.get(i);
				vp = new VarPair(var2, var1, 1);
				order = vp.belongs(varsPairs);
				if (!order) {
					vp = new VarPair(var1, var2, 1);
					reverse =  vp.belongs(varsPairs);
				}
				i++;	
			}
		
			if (order) {
				this.constantOrderDirection = 1;
			}
			else if (i == arguments.size() && !order)
				if (reverse && this.constantOrderDirection == 0) {
					this.constantOrderDirection = 2;
					newOrder = new ArrayList<VarPair>();
					Iterator<VarPair> it = varsPairs.iterator();
					while (it.hasNext()) {
						VarPair varPair = it.next();
						VarPair revVarPair = new VarPair(varPair.getVar2(),varPair.getVar1(),varPair.getSign());
						newOrder.add(revVarPair);
					}
				}
		}
		else {
			int i = 0;
			while (i < arguments.size() && !order) {
				Variable var1 = (Variable) rootArgs.get(i);
				Variable var2 = (Variable) arguments.get(i);
				if (this.constantOrderDirection == 1)
					vp = new VarPair(var2, var1, 1);
				else 
					vp = new VarPair(var1, var2, 1);
				
				order = vp.belongs(varsPairs);
				i++;	
			}
			if (!order)
				result = false;
		}
		
		this.orderedVars = newOrder;
		return result;
	}
	
	// call before new variables have been bounded
	private void checkNewOrdersInVars(
			Node<Literal, InformationContext> selectedNode) {
		Literal l = selectedNode.getDataNode();
		Coverage cov = selectedNode.getInformationContext().getCoverage();
		List<Term> varsToCheck = new ArrayList<Term>(this.variableList);
		Iterator<Term> itVars;
		Iterator<Bindings> itBindings;
		Set<Bindings> bindings;
		boolean end = false;
		int index1, index2;
		Term termToCheck = null;
		List<Term> args = l.getArgs();
		int order = 0; // 1 means first argument is less than second argument and 2 means first arg is greater than the second one
		bindings = new HashSet<Bindings>(cov.getPositiveBindings());
		bindings.addAll(cov.getNegativeBindings());
		
		if (l.getPredicate().isPredefined()) {
			Term t1 = args.get(0);
			Term t2 = args.get(1);
			if (t1 instanceof Variable && t2 instanceof Constant) {
				//check order
				varsToCheck.remove(t1);
				List<Constant> constants = this.kbo.getConstantSet(t1.getType().getTypeName());
				List<String> values = new ArrayList<String>();
				for (Constant c : constants)
					values.add(c.getValue());
				
				String value = t2.getValue();
				if (values.contains(value)) {
					index1 = values.indexOf(value); 
					itVars = varsToCheck.iterator();
					while (itVars.hasNext()) {
						termToCheck = itVars.next();
						end = false;
						order = 0;
						itBindings = bindings.iterator();
						while (itBindings.hasNext() && !end) {
							Bindings b = itBindings.next();
							if (b.containsVariable((Variable) termToCheck)) {
								Constant c = b.getConstant((Variable) termToCheck);
								if (values.contains(c.getValue())) {
									index2 = values.indexOf(c.getValue());
									if (index1 < index2) {
										if (order != 2)
											order = 1;
										else end = true;
									}
									else {
										if (order != 1)
											order = 2;
										else end = true;
									}
								}
								else 
									end = true;
							}
						}
						if (!end) {
							//add a vair pair in orderedVars
							VarPair vp = null;
							if (order != -1) {
								if (order == 0)
									vp = new VarPair((Variable) t1, (Variable) termToCheck, 0);
								else if (order == 1)
									vp = new VarPair((Variable) t1, (Variable) termToCheck, 1);
								else if (order == 2)
									vp = new VarPair((Variable) t1, (Variable) termToCheck, 2);
								
								if (!vp.belongs(this.orderedVars))
									this.orderedVars.add(vp);
							}
						}
					}
				}
			}
			else {
				VarPair vp0, vp1, vp2;
				Set<Variable> terms = cov.getFirstPositiveBinding().getVariables();
				for (Term t : terms) {
					Type type = t.getType();
					for (Term v : terms) {
						if (type.equals(v.getType()) && !v.equals(t)) {
							vp0 = new VarPair((Variable) t,(Variable) v, 0);
								vp1 = new VarPair((Variable) t, (Variable) v, 1);
								vp2 = new VarPair((Variable) t,(Variable) v, 2);
								
								if (!vp0.belongs(this.orderedVars) && !vp1.belongs(this.orderedVars) && !vp2.belongs(this.orderedVars)) {
									order = checkOrder(this.kbo.getConstantSet(type.getTypeName()), vp1, bindings); 
									if (order == 0)
										this.orderedVars.add(vp0);
									else if (order == 1)
										this.orderedVars.add(vp1);
									else if (order == 2)
										this.orderedVars.add(vp2);
								}
						}
					}
				}
			}
		}
		else  {
			VarPair vp0, vp1, vp2;
			List<Node<Literal, InformationContext>> fullPath = selectedNode.getFullPath();
			Node<Literal,InformationContext> head = fullPath.remove(0);
			List<Term> headArgs = head.getDataNode().getArgs();
			for (Term t1 : headArgs) {
				Type type = t1.getType();
				for (Term t2 : cov.getFirstPositiveBinding().getVariables()) {
					if (type.equals(t2.getType()) && !t2.equals(t1)) {
						vp0 = new VarPair((Variable) t1, (Variable) t2, 0);
						vp1 = new VarPair((Variable) t1, (Variable) t2, 1);
						vp2 = new VarPair((Variable) t1, (Variable) t2, 2);
						if (!vp0.belongs(this.orderedVars) && !vp1.belongs(this.orderedVars) && !vp2.belongs(this.orderedVars)) {
							order = checkOrder(this.kbo.getConstantSet(type.getTypeName()), vp1, bindings); 
							if (order == 0)
								this.orderedVars.add(vp0);
							else if (order == 1)
								this.orderedVars.add(vp1);
							else if (order == 2)
								this.orderedVars.add(vp2);
						}
					}
				}
			}
		}
	}
	
	private int checkOrder(List<Constant> constantList, VarPair vp, Set<Bindings> bindings) {
		
		int result = 0; // 0 means they are equals, 1 means the firstarg is less than the second arg, 2 means the second arg is less than the first one
		boolean end = false;
		Iterator<Bindings> it;
		
		it = bindings.iterator();
		while (it.hasNext() && !end) {
			Bindings b = it.next();
			Term constant1 = b.getConstant(vp.getVar1());
			Term constant2 = b.getConstant(vp.getVar2());
						
			int index1 = constant1.belongs(constantList);
			int index2 = constant2.belongs(constantList);
			
			if (index1 < index2) {
				if (result != 2)
					result = 1;
				else end = true;
			}
			else if (index1 > index2) {
				if (result != 1)
					result = 2;
				else end = true;
			}	
			else 
				if (result != 0)
					end = true;
		}
		result = !end ? result : -1;
		return result;
	}

//	private List<Node<Literal,InformationContext>> checkRegrowRule(Node<Literal, InformationContext> selectedNode) {
//		List<Node<Literal, InformationContext>> result;
//		int headArity;
//		Literal l, head;
//		InformationContext ic;
//		boolean complete = false, firstTime = true;
//		Node<Literal, InformationContext> headNode, fatherNode, childNode, leaf = null;
//		List<Node<Literal, InformationContext>> currentPath, children;
//		Set<Term> varSet = null;
//		
//		l = selectedNode.getDataNode();
//		ic = selectedNode.getInformationContext();
////		complete = ic.getOrigCoverage().getNegativeBindings().size() == 0;
//		complete = ic.getConfusionMatrixList().getNewcm().getFalsePositives() == 0;
//		currentPath = selectedNode.getFullPath();
//		currentPath.remove(selectedNode);
//		headNode = currentPath.remove(0);
//		head = headNode.getDataNode();
//		headArity = head.getPredicate().getArity();
//		
//		// Checking if we can regrow the rule in production
//		if (!ic.isDeterminate()) {
//			if (!l.getPredicate().isTarget() && l.getMaxVarIndex() < headArity && 
//					BranchOperations.getNumberOfVars(currentPath) > headArity && 
//					BranchOperations.getDetLitNumber(currentPath) != currentPath.size()) {
//				result = new ArrayList<Node<Literal,InformationContext>>();
//				for (int i = currentPath.size() - 1; i >= 0; i--) {
//					Node<Literal, InformationContext> node = currentPath.get(i);
//					fatherNode = node.getParent();
//					l = node.getDataNode();
//					children = node.getChildren();
//					if (l.getMaxVarIndex() > head.getMaxVarIndex() ){ //&& !node.getInformationContext().isDeterminate()) { // this literal added a new variable
//						// removing all children of this node because it is going to be deleted too
//						for (Node<Literal, InformationContext> child : children) {
//							
//							// removing children if they were not saved as savedPoints
//							if (child.getDataNode().isPositive()) {
//								varSet = BranchOperations.getVarsInPath(fatherNode);
//								if (this.negatedNodes.containsKey(child.hashCode())){
//									Node<Literal, InformationContext> negatedChild = this.negatedNodes.get(child.hashCode());
//									if (!negatedChild.equals(selectedNode) && contains(result, negatedChild.getDataNode()) < 0) {
//										if (this.savePoints.contains(negatedChild)) {
//											if (varSet.containsAll(negatedChild.getDataNode().getVars())) {
//												// We do not need to remove it, just change the father
//												negatedChild.setParent(fatherNode);
//												fatherNode.addChild(negatedChild);
//											}
//											else {//deleting the child node from the savedPoints
//												this.savePoints.remove(negatedChild);
//												negatedChild.setParent(null);
//												this.negatedNodes.remove(negatedChild);
//											}
//										}
//									}
//								}
//								if (!child.equals(selectedNode)) {
//									if (this.savePoints.contains(child)) {//deleting the child node from the savedPoints
//										if (varSet.containsAll(child.getDataNode().getVars())) {
//											// We do not need to remove it, just change the father
//											child.setParent(fatherNode);
//											fatherNode.addChild(child);
//										}
//										else {
//											this.savePoints.remove(child);
//											child.setParent(null); // set the new parent because this node is going to be removed
//										}
//									}
//								}
//							}
//						}
//						childNode = result.size() > 0 ? result.get(0) : selectedNode;
////						node.getChildren().clear();
////						node.addChild(childNode);
//						
//						// if the node that is going to be removed is determinate, remove from the list of explored determinate literals too
////							if (node.getInformationContext().isDeterminate()) {
////								this.exploredDetLitList.remove(node);
//////								if (this.nDet > 0) this.nDet--;
////								this.exploredDetLit = this.exploredDetLitList.size() > 0;
////							}
//						
//						// removing node
//						int indexChild = -1;
//						if ((indexChild = contains(fatherNode.getChildren(), childNode.getDataNode())) >= 0) {
//							fatherNode.getChildren().get(indexChild).setParent(null);
//							ArrayList<Node<Literal, InformationContext>> childToRemove = new ArrayList<Node<Literal,InformationContext>>();
//							for (Node<Literal, InformationContext> n : childNode.getChildren()) {
//								if (n.getDataNode().getMaxVarIndex() > head.getMaxVarIndex())
//									childToRemove.add(n);
//							}
//							childNode.getChildren().removeAll(childToRemove);
//							childNode.setParent(fatherNode);
//							fatherNode.getChildren().set(indexChild, childNode);
//							
//						}
//						else {
//							childNode.setParent(fatherNode);
//							fatherNode.addChild(childNode);
//						}
//						
//						node.getChildren().clear();
//						if (!complete) {
//							fatherNode.getChildren().remove(node);
//							node.setParent(null);
//						}
//						else {
//							if (firstTime) {
//								leaf = new Node<Literal,InformationContext>();
//								leaf.setParent(node);
//								leaf.setInformationContext(selectedNode.getInformationContext().clone());
//								leaf.setDataNode(selectedNode.getDataNode().clone());
//								node.addChild(leaf);
//								firstTime = false;
//								this.candidateNodes.add(leaf);
//							}
//							else
//								node.addChild(leaf);
//							leaf = node;
//						}
//					}
//					else {
//						result.add(0, node);
//					}
//				}
//			}
//			else 
//				result = currentPath;
//		}
//		else
//			result = currentPath;
//		return result;
//	}

	private List<Node<Literal, InformationContext>> selectBestLiterals() {
		List<Node<Literal,InformationContext>> result = new ArrayList<Node<Literal,InformationContext>>();
		List<Node<Literal,InformationContext>> children;
		int firstNodeIndex = 0;
		
		Node<Literal, InformationContext> bestNode = this.candidateNodes.remove(firstNodeIndex);
		
		while (bestNode.getInformationContext().isDeterminate() && itsVarsBoundedByPreviousDet(bestNode)) // it deletes all determinate literals if all of its variables are bounded
				bestNode = this.candidateNodes.remove(firstNodeIndex);
		
		if (bestNode.getInformationContext().isDeterminate()) { //if the best node is determinate, we will select all of the evaluated determinate nodes
			
			if (!this.continueSearching(bestNode)) { //unless we cannot add more determinate literals (depth)
				while (bestNode.getInformationContext().isDeterminate()) { //we are looking for a non-determinate literal.
					bestNode = this.candidateNodes.remove(firstNodeIndex);
				}
				result.add(bestNode); // it adds the best literal after removing every determinate literal
			}
			else { // it adds every determinate literal but it restructures the exploration tree to link them to each other.
				Node<Literal, InformationContext> previousNode = bestNode.getParent();
				children = previousNode.getChildren(); // watch out! children and this.currentGeneration point the same list of nodes
				while (bestNode.getInformationContext().isDeterminate() && this.continueSearching(bestNode) && !this.candidateNodes.isEmpty()) { //we include a list of determinate literal
					if (!itsVarsBoundedByPreviousDet(bestNode)) {
						result.add(bestNode);
						children.remove(bestNode);
						bestNode.setParent(previousNode);
						previousNode.addChild(bestNode);
						previousNode = bestNode;
						bestNode = this.candidateNodes.remove(firstNodeIndex);
					}
					else {
						if (!this.candidateNodes.isEmpty())
							bestNode = this.candidateNodes.remove(firstNodeIndex);
					}
				}
				if (!bestNode.getInformationContext().isDeterminate())
					this.candidateNodes.add(0, bestNode);
			}
		}
		else
			result.add(bestNode);
		
		return result;
	}

	private boolean itsVarsBoundedByPreviousDet(Node<Literal, InformationContext> node) {
		boolean result = false, end = false;
		Literal literal = node.getDataNode(), head;
		String predicateName = literal.getPredicate().getName();
		List<Term> existingVars;
		
		List<Node<Literal,InformationContext>> nodes = node.getFullPath();
		nodes.remove(node);
		head = nodes.remove(0).getDataNode();
		
		existingVars = new ArrayList<Term>(literal.getArgs());
		existingVars.removeAll(literal.getUnboundedVars());
		
		for (Iterator<Term> iterator = head.getArgs().iterator(); iterator.hasNext() && !end;) {
			Term t = iterator.next();
			if (existingVars.contains(t)) 
				end = true;
		}
		
		if (!end && existingVars.size() > 0) {
			Iterator<Node<Literal,InformationContext>> it = nodes.iterator();
			while (it.hasNext() && !existingVars.isEmpty() && !end) {
				Node<Literal,InformationContext> n = it.next();
				if (n.getInformationContext().isDeterminate()) {
					List<Term> previousNewVars = getNodeNewVars(n);
					for (int i = 0; i < previousNewVars.size() && !end; i++) {
						Term term = previousNewVars.get(i);
						if (existingVars.contains(term)) 
							if (!n.getDataNode().getPredicate().getName().equals(predicateName))
								end = true;
							else
								existingVars.remove(term);
					}
				}
			}
			if (!end)
				result |= (existingVars.size() == 0);
		}
		
		return result;
	}
	
	private List<Term> getNodeNewVars(Node<Literal, InformationContext> node) {
		List<Term> result = new ArrayList<Term>(node.getDataNode().getArgs());
		Set<Term> existingVars = new HashSet<Term>();
		
		List<Node<Literal,InformationContext>> nodes = node.getFullPath();
		nodes.remove(node);
		Iterator<Node<Literal,InformationContext>> it = nodes.iterator();
		while (it.hasNext() && !result.isEmpty()) {
			Node<Literal,InformationContext> n = it.next();
			
			for (Term term : result) {
				if (n.getDataNode().getArgs().contains(term))
					existingVars.add(term);
			}
			result.removeAll(existingVars);
		}
		return result;
	}


	// deber�a de retornar las matrices de confusi�n pero no modificarlas??
	private ConfusionMatrixList updateConfusionMatrixList(Node<Literal,InformationContext> head, List<Node<Literal,InformationContext>> body) {
		ConfusionMatrixList result = new ConfusionMatrixList();
		Node<Literal, InformationContext> node = body.get(body.size()-1);
		List<Literal> literals = new ArrayList<Literal>();
		InformationContext ic = null, icp = null;
		Coverage origcr = null, nowcr = null, newcr = null;
		
		for (Node<Literal, InformationContext> n : body)
			literals.add(n.getDataNode());
		
		ConfusionMatrix origcm = null;
		ConfusionMatrix nowcm = null;
		ConfusionMatrix newcm = null;
		
		// confusion matrix of the parent node
		icp = node.getParent().getInformationContext();
		ConfusionMatrixList cmlp = icp.getConfusionMatrixList();
		ConfusionMatrix origcmp = cmlp.getOrigcm();
		ConfusionMatrix newcmp = cmlp.getNewcm();
		
		// coverage of the current node
		ic = node.getInformationContext();
		origcr = ic.getOrigCoverage();
		nowcr = bop.origmatch(icp.getCoverage(), head,literals); 
		newcr = ic.getCoverage();
		
		// updating confusion matrix list for not negated node
		
		origcm = new ConfusionMatrix();
		nowcm = new ConfusionMatrix();
		newcm = new ConfusionMatrix();
		
		origcm.setTruePositives(origcr.getPositiveBindings().size());
		origcm.setFalsePositives(origcr.getNegativeBindings().size());
		origcm.setFalseNegatives(origcmp.getTruePositives() - origcm.getTruePositives());
		origcm.setTrueNegatives(origcmp.getFalsePositives() - origcm.getFalsePositives());
		
		nowcm.setTruePositives(nowcr.getPositiveBindings().size());
		nowcm.setFalsePositives(nowcr.getNegativeBindings().size());
		nowcm.setFalseNegatives(newcmp.getTruePositives() - nowcm.getTruePositives());
		nowcm.setTrueNegatives(newcmp.getFalsePositives() - nowcm.getFalsePositives());
			
		newcm.setTruePositives(newcr.getPositiveBindings().size());
		newcm.setFalsePositives(newcr.getNegativeBindings().size());
		newcm.setFalseNegatives(nowcm.getFalseNegatives());
		newcm.setTrueNegatives(nowcm.getTrueNegatives());
		
		result.setOrigcm(origcm);
		result.setNowcm(nowcm);
		result.setNewcm(newcm);
		
		return result;
	}
	
	/**
	 * 
	 * @param selectNode
	 * @param repeatedVars
	 */
	private void updateDeterminateLiteral(Node<Literal, InformationContext> selectNode, Map<Term, Variable> repeatedVars) {
		
		InformationContext icSelectedNode = selectNode.getInformationContext();
		Set<Bindings> newNegBindings = icSelectedNode.getCoverage().getNegativeBindings();
		Set<Bindings> origNegBindings = icSelectedNode.getOrigCoverage().getNegativeBindings();
		
		// updating confusion matrix lists
		ConfusionMatrix newCM = icSelectedNode.getConfusionMatrixList().getNewcm();
		newCM.setTrueNegatives(newCM.getTrueNegatives() + (newCM.getFalsePositives() - newNegBindings.size()));
		newCM.setFalsePositives(newNegBindings.size());
		ConfusionMatrix nowCM = icSelectedNode.getConfusionMatrixList().getNowcm();
		nowCM.setTrueNegatives(newCM.getTrueNegatives());
		nowCM.setFalsePositives(newCM.getFalsePositives());
		ConfusionMatrix origCM = icSelectedNode.getConfusionMatrixList().getOrigcm();
		origCM.setTrueNegatives(origCM.getTrueNegatives() + (origCM.getFalsePositives() - origNegBindings.size()));
		origCM.setFalsePositives(origNegBindings.size());
		
		if (!repeatedVars.isEmpty()) {
			List<Term> currentArgs = selectNode.getDataNode().getArgs();
			for (int j = 0; j < currentArgs.size(); j++) {
				Term term = currentArgs.get(j);
				if (repeatedVars.containsKey(term)) {
					currentArgs.set(j, repeatedVars.get(term)); //check the coverage
					icSelectedNode.getCoverage().setPositiveBindings(this.updateBindings(repeatedVars, icSelectedNode.getCoverage().getPositiveBindings()));
					icSelectedNode.getCoverage().setNegativeBindings(this.updateBindings(repeatedVars, icSelectedNode.getCoverage().getNegativeBindings()));
				}
			}
		}
		icSelectedNode.setGain(assess(selectNode, selectNode.getParent().getInformationContext().getScore()));
		
	}

	private Map<Term, Variable> removeRepetitiousVars(List<Term> newVarList, Node<Literal, InformationContext> current) {
		Map<Term, Variable> result = new HashMap<Term, Variable>();
		boolean end = false;
		
		Literal lit = current.getDataNode();
		Coverage currentCoverage = current.getInformationContext().getCoverage();
		
		Set<Bindings> pos = null;
//		Set<Bindings> neg = null;
		Bindings b = null;
		
		ICombinatorial<Integer> combinations = new CombinationsWithoutRepetition<Integer>(2, newVarList.size());
		while (combinations.hasNext()) {
			
			int[] index = combinations.next();
			Term v1 = newVarList.get(index[0]);
			Term v2 = newVarList.get(index[1]);
			
			pos = currentCoverage.getPositiveBindings();
//			neg = currentCoverage.getNegativeBindings();
			
			//check if v1 and v2 contains the same values in positive bindings
			end = false;
			
			Iterator<Bindings> it = pos.iterator();
			while (!end && it.hasNext()) {
				b = it.next();
				if (!b.getConstant(v1).equals(b.getConstant(v2)))
					end = true;
			}
			
			//check if v1 and v2 contains the same values in negative bindings
//			it = neg.iterator();
//			while (!end && it.hasNext()) {
//				b = it.next();
//				if (!b.getConstant(v1).equals(b.getConstant(v2)))
//					end = true;
//			}
			
			// if v1 and v2 contains the same values for each bindings in the current node coverage means
			// they are repeated. Then we have to remove v2 or v1 of the set of bindings and update  
			// the list of arguments of the current node
			if (!end) { // ojo repasar esto
				lit.printScreen(); System.out.print("-->");
				int indexToReplace = lit.getArgs().indexOf(v2);
				if (indexToReplace != -1) {
					System.out.println(v2.getName() + " == " + v1.getName());
					result.put(v2, (Variable) v1);
				}
				else {
					indexToReplace = lit.getArgs().indexOf(v1);
					if (indexToReplace != -1) {
						System.out.println(v1.getName() + " == " + v2.getName());
						result.put(v1, (Variable) v2);
					}
				}
			}
//			else { 
//				result.put(v1, v1);
//				result.put(v2, v2);
//			}
		}
		return result;
	}
	

	/**
	 * 
	 * @param nodes
	 */
	private void rewriteNodes(List<Node<Literal, InformationContext>> nodes) {
		// add all new variables in the resulting node list
		List<Term> variablesToModified = new ArrayList<Term>();
		int numberOfNewVars = 0;
		for (Node<Literal, InformationContext> n : nodes) { //get the number of new variables
			List<Term> nodeArgs = n.getDataNode().getArgs();
			for (Term term : nodeArgs) {
				if (!term.isBound())
					numberOfNewVars++;
			}
		}
		
		int i = 0;
		for (; i < numberOfNewVars && i < this.newVars.size(); i++) 
			variablesToModified.add(this.newVars.get(i));
		
		for (; i < numberOfNewVars; i++)
			variablesToModified.add(new Variable());
		 
		Coverage icNode = null;
		Set<Bindings> posBindings = null;
		Set<Bindings> negBindings = null;
		Map<Term, Variable> varsModified = null;
		
		for (Node<Literal, InformationContext> n : nodes) {
			icNode = n.getInformationContext().getCoverage();
			varsModified = new HashMap<Term, Variable>();
			List<Term> args = n.getDataNode().getArgs();
			for (i = 0; i < args.size(); i++) {
				Term term = args.get(i);
				if (!term.isBound()) {
					Term t = variablesToModified.remove(0);
					args.set(i, t);
					varsModified.put(term, (Variable) t);
				}
			}
			
			posBindings = updateBindings(varsModified, icNode.getPositiveBindings());
			negBindings = updateBindings(varsModified, icNode.getNegativeBindings());
			icNode.setPositiveBindings(posBindings);
			icNode.setNegativeBindings(negBindings);
		}
	}

	private Set<Bindings> updateBindings(Map<Term, Variable> varsModified, Set<Bindings> nodeBindings) {
		Set<Bindings> result = new HashSet<Bindings>();
		Set<Variable> existingVars = null;
		for (Bindings b : nodeBindings) {
			existingVars = b.getBindings().keySet();
			Bindings bindings = new Bindings();
			for (Variable v : existingVars) {
				if (varsModified.containsKey(v))
					bindings.addBinding(varsModified.get(v), b.getBindings().get(v));
				else
					bindings.addBinding(v, b.getBindings().get(v));
			}
			result.add(bindings);
		}
		return result;
	}

//	@Override
//	protected Node<Literal, InformationContext> selectNode() {
//		Node<Literal, InformationContext> result = null;
//		String line = "";
//		int firstNodeIndex = 0;
//		fc.setMaxGain(thresholds.getMaxPossibleGain());
//		Collections.sort(this.currentGeneration, fc);
//		
//		/**
//		 * PARA CORTAR EL A�ADIR MUCHOS LITERALES DETERMINADOS QUE PUEDE QUE NO NOS LLEVEN
//		 * A NINGUNA SOLUCI�N LO QUE HACEMOS ES QUE CUANDO TENEMOS LITERALES DETERMINADOS
//		 * PERO TENEMOS ALG�N LITERAL GAINFUL CON GANANCIA BUENA, ESCOGEMOS A �STE
//		 */
//		result = this.currentGeneration.get(firstNodeIndex);
//		this.currentGeneration.remove(firstNodeIndex);
//		while (result.getInformationContext().isDeterminate() && !this.continueSearching(result)) {
//			Collections.sort(this.currentGeneration, fcg);
//			result = this.currentGeneration.get(firstNodeIndex);
//			this.currentGeneration.remove(firstNodeIndex); //ojito que esto lo acabo de modificar
//		}
//		
////		else if (result.getInformationContext().getInformationGain() <= 0 && !result.getDataNode().hasNewVars())
////			//no devolver ning�n nodo, borrar la lista
//		
//		rule.addNode(result);
//		
//		int p = result.getInformationContext().getCoverage().getPositiveBindings().size();
//		this.thresholds.updateMaxPossibleGain(p * result.getInformationContext().getScore());
//		
//		//bounding free vars and adding to variableSet
//		this.boundNewVars(result.getDataNode());
//		
//		line = "\n***************************************************************\n";
//		System.out.print(line);
//		FileUtilities.writeToFile(f.getAbsolutePath(), line);
//		line = "\tBEST LITERAL:    ";
//		System.out.print(line);
//		FileUtilities.writeToFile(f.getAbsolutePath(), line);
//		line = this.print(result);
//		FileUtilities.writeToFile(f.getAbsolutePath(), line);
//		System.out.println(line);
//		line = "***************************************************************\n";
//		FileUtilities.writeToFile(f.getAbsolutePath(), line);
//		System.out.print(line);
//	
//		return result;
//	}

	@Override
	protected boolean hasNext(Node<Literal, InformationContext> node) {
		boolean result = true;
		if (this.nodeFactories.containsKey(node.hashCode()))
			result = this.nodeFactories.get(node.hashCode()).hasNext();
		return result;
	}

	@Override
	protected boolean isSolution() {
		boolean result = false;
		
		int neg = currentNode.getInformationContext().getOrigCoverage().getNegativeBindings().size();
		int pos = currentNode.getInformationContext().getOrigCoverage().getPositiveBindings().size();
		
		result = (neg == 0) && (pos > 0);
		
//		result = (((neg == 0) && (pos > 0) && (!this.candidateNodes.isEmpty()))
//				|| (!this.hasNext(currentNode)));
		
//		if (result)
//			this.printTree(0, this.targetNode);
		return result;
	}

	
//	private void updateFullRule(List<Node<Literal, InformationContext>> body, 
//			Node<Literal, InformationContext> result) {
//	
//		Node<Literal, InformationContext> head = this.cloneNode(this.targetNode);
//		List<Literal> literals = new ArrayList<Literal>();
//		
//		for (Node<Literal, InformationContext> node : body) {
//			literals.add(node.getDataNode());
//			node.getInformationContext().setCoverage(
//					bop.match(head.getInformationContext().getOrigCoverage(), head.getDataNode(), literals));
//			
//			node.getInformationContext().setOrigCoverage(
//					bop.origmatch(head.getInformationContext().getOrigCoverage(), head, literals));
//		}
//	
//		literals.add(result.getDataNode());
//		
//		result.getInformationContext().setCoverage(
//				bop.match(head.getInformationContext().getOrigCoverage(), head.getDataNode(), literals));
//		
//		result.getInformationContext().setOrigCoverage(
//				bop.origmatch(head.getInformationContext().getOrigCoverage(), head, literals));
//	}

	@Override
	protected Node<Literal, InformationContext> restoreBestSavedPoint() throws IOException { //ojo repasar sobre todo si se han introducido varios literales det en una regla. Cuidado con las variables existentes y las nuevas
		Node<Literal, InformationContext> result = null, lastNode;
		int pathSize = 0;
		StringBuffer buffer, orderInVars = new StringBuffer();
		
		if (!this.savePoints.isEmpty()) {
			Collections.sort(this.savePoints,fc); // esta cada vez que se guarda un save point
			result = this.savePoints.get(0);
			this.savePointsVisited.add(result);
			
			//reseting parameters
			update();
			this.savePoints.remove(0);
			this.currentPath = result.getPath();
			pathSize = this.currentPath.size();
			lastNode = this.currentPath.get(pathSize-1);
			
			//reset some values (existing variables, new variables and the order among the variables)
			this.variableList = this.getUnboundedVars(this.targetNode, this.currentPath);
			Variable.resetExistingVariableCounter(BranchOperations.getMaxVarIndex(this.currentPath)+1);
			this.newVars.clear();
			this.orderedVars.clear();
			this.updateOrderedVars(this.targetNode.getDataNode());
			this.maxDepth = 0;
			this.constantOrderDirection = 0;
			boolean previousDet = false;
			Type unboundType = new Type();
			for(Node<Literal, InformationContext> n : this.currentPath) {
				Literal lit = n.getDataNode();
				if (lit.getPredicate().isTarget()) {
					reverseConstantOrderIfNecessary(this.orderedVars,lit);
					this.lf.setConstantOrderDirection(this.constantOrderDirection);
				}
				this.updateOrderedVars(n.getDataNode());
				if (recursion) 
					this.checkNewOrdersInVars(n);
				
				for (Term t : lit.getArgs())
					if (t.belongs(this.variableList) == -1 && !(t instanceof Constant))
						t.setType(unboundType);
				
				//computing the depth of determinate literals
				if (n.getDataNode().hasUnboundedVars() && !previousDet) this.maxDepth++;
				if(n.getInformationContext().isDeterminate())
					previousDet = true;
				else 
					previousDet = false;
			
				this.boundNewVars(lit);
				updateOrderedVars(lit);
				if (recursion) 
					this.checkNewOrdersInVars(n);
			}
			
			buffer = new StringBuffer("\nThere is no solution... \nRestoring Best Saved Point ");
			buffer.append(print(result));
			this.trace.writeToFile(buffer.toString());
			this.trace.writeToFile(orderInVars.toString());
			
			this.backtrackingNumber++;
			this.outputData.incBackTrackingNumber();
			
			this.updateCoverage(lastNode, true);
			this.thresholds.resetThresholds();
			thresholds.updateMaxPossibleGain(this.heuristic.getHeuristicStrategy().getMaxPossibleGain(lastNode));
			printState();
		}
	
//		buffer = new StringBuffer("\nBranching node: ");
//		buffer.append(this.print(currentNode) + "\n");
//		this.trace.writeToFile(buffer.toString());
		
		return result;
	}
	
	/**
	 * Bounds new vars belonging to a specific literal that has been added to the rule in production
	 * @param bestLit candidate literal to include in the production rule
	 * @return returns a list of new variables that have changed to existing variables
	 */
	private List<Term> boundNewVars(Literal bestLit) {
		//bounding free vars and adding to variableSet
		List<Term> result = new ArrayList<Term>();
		List<Term> literalArgs = bestLit.getArgs();
		if (!bestLit.getPredicate().isPredefined()){// && bestLit.isPositive()) {
			for(int i = 0; i< literalArgs.size();i++) {
				Term t = literalArgs.get(i);
				t.setType(bestLit.getPredicate().getArgsType().get(i));
				if (!variableList.contains(t)) {
					variableList.add(t);
					result.add(t);
				}
			}
		}
//		else if (bestLit.isNegated() && bestLit.hasUnboundedVars()){
//			List<Term> args = new ArrayList<Term>();
//			for (Term term : bestLit.getArgs()) {
//				if (!term.isBound())
//					term = new AnonymousVariable();
//				args.add(term);
//			}
//			bestLit.setArgs(args);
//		}
		
		return result;
	}
	
	/**
	 * Unbounds new vars because a backtracking was performed and some literals with new vars can dissapear from the rule in production.
	 * @param bestLit last literal included in the rule
	 * @return returns a list of existing variables
	 */
	private List<Term> getUnboundedVars(Node<Literal,InformationContext> head, List<Node<Literal,InformationContext>> body) {
		List<Term> result = new ArrayList<Term>();
		List<Term> literalArgs = null;
		
		result.addAll(head.getDataNode().getArgs());
		
		for (Node<Literal,InformationContext> n : body) {
			Literal l = n.getDataNode();
			if (!l.getPredicate().isPredefined()) { //&& l.isPositive()
				literalArgs = l.getArgs();
				for(int i = 0; i< literalArgs.size();i++) {
					Term t = literalArgs.get(i);
					if (!result.contains(t))
						result.add(t);
				}
			}
		}
		return result;
	}

	@Override
	protected void savePoints() throws IOException {
		float threshold = 0;
		double minSaveableGain = 0;
		double informationGain = 0;
		InformationContext icNode = null;
		boolean isCurrentNodeDet = false;
		Node<Literal, InformationContext> bestCompleteNode = null;
		int bestPosCover = 0, posSize = 0, negSize = 0;
		StringBuffer buffer;
		
		try {
			threshold = Float.parseFloat(PropertiesStore.getProperty("threshold"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FoilException e) {
			e.printStackTrace();
		}
		
		isCurrentNodeDet = currentNode.getInformationContext().isDeterminate();
		
		/**
		 * if the selected node is determinate the minimum useful gain will reach
		 * a higher threshold.
		 */
		if (isCurrentNodeDet) //only determinate nodes and those with gain >= 0.8 * maxPossGain will be saved 
			informationGain = thresholds.getMaxPossibleGain();
		else
			informationGain = currentNode.getInformationContext().getGain();
		
		minSaveableGain = threshold * informationGain;
		
		for (Node<Literal, InformationContext> node : this.savePoints) { // it keeps the best cover of a consistent save point
			icNode = node.getInformationContext();
			posSize = icNode.getConfusionMatrixList().getOrigcm().getTruePositives();
			negSize = icNode.getConfusionMatrixList().getOrigcm().getFalsePositives();
			
			if ((negSize == 0) && (posSize > 0) ) {
				if (bestPosCover < posSize) {
					bestCompleteNode = node;
					bestPosCover = posSize;
				}
			}
		}
		
		for (Node<Literal, InformationContext> node : this.candidateNodes) {
			icNode = node.getInformationContext();
			boolean isNodeDet = icNode.isDeterminate();
			posSize = icNode.getConfusionMatrixList().getOrigcm().getTruePositives();
			negSize = icNode.getConfusionMatrixList().getOrigcm().getFalsePositives();
		
			if ((negSize == 0) && (posSize > 0) ) {
				if (bestPosCover < posSize) {
					bestCompleteNode = node;
					bestPosCover = posSize;
				}
			}
			else if ((icNode.getGain() >= minSaveableGain) 
					|| (isCurrentNodeDet && isNodeDet && this.continueSearching(currentNode))) // if a determinate node is selected as the candidate literal, all determinate nodes will be saved 
					{ //if the literal does not cover any negative bindings, it will be saved too 
				
				if (containsEquivalentNode(this.savePoints, node) < 0 && containsEquivalentNode(this.savePointsVisited,node) < 0) {
					
//					this.savePoints.add(this.cloneNode(node));
					this.savePoints.add(node); //there is no copy of the nodes, save points always point to the nodes in the tree being built
					
					if (this.maxCheckPoints > 0 && (maxCheckPoints - this.backtrackingNumber - 1) < this.savePoints.size()) {
						Collections.sort(this.savePoints,fc);
						int size = this.savePoints.size();
						for (int i = size-1; i >= (maxCheckPoints - this.backtrackingNumber - 1); i--)
							this.savePointsVisited.add(cloneNode(this.savePoints.remove(i)));
					}
				}
			}
		}
		
		if (bestCompleteNode != null) { //if some of the evaluated node does not cover any negative instance (it is then a complete node) it will be saved in the save points list
			if (containsEquivalentNode(this.savePoints, bestCompleteNode) < 0 && containsEquivalentNode(this.savePointsVisited,bestCompleteNode) < 0) {
//				this.savePoints.add(this.cloneNode(bestCompleteNode));
				this.savePoints.add(bestCompleteNode);
				
				if (this.maxCheckPoints > 0 && (maxCheckPoints - this.backtrackingNumber) < this.savePoints.size()) {
					Collections.sort(this.savePoints,fc);
					int size = this.savePoints.size();
					for (int i = size-1; i >= (maxCheckPoints - this.backtrackingNumber); i--)
						this.savePointsVisited.add(cloneNode(this.savePoints.remove(i)));
				}
			}
		}
		if (this.savePoints.size() > 0) {
			buffer = new StringBuffer("Current Saved Literals \n");
			for (Node<Literal, InformationContext> node : this.savePoints)
				buffer.append(print(node) + "\n");
			this.trace.writeToFile(buffer.toString());
		}
		
	}
	
	/**
	 * Checks if a specific node is equivalent to a node saved in a node list. 
	 * Two nodes will be equivalent if its literals are equals 
	 * and the informationContext (only the original coverage is checked) of the node is equal 
	 * or worse than those of the saved node in the nodeList. 
	 * @param nodeList list of nodes where search for the node
	 * @param node node to search for
	 * @return -1 if the node does not belong to the node list, the index of the node list where node is otherwise
	 */
	private int containsEquivalentNode(List<Node<Literal, InformationContext>> nodeList,
			Node<Literal, InformationContext> node) {
		int result = 0;
		boolean end = false;
		Literal l = node.getDataNode();
		InformationContext ic = node.getInformationContext();
		ConfusionMatrix origcm = ic.getConfusionMatrixList().getOrigcm();
		
		while (result < nodeList.size() && !end) {
			Node<Literal, InformationContext> nodeAux = nodeList.get(result);
			Literal lAux = nodeAux.getDataNode();
			InformationContext icAux = nodeAux.getInformationContext();
			ConfusionMatrix origcmAux = icAux.getConfusionMatrixList().getOrigcm();
			
			if (l.equals(lAux) && origcm.equals(origcmAux))
				end = true;
			else 
				result++;
		}
		if (!end)
			result = -1;
		
		return result;
	}
	
		
	@Override
	protected Node<Literal, InformationContext> getParent(Node<Literal, InformationContext> node) {
		return node.getParent();
	}

	@Override
	protected boolean checkState() {
		boolean result = false;
		Set<Bindings> positiveBindingsToCover,positiveBindingsCovered, remainingPosBindings;
		Node<Literal, InformationContext> targetNodeCopy;
		InformationContext icTargetNode;
		Coverage cr;
		
		//Iterating over the rules and checking the originals positives bindings that are covered
		
		targetNodeCopy = this.targetNode;
		icTargetNode = this.targetNode.getInformationContext();
		
		positiveBindingsCovered = new HashSet<Bindings>();
		for (Node<Literal,InformationContext> n : finalNodes)
			positiveBindingsCovered.addAll(n.getInformationContext().getOrigCoverage().getPositiveBindings());
		
		positiveBindingsToCover = icTargetNode.getOrigCoverage().getPositiveBindings();
		remainingPosBindings = new HashSet<Bindings>(positiveBindingsToCover);
		remainingPosBindings.removeAll(positiveBindingsCovered);
			
		if (remainingPosBindings.size() == 0)
			result = true;
		else {
			cr = new Coverage();
			cr.setNegativeBindings(icTargetNode.getOrigCoverage().getNegativeBindings());
			cr.setPositiveBindings(remainingPosBindings);
				
			icTargetNode = new InformationContext();
			icTargetNode.setCoverage(cr);
			icTargetNode.setOrigCoverage(cr);
			
			this.targetNode = new Node<Literal, InformationContext>();
			this.targetNode.setDataNode(targetNodeCopy.getDataNode());
			this.targetNode.setInformationContext(icTargetNode);
		}
		
		return result;
	}
	
	@Override
	protected void printState () throws IOException {
		int detLitNumber = 0, coveredPosBindings, coveredTotBindings;
		StringBuffer buffer;
		Node<Literal, InformationContext> lastNode;
		InformationContext ic;
		
		int size = this.currentPath.size();
		if (size > 0) {
			lastNode = this.currentPath.get(this.currentPath.size()-1);
			detLitNumber = BranchOperations.getDetLitNumber(this.currentPath);
		}
		else 
			lastNode = this.targetNode;
		
		ic = lastNode.getInformationContext();
		
		coveredPosBindings = ic.getOrigCoverage().getPositiveBindings().size();
		coveredTotBindings = ic.getOrigCoverage().getNegativeBindings().size() + coveredPosBindings;
		
		buffer = new StringBuffer("\nState " + coveredPosBindings + "/" + 
				coveredTotBindings + "\t" + "availableBits " + (Utilities.encode(coveredPosBindings, allTuples) - ic.getNewRuleBits()) + " ");
		buffer.append(detLitNumber > 0 ? detLitNumber + " weak literals" : "");
		buffer.append("\nCurrent Rule: ");
		buffer.append(BranchOperations.pathToString(this.targetNode,this.currentPath) + "\n");
		this.trace.writeToFile(buffer.toString());
		ic.getCoverage().writeToFile(trace);
	}

	@Override
	protected String print(Node<Literal, InformationContext> node) {
		String result = node.getDataNode().toString();
		return result;
	}

	@Override
	protected List<Node<Literal, InformationContext>> getChildren(Node<Literal, InformationContext> node) {
		return node.getChildren();
	}
	
//	private Map<Term, Term> getDuplicateVars(Node<Literal, InformationContext> detNode) {
//		Map<Term, Term> result = new HashMap<Term, Term>();
//		Map<Term, Term> possibleDuplicateVars = null;
//		boolean end = false;
//		int i = 0;
//		Literal detLit = detNode.getDataNode();
//		List<Term> detArgs = detLit.getArgs();
//		
//		while (i < exploredDetLitList.size() && !end ) {
//			Node<Literal, InformationContext> node = exploredDetLitList.get(i);
//			Literal lit = node.getDataNode();
//			
//			if (lit.getPredicate().equals(detLit.getPredicate())) {
//				possibleDuplicateVars = new HashMap<Term, Term>();
//				for (int j = 0; j < detArgs.size() && !end; j++) {
//					if (detArgs.get(j).isBound()) {
//						if (!lit.getArgs().get(j).equals(detArgs.get(j))) {
//						//si tiene tipo y los dos tipos son iguales, continuamos
//							end = true;
//						}
//						else
//							possibleDuplicateVars.put(detArgs.get(j), lit.getArgs().get(j));
//					}
//				}
//				if (!end) {
//					for (Term v: possibleDuplicateVars.keySet())
//						result.put(v, possibleDuplicateVars.get(v));
//					end = true;
//				}
//				else 
//					end = false;
//			}				
//			i++;
//		}
//		return result;
//	}
	
//	private Map<Term, Term> getDuplicateVars(Node<Literal, InformationContext> detNode) {
//		Map<Term, Term> result = new HashMap<Term, Term>();
//		boolean duplicateVars = true;
//		boolean end = false;
//		int i = 0;
//		Literal detLit = detNode.getDataNode();
//		InformationContext icDetNode = detNode.getInformationContext();
//		
//		while (i < exploredDetLitList.size() && !end ) {
//			Node<Literal, InformationContext> node = exploredDetLitList.get(i);
//			Literal lit = node.getDataNode();
//			InformationContext icNode = node.getInformationContext();
//			
//			if (lit.getPredicate().equals(detLit.getPredicate())) {
//				if (this.checkCoverage(icDetNode, icNode)) {
//					List<Term> argsExploredLit = detLit.getArgs();
//					List<Term> argsLit = lit.getArgs();
//					int j = 0;
//					duplicateVars = true;
//					while (j < argsExploredLit.size() && duplicateVars) {
//						Term t1 = argsExploredLit.get(j);
//						Term t2 = argsLit.get(j);
//						if (!t1.hasType()) {
//							result.put(t1, t2);
//							end = true;
//						}
//						else if (!t1.equals(t2)) {
//							duplicateVars = false;
//							end = false;
//							result.clear();
//						}
//						j++;
//					}
//				}
//			}				
//			i++;
//		}
//		return result;
//	}

	@Override
	protected boolean stop() {
		boolean result = false;
		Node<Literal, InformationContext> currentNegatedNode = this.negatedNodes.get(currentNode.hashCode());
		result = this.stopStrategy.getStopStrategy().stop(currentNode.getInformationContext(), thresholds.getMaxPossibleGain());
		result |= this.stopStrategy.getStopStrategy().stop(currentNegatedNode.getInformationContext(), thresholds.getMaxPossibleGain());
		
		result |= this.thresholds.getBestLitGain() >= this.thresholds.getMaxPossibleGain();
		
		return result;
	}

	@Override
	protected String getGoalPredicate() {
		return this.goalPredicate;
	}

	@Override
	protected void removeUselessBranchesNodeLevel() {
		Node<Literal, InformationContext> parentNode;
		List<Node<Literal, InformationContext>> children, newChildren;
		
		parentNode = this.currentNode.getParent();
		children = parentNode.getChildren();
		children.remove(this.currentNode);
		newChildren = new ArrayList<Node<Literal,InformationContext>>();
		newChildren.add(this.currentNode);
		
		for (Node<Literal, InformationContext> child : children) {
			if (this.savePoints.contains(child) || !child.getChildren().isEmpty())
				newChildren.add(child);
			else {
				child.setParent(null);
				child.getChildren().clear();
				child = null;
			}
		}
		parentNode.setChildren(newChildren);
	}
	
//	@Override
//	protected void removeUselessBranchesTreeLevel() {
//		Node<Literal, InformationContext> parentNode;
//		List<Node<Literal, InformationContext>> nodeList, children, newChildren;
//		
//		nodeList = this.currentNode.getPath();
//		for (Node<Literal, InformationContext> n : nodeList) {
//			parentNode = n.getParent();
//			children = parentNode.getChildren(); //getting the siblings of n
//			children.remove(n);
//			
//			newChildren = new ArrayList<Node<Literal,InformationContext>>();
//			newChildren.add(n);
//			for (Node<Literal, InformationContext> child :children) {
//				if (this.savePoints.contains(child))
//					newChildren.add(child);
//				else {
//					child.setParent(null);
//					child.getChildren().clear();
//					child = null;
//				}
//			}
//			parentNode.setChildren(newChildren);
//		}
//	}
	
	/**
	 * OJO que no es exactamente como foil v6.4, para que sea igual, 
	 * cambiar los comentarios...
	 * @return
	 */
	private boolean continueSearching(Node<Literal, InformationContext> node) {
		boolean result = true;
		int newDepth = this.maxDepth;
		if (node.getDataNode().hasUnboundedVars())
			newDepth++;
		if (newDepth > this.determinancyDepth)
			result = false;
		
//		if (this.getMaxDepth(node) > this.determinancyDepth)
//			result = false;
		
		return result;
	}
	
	/**
	 * 
	 * @param body
	 * @return
	 */
//	private int getMaxDepth(Node<Literal, InformationContext> node) {
//		int result = 0;
//		List<Term> varsWithDepth0 = this.targetNode.getDataNode().getArgs();
//		List<Term> exploredVars = new ArrayList<Term>();
//		List<Node<Literal, InformationContext>> nodes = new ArrayList<Node<Literal,InformationContext>>(this.currentPath);
//		nodes.add(node);
//		
//		for (Node<Literal, InformationContext> n : nodes) {
//			if (!n.getDataNode().getPredicate().isPredefined()) {
//				List<Term> args = n.getDataNode().getArgs();
//				for (Term term : args) {
//					if (!varsWithDepth0.contains(term)) {
//						if (!exploredVars.contains(term)) {
//							result++;
//							exploredVars.add(term);
//						}
//					}
//				}
//			}
//		}
//		return result;
//	}
	
	// la regla no est� podada a�n
	// lo deber�a llamar checkstate
//	private PrologRule processRule(Node<Literal, InformationContext> finalNode) {
//		PrologRule result = new PrologRule();
//		
//		List<Node<Literal, InformationContext>> body = finalNode.getPath();
//		Node<Literal, InformationContext> headNode = body.get(0);
//		
//		result.setHead(this.cloneNode(headNode));
//		result.setBody(new ArrayList<Node<Literal,InformationContext>>(body));
//		result = this.updateCoverage(result);
//		result = removeUselessLit(result);
//		
//		return result;
//	}

	@Override
	protected FoilOutputData postProcessing(List<Node<Literal,InformationContext>> finalNodes) throws IOException {
		
		List<Node<Literal, InformationContext>> result = new ArrayList<Node<Literal,InformationContext>>();
		List<Node<Literal, InformationContext>> resultCopy = new ArrayList<Node<Literal,InformationContext>>();

		Coverage origCov = kbo.getOrigCoverage();
		outputData.setInstNumber(origCov.getPositiveBindings().size(), origCov.getNegativeBindings().size());
		
		/**
		 * STEP 1: removing duplicate rules
		 */
		
		result = this.removeSubsumedRules(finalNodes);
			
		// all Variables that are in a literal of the type EQUAL (V, C) are replaced by the constant C
		// for example, the rule target(X0, X1):- l1(X0, X2), EQUAL(X0, 'c0') is replaced by
		// the rule target(c0, X1):- l1(c0, X2).
		
		for (Node<Literal, InformationContext> n : result) {
			resultCopy.add(replacePredefinedPredicates(n));
//			this.printBranch(resultCopy.get(resultCopy.size()-1));
		}
		
		result = resultCopy;
		result = this.removeSubsumedRules(result);
		
		outputData.setRuleSet(result);
		this.trace.writeToFile(this.outputData.print());
		this.trace.closeFile();
		return this.outputData;
	}
	
	@Override
	public Node<Literal, InformationContext> postProcessBranch(Node<Literal, InformationContext> finalNode) {
		Node<Literal,InformationContext> result;
		this.updateCoverage(finalNode, false);
		result = this.postprocess(finalNode);
		try {
			this.trace.writeToFile("\nRule: " + this.printBranch(result) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Node<Literal, InformationContext> postprocess(Node<Literal, InformationContext> finalNode) {
		Node<Literal, InformationContext> result;
		int nodesNumber, prunedNodesNumber = 0;
		List<Node<Literal, InformationContext>> path = finalNode.getFullPath();
		
		this.outputData.incRuleNodesNumberBeforePrune(path.size()-1);
		nodesNumber = path.size() - 1;
		result = this.removeUselessDETLit(finalNode);
		this.updateCoverage(result, false); //watch out, maybe we should write this line after removeuselessgainfullit
		result = this.removeUselessGainfulLit(result); //it spends a lot of time
		path = result.getFullPath();
		prunedNodesNumber += (nodesNumber - (path.size()-1));
		this.outputData.setRulePrunedNodesNumber(prunedNodesNumber);
		return result;
	}
	
	private Node<Literal, InformationContext> replacePredefinedPredicates(Node<Literal, InformationContext> node) {
	
		Node<Literal, InformationContext> result = null;
		Map<Variable, Constant> positiveVC = new HashMap<Variable, Constant>();
		Map<Variable, Variable> positiveVV = new HashMap<Variable, Variable>();
		
		/**
		 * STEP 1: to get all Literals of the type EQUAL(V = C) and save the Variable V and Constant C in a Map
		 */

		List<Node<Literal,InformationContext>> path = node.getFullPath();
		List<Node<Literal,InformationContext>> body = path.subList(1, path.size());
		
		for (int i = body.size()-1; i >= 0; i--) {
			Literal l = body.get(i).getDataNode();
			if (l.getPredicate().getName().equals("EQUAL")) {
				List<Term> args = l.getArgs();
				Term arg1 = args.get(0);
				Term arg2 = args.get(1);
				if (arg1.getClass().getSimpleName().equals("Variable") && arg2.getClass().getSimpleName().equals("Constant")) {
					Term v = l.getArgs().get(0);
					Term c = l.getArgs().get(1);
					if (l.isPositive() && !positiveVC.containsKey((Variable) v))
						positiveVC.put((Variable) v, (Constant) c);	
					else if (this.partialConstantSet.containsKey(v.getType().getTypeName())) {
						List<Constant> constantList = this.partialConstantSet.get(v.getType().getTypeName());
						if (constantList.size() == 2) {
							for (int j = 0; j < constantList.size(); j++) {
								if (!c.equals(constantList.get(j)))
									c = constantList.get(j);
							}
							positiveVC.put((Variable) v, (Constant) c);
						}
					}
				}
				else if (arg1.getClass().getSimpleName().equals("Variable") && arg2.getClass().getSimpleName().equals("Variable")) {
					Term v1 = l.getArgs().get(0);
					Term v2 = l.getArgs().get(1);
					if (v1.getName().compareTo(v2.getName()) < 0) { // v1 will be the variable with a higher value that the one of the v2 
						Term aux = v1;
						v1 = v2;
						v2 = aux;
					}
					if (l.isPositive()) {
						while (positiveVV.containsKey(v1)){
							v1 = positiveVV.get(v1);
							if (v1.getName().compareTo(v2.getName()) < 0) {
								Term aux = v1;
								v1 = v2;
								v2 = aux;
							}
						}
						while (positiveVV.containsKey(v2)){
							v2 = positiveVV.get(v2);
						}
						
//						if (!positiveVC.containsKey(v1)) 
							// if exists somethink like V1 = Cj, we do not take into account V1 = Vj
							// because the first one is more specific
							positiveVV.put((Variable) v1, (Variable) v2);
					}
				}
			}
		}
			
		if (!positiveVC.isEmpty() || !positiveVV.isEmpty()) {
			
			positiveVC = transitiveClosureVC(positiveVC, positiveVV);
			
			/**
			 * STEP 2: to remove all predicates of the types EQUAL(V, C) and EQUAL(Vi,Vj)
			 */
			
			Map<Variable, Term> positiveVT = new HashMap<Variable, Term>();
			positiveVT.putAll(positiveVC);
			positiveVT.putAll(positiveVV);
		
			result = removeEqualPredicates(node, positiveVT);
			
			/**
			 * STEP 3: make the replacement in the head and in the body
			 */
			Node<Literal,InformationContext> head, root;
			head = replaceArgumentsOfTheHead(result, positiveVV);
			root = result.getRoot();
			root.setDataNode(head.getDataNode());
			root.setInformationContext(head.getInformationContext());
			result = replaceArguments(result, positiveVV);
			head = replaceArgumentsOfTheHead(result, positiveVC);
			root = result.getRoot();
			root.setDataNode(head.getDataNode());
			root.setInformationContext(head.getInformationContext());
			result = replaceArguments(result, positiveVC);
			
			positiveVT.clear();
			positiveVT.putAll(positiveVV);
			result = this.updateCoverage(result, positiveVT);
			
			positiveVT.clear();
			positiveVT.putAll(positiveVC);
			result = this.updateCoverage(result, positiveVT);
		}
		return result == null ? node:result;
	}

//	X2 = X4, X1 = X2,  
//	X1 = []
//	X1 = [], X2 = []
//	X1 = [], X2 = [], X4 = []
	
	private Map<Variable, Constant> transitiveClosureVC(
			Map<Variable, Constant> positiveVC,
			Map<Variable, Variable> positiveVV) {
		Map<Variable, Constant> result = new HashMap<Variable, Constant>(positiveVC);
		Map<Variable, Variable> posVV = new HashMap<Variable, Variable>();
		
		for (Variable v1 : positiveVV.keySet()) {
			Variable v2 = positiveVV.get(v1);
			if (result.containsKey(v1)) {
				result.put(v2, result.get(v1));
				result.putAll(transitiveClosureVC(result, posVV));
			}
			else if (result.containsKey(v2)) {
				result.put(v1, result.get(v2));
				result.putAll(transitiveClosureVC(result, posVV));
			}
			else 
				posVV.put(v1, v2);
		}
		return result;
	}

	// hay que arreglarlo a ver si se va a eliminar una regla que cubria los bindings de otra regla que se ha eliminado
	private List<Node<Literal, InformationContext>> removeSubsumedRules(List<Node<Literal, InformationContext>> finalNodes) {
		
//		KnowledgeBaseFactory kb = KnowledgeBaseFactory.getInstance();
//		BindingsOperations bop = kb.getBindingsOperations();
//		Coverage origCoverage = kb.getKnowledgeBaseOperations().getOrigCoverage();
		
		List<Node<Literal, InformationContext>> result = new ArrayList<Node<Literal,InformationContext>>();
		List<List<Node<Literal, InformationContext>>> branches = new ArrayList<List<Node<Literal,InformationContext>>>();
		for (Node<Literal, InformationContext> node : finalNodes)
			branches.add(node.getFullPath());
		
		Set<Bindings> bindings, restOfBindings;
		
		List<List<Node<Literal, InformationContext>>> listToCheck = null;
		Set<List<Node<Literal, InformationContext>>> rulesToRemove = new HashSet<List<Node<Literal,InformationContext>>>();
		List<Node<Literal, InformationContext>> ruleToCheck = null;
		
		if (branches.size() >= 2) {
			int i = 0;
			Iterator<List<Node<Literal, InformationContext>>> it = branches.iterator();
			while (it.hasNext() && !(branches.size() == 2 && i == 1)) {
				List<Node<Literal, InformationContext>> branch = it.next();
				listToCheck = new ArrayList<List<Node<Literal,InformationContext>>>();
				ruleToCheck = branch;
				
				if (i != 0)
					listToCheck.addAll(branches.subList(0, i));
				if (i+1 < branches.size())
					listToCheck.addAll(branches.subList(i+1, branches.size()));
				
				listToCheck.removeAll(rulesToRemove);
				
				bindings = new HashSet<Bindings>();
				
//				bop.origmatch(origCoverage, ruleToCheck.get(0), literalList).getPositiveBindings();
				
				bindings.addAll(ruleToCheck.get(ruleToCheck.size()-1).getInformationContext().getOrigCoverage().getPositiveBindings());
				
				restOfBindings = new HashSet<Bindings>();
				for (List<Node<Literal, InformationContext>> li : listToCheck) {
					restOfBindings.addAll(li.get(li.size()-1).getInformationContext().getOrigCoverage().getPositiveBindings());
				}
				
				if (!listToCheck.isEmpty())	
					if (!bop.essential(bindings, restOfBindings))
						rulesToRemove.add(ruleToCheck);
				i++;
			}
			
			// Add the minimal set of rules that covers all positive tuples
			branches.removeAll(rulesToRemove);		
		}
		for (List<Node<Literal, InformationContext>> list : branches)
			result.add(list.get(list.size()-1));
		return result;
	}

	/**
	 * Remove all equal(Variable, Constant) predicates of a rule
	 * @param body body of the rule
	 * @return the new body of the rule without the kind of the predicate equal(Variable, Constant)
	 */
	private Node<Literal,InformationContext> removeEqualPredicates(Node<Literal,InformationContext> node, Map<Variable, ? extends Term> positiveVT) {
	
		Node<Literal, InformationContext> result, nextNode;
		List<Node<Literal, InformationContext>> branch;
		Literal l = null;
		List<Term> args = null;
		boolean equalPredicate = false;
		double bits = 0, nextBits = 0;
		boolean ok = true;
		
		//first, the negated predefined literals are removed
		branch = node.getFullPath();
		Node<Literal, InformationContext> head = branch.get(0);
		List<Node<Literal, InformationContext>> body = branch.subList(1, branch.size());
		List<Node<Literal, InformationContext>> newBody = new ArrayList<Node<Literal,InformationContext>>();
		
		
		// first we drop each negated literal of the type Vi != Cj where Vi is a variable and Cj is a constant
		// in those cases in which there is a not negated literal in the rule of the type Vi = Cj.
		for (Node<Literal, InformationContext> n : body) {
			l = n.getDataNode();
			equalPredicate = l.getPredicate().getName().equals("EQUAL");
			
			if (equalPredicate && l.isNegated()) {
				ok = true;
				args = l.getArgs();
				for (Term term : args) {
					if (positiveVT.containsKey(term))
						ok = false;
				}
				if (ok)
					newBody.add(n);
			}
			else
				newBody.add(n);
		}
		
		body = newBody;
		newBody = new ArrayList<Node<Literal,InformationContext>>();
		//after, the positive predefined literals are removed
		Node<Literal, InformationContext> n = null;
		for (int i = 0; i < body.size(); i++) {
			n = body.get(i);
			l = n.getDataNode();
			equalPredicate = l.getPredicate().getName().equals("EQUAL");
			if ((equalPredicate && l.isPositive()) || (equalPredicate && l.isNegated() && positiveVT.containsKey(l.getArgs().get(0)))) {
				args = l.getArgs();
//				Term arg1 = args.get(0);
//				Term arg2 = args.get(1);
				bits = n.getInformationContext().getBits();
//				if (!(arg1 instanceof Variable && arg2 instanceof Constant)){
//					newBody.add(node);
//				}
//				else {
					// adding the bits of the removed predefined literals to the next literal or previous (if the first one does not exists).
					if (i-1 > body.size()) {
						nextNode = body.get(i-1);
					}
					else
						nextNode = head;
					
					nextBits = nextNode.getInformationContext().getNewRuleBits();
					nextNode.getInformationContext().setNewRuleBits(bits + nextBits);
//				}
			}
			else
				newBody.add(n);
		}
		result = this.updateBranch(head,newBody);
		return result;
	}
	
	/**
	 * Replace a variable with a Constants if a predicate of the kind equal(V,C) was found
	 * @param body body of the rule under postprocessing 
	 * @param positiveVC map with bindings of the kind V = C
	 * @param negativeVC map with bindings of the kind V <> C
	 * @return a new body with all replacements made
	 */
	private Node<Literal, InformationContext> replaceArguments(
			Node<Literal, InformationContext> node,
			Map<Variable, ? extends Term> positiveVT) {
		
		Node<Literal, InformationContext> result;
		Node<Literal, InformationContext> n, head, nBody;
		List<Node<Literal,InformationContext>> path = node.getFullPath();
		
		head = path.get(0);
		List<Node<Literal,InformationContext>> body = path.subList(1, path.size());
		
		n = new Node<Literal, InformationContext>();
		n.setDataNode(head.getDataNode().clone());
		n.setInformationContext(head.getInformationContext().clone());
		result = n;
		
		Term value = null;
		
		for (int i = 0; i < body.size(); i++) {
			nBody = body.get(i);
			n = new Node<Literal, InformationContext>();
			n.setDataNode(nBody.getDataNode().clone());
			n.setInformationContext(nBody.getInformationContext().clone());
			
			Literal l = n.getDataNode();
			if (!l.getPredicate().isPredefined()) {
				List<Term> newArgs = new ArrayList<Term>();
				List<Term> args = l.getArgs();
				for (Term term : args) {
					if (positiveVT.containsKey(term)) {
						value = positiveVT.get(term);
						newArgs.add(value);
					}
					else
						newArgs.add(term);
				}
				l.setArgs(newArgs);
			}
			
			result.addChild(n);
			n.setParent(result);
			result = n;
		}
		return result;
	}
	
	private Node<Literal, InformationContext> removeUselessDETLit(Node<Literal, InformationContext> node) {
		
		Node<Literal, InformationContext> result, head;
		List<Node<Literal, InformationContext>> newBody = new ArrayList<Node<Literal,InformationContext>>();
		List<Node<Literal,InformationContext>> path = node.getFullPath();
		int branchSize = path.size();
		head = path.get(0);
	
		List<Node<Literal, InformationContext>> bodyToCheck = new ArrayList<Node<Literal,InformationContext>>();
		if (branchSize > 1)
			bodyToCheck.addAll(path.subList(1, branchSize));
		
		boolean ok = true, end = false;
		Set<Term> varsToRemove = null;
		Node<Literal, InformationContext> essentialNode = null , nextNode = null;
		InformationContext icEssentialNode = null;
		List<Term> essentialNodeArgs = null, nextNodeArgs = null;
		
		Set<Variable> newVars = null;
		Set<Term> newEssentialNodeVars = null, newEssentialNodeVarsUsed = null;
		
		if (!head.getInformationContext().getOrigCoverage().getNegativeBindings().isEmpty()) { //it means that the rule has a non-empty body
			newVars = this.getNewVariables(head, bodyToCheck);
			varsToRemove = new HashSet<Term>();
			newEssentialNodeVars = new HashSet<Term>();
			newEssentialNodeVarsUsed = new HashSet<Term>();
			
			for (int i = bodyToCheck.size()-1; i >= 0; i--) {
				end = false;
				ok = true;
				
				essentialNode = bodyToCheck.get(i);	
				icEssentialNode = essentialNode.getInformationContext();
				
				if (icEssentialNode.isDeterminate()) {
					essentialNodeArgs = essentialNode.getDataNode().getArgs();
					newEssentialNodeVars.clear();
					
					// getting the new vars added by this node (essentialNode)
					for (int k = 0; k < essentialNodeArgs.size(); k++) {
						Term t = essentialNodeArgs.get(k);
						if (newVars.contains(t))
							newEssentialNodeVars.add(t);
					}
					
					if (!newEssentialNodeVars.isEmpty()) {
						// ckecking if new variables are used in next literals
						for (int j = 0; j < newBody.size() && !end; j++) {
							
							nextNode = newBody.get(j);
							nextNodeArgs = nextNode.getDataNode().getArgs();
							
							/**
							 * Checking if any of the new variables is used in next literals
							 * if some variable is used later, the determinate literal can not be dropped
							 * otherwise, the determinate literal is dropped from the rule  
							 */
							
							for (int k = 0; k < nextNodeArgs.size() && !end; k++) {
								Term t = nextNodeArgs.get(k);
								if (newEssentialNodeVars.contains(t)) {
									end = true;
									for (int l = 0; l <= i-1 && end; l++) {
										if (bodyToCheck.get(l).getDataNode().getArgs().contains(t))
											end = false;
									}
									if (end) 
										newEssentialNodeVarsUsed.add(t);
								}
							}
						}
					}
					if (!end) {
						
						Literal l;
						List<Literal> body1 = new ArrayList<Literal>();
						List<Literal> body2 = new ArrayList<Literal>();
						Coverage origCoverage = head.getInformationContext().getOrigCoverage();
						
						for (int j = 0; j < i; j++) {
							l = bodyToCheck.get(j).getDataNode();
							body1.add(l);
							body2.add(l);
						}
						body2.add(essentialNode.getDataNode());
						for (Node<Literal, InformationContext> n : newBody) {
							l = n.getDataNode().clone();
							body1.add(l);
							body2.add(l);
						}
						
						Set<Bindings> posBindings1, posBindings2;
						Set<Bindings> negBindings1, negBindings2;
						
						posBindings1 = posBindings2 = origCoverage.getPositiveBindings();
						negBindings1 = negBindings2 = origCoverage.getNegativeBindings();
						
						if (bop.nMatches(posBindings1,head,body1) >= bop.nMatches(posBindings2, head, body2) &&
								bop.nMatches(negBindings1, head, body1) <= bop.nMatches(negBindings2, head, body2)) {
							
							ok = false;
							varsToRemove.addAll(newEssentialNodeVars);
						}
					}
				}
			
				if (ok) {
					for (Term term : varsToRemove) { //deleting new vars not used from the coverage
						icEssentialNode.getCoverage().removeVar(term);
					}
					newBody.add(0,essentialNode);
				}
			}
		}
		
		result = this.updateBranch(head, newBody);
		return result;
	}
	
	private Node<Literal, InformationContext> updateBranch(Node<Literal, InformationContext> head, List<Node<Literal, InformationContext>> newBody) {
		Node<Literal, InformationContext> result = new Node<Literal,InformationContext>();
		Node<Literal, InformationContext> node, n;
		double bits;
		
		node = new Node<Literal, InformationContext>();
		node.setDataNode(head.getDataNode().clone());
		node.setInformationContext(head.getInformationContext().clone());
		result = node;
		
		for (int j = 0; j < newBody.size(); j++) {
			
			n = newBody.get(j);
			node = new Node<Literal, InformationContext>();
			node.setDataNode(n.getDataNode().clone());
			node.setInformationContext(n.getInformationContext().clone());
			bits = result.getInformationContext().getNewRuleBits() + node.getInformationContext().getBits();
			node.getInformationContext().setNewRuleBits(bits);
			node.setParent(result);
			result.addChild(node);
			result = node;
		}
		return result;
	}

	/**
	 * Gets the set of new variables in the rule. They are variables that 
	 * do not belong to the list of arguments of the head literal.
	 * @return a set with all new variables
	 */
	public Set<Variable> getNewVariables(Node<Literal,InformationContext> head, List<Node<Literal,InformationContext>> body) {
		Set<Variable> result = new HashSet<Variable>();
		List<Term> headArgs = head.getDataNode().getArgs();
		
		for(Node<Literal,InformationContext> n: body) {
			for(Term t: n.getDataNode().getArgs()) {
				if (t.belongs(headArgs) < 0 && t instanceof Variable)
					result.add((Variable) t);
			}
		}
		
		return result;
	}
	
	/**
	 * Removes the useless determinate literal, i.e when a determinate literal has not gain and its new variables
	 * are not used in the next literals of the rule
	 * @param newRule body of the rule
	 * @return body with useful nodes. The useless nodes are pruned from the body of the rule
	 */
	
	private Node<Literal,InformationContext> removeUselessGainfulLit(Node<Literal,InformationContext> node) {
		
		Node<Literal,InformationContext> result;
		List<Node<Literal, InformationContext>> path = node.getFullPath();
		Node<Literal, InformationContext> head = path.get(0);
		List<Node<Literal, InformationContext>> newBody = new ArrayList<Node<Literal,InformationContext>>();
		
		boolean ok = true, end = false;
		Set<Term> varsToRemove = null;
		List<Node<Literal, InformationContext>> bodyToPrune = null;
		Node<Literal, InformationContext> essentialNode = null , nextNode = null;
		InformationContext icEssentialNode = null;
		List<Term> essentialNodeArgs = null, nextNodeArgs = null;
		
		Set<Variable> newVars = null;
		Set<Term> newEssentialNodeVars = null, newEssentialNodeVarsUsed = null;
		
		if (!head.getInformationContext().getOrigCoverage().getNegativeBindings().isEmpty()) {
			bodyToPrune = path.subList(1, path.size());
			newVars = this.getNewVariables(head, bodyToPrune);
			varsToRemove = new HashSet<Term>();
			newEssentialNodeVars = new HashSet<Term>();
			newEssentialNodeVarsUsed = new HashSet<Term>();
			
			for (int i = bodyToPrune.size()-1; i >= 0; i--) {
				end = false;
				
				essentialNode = bodyToPrune.get(i);
				icEssentialNode = essentialNode.getInformationContext();
				ok = true;
					
				essentialNodeArgs = essentialNode.getDataNode().getArgs();
				newEssentialNodeVars.clear();
				
				// getting the new vars added for this node (essentialNode)
				for (int k = 0; k < essentialNodeArgs.size(); k++) {
					Term t = essentialNodeArgs.get(k);
					if (newVars.contains(t))
						newEssentialNodeVars.add(t);
				}
				
				if (!newEssentialNodeVars.isEmpty()) {
					// ckecking the new variables that are used in next literals
					for (int j = 0; j < newBody.size() && !end; j++) {
						
						nextNode = newBody.get(j);
						nextNodeArgs = nextNode.getDataNode().getArgs();
						
						/**
						 * Checking if any of the new variables is used in next literals
						 * if some variable is used later, the determinate literal can not be dropped
						 * otherwise, the determinate literal is dropped from the rule  
						 */
						
						for (int k = 0; k < nextNodeArgs.size() && !end; k++) {
							Term t = nextNodeArgs.get(k);
							if (newEssentialNodeVars.contains(t)) {
								end = true;
								for (int l = 0; l <= i-1 && end; l++) {
									if (bodyToPrune.get(l).getDataNode().getArgs().contains(t))
										end = false;
								}
								if (end) 
									newEssentialNodeVarsUsed.add(t);
							}
						}
					}
				}

				if (!end) {
					
					Literal l;
					List<Literal> body1 = new ArrayList<Literal>();
					List<Literal> body2 = new ArrayList<Literal>();
					Coverage origCoverage = head.getInformationContext().getOrigCoverage();
					
					for (int j = 0; j < i; j++) {
						l = bodyToPrune.get(j).getDataNode();
						body1.add(l);
						body2.add(l);
					}
					body2.add(essentialNode.getDataNode());
					for (Node<Literal, InformationContext> n : newBody) {
						l = n.getDataNode();
						body1.add(l);
						body2.add(l);
					}
					
					Set<Bindings> posBindings1, posBindings2, negBindings1, negBindings2;
					posBindings1 = posBindings2 = origCoverage.getPositiveBindings();
					negBindings1 = negBindings2 = origCoverage.getNegativeBindings();
					
					if (bop.nMatches(posBindings1, head, body1) >= bop.nMatches(posBindings2, head, body2) &&
							bop.nMatches(negBindings1, head, body1) <= bop.nMatches(negBindings2, head, body2)) {
						
						ok = false;
						varsToRemove.addAll(newEssentialNodeVars);
					}
				}
				
				if (ok) {
					for (Term term : varsToRemove) { //deleting new vars not used from the coverage
						icEssentialNode.getCoverage().removeVar(term);
					}
					newBody.add(0,essentialNode);
				}
			}
		}

		result = this.updateBranch(head,newBody);
		return result;
	}
	
		
	/**
	 * Replace a Variable with a Constant in the head of the rule if a predicate of the kind equal(V,C) was found
	 * @param headIC 
	 * @param body body of the rule
	 * @param positiveVC map with bindings of the kind V = C
	 * @param negativeVC map with bindings of the kind V <> C
	 * @return a new head node with all replacements made
	 */
	private Node<Literal, InformationContext> replaceArgumentsOfTheHead(
			Node<Literal,InformationContext> node, Map<Variable, ? extends Term> positiveVT) {
		
		Node<Literal, InformationContext> result = new Node<Literal, InformationContext>();
		
		List<Node<Literal, InformationContext>> branch = node.getFullPath();
		Node<Literal,InformationContext> head = branch.get(0);
		Literal l = head.getDataNode().clone();
		InformationContext headIC = head.getInformationContext().clone();
		
		List<Term> newArgs = new ArrayList<Term>();
		for (Term term : l.getArgs()) {
			if (positiveVT.containsKey(term))
				newArgs.add(positiveVT.get(term));
			else 
				newArgs.add(term);
		}
		l.setArgs(newArgs);
		
		result.setDataNode(l);
		result.setInformationContext(headIC);
		result.setChildren(node.getChildren());
		
		return result;
	}

	/**
	 * check wether the literal pair generated already exits in the body rule or in the pruned nodes with subsumed arguments. 
	 * We also rule out literals which contain some of its variables in a previous predefined literal in the rule.
	 * i.e., if the rule contain the literal EQUAL(X0,X1), it has no sense evaluate p1(X0,X0,X3) and p1(X0,X1,X3).   
	 * @param lp literal pair
	 * @param nodeListToCheck 
	 * @return true either the literal or negated literal in lp exists in the body rule or in mostGeneralNodesPruned list, false otherwise
	 */
	private Literal isAnEquivalentLiteral(LiteralPair lp, boolean checkBodyRule) {
		Literal result = null;
		boolean ok = false;
		Literal l = lp.getLiteral();
		Literal nl = lp.getNegatedLiteral();
		List<Node<Literal, InformationContext>> nodeListToCheck = checkBodyRule ? this.currentPath : this.mostGeneralNodesPruned;
		Iterator<Node<Literal, InformationContext>> it = nodeListToCheck.iterator();
		
		List<Node<Literal, InformationContext>> partialBody = new ArrayList<Node<Literal,InformationContext>>();
		
		Node<Literal, InformationContext> n = null;
		Map<Term, Term> equalVars = new HashMap<Term, Term>();
		Term t1 = null, t2 = null, t_1 = null, t_2 = null;
		
		while (it.hasNext() && !ok) {
			n = it.next();
			Literal aux = n.getDataNode();
			Predicate p = aux.getPredicate();
			
			if (aux.equals(l) || aux.equals(nl)) {
				result = aux.clone();
				ok = true;
			}
			else if (checkBodyRule ? subsumed(aux, l, equalVars) : subsumed (l, aux, equalVars)) {
				ok = true;
				if (checkBodyRule) {
					partialBody.add(n); 
					if (p.isPredefined() && p.getName().equals("EQUAL")) {
						List<Term> args = aux.getArgs();
						equalVars.put(args.get(1),args.get(0));
					}
					
					Set<Term> lastNewVars = BranchOperations.getLastNewVariables(this.targetNode,partialBody);
					for (int i = 0; i < aux.size() && ok; i++) {
						t1 = aux.getArgs().get(i);
						t2 = l.getArgs().get(i);
						if (t2.isBound() && !t1.equals(t2) && !((equalVars.containsKey(t2) && equalVars.get(t2).equals(t1))
								|| (equalVars.containsKey(t1) && equalVars.get(t1).equals(t2))))
							ok = false;
						else if (!lastNewVars.contains(t1) && !t2.equals(t1)) {
							if (t1.getName().compareTo(t2.getName()) <= 0) {
								t_1 = t2;
								t_2 = t1;
							}
							else {
								t_1 = t1;
								t_2 = t2;
							}
							if (!(equalVars.containsKey(t_1) && equalVars.get(t_1).equals(t_2)))
								ok = false;
						}
					}
				}
				if (ok) result = aux.clone();
			}
			else if (checkBodyRule) {
				partialBody.add(n);
				if (p.isPredefined() && p.getName().equals("EQUAL")) {
					List<Term> args = aux.getArgs();
					equalVars.put(args.get(1),args.get(0));
				}
			}
		}
		
		return result;
	}

	/**
	 * return true if l1 is subsumed by l2, false otherwise.
	 * @param l1
	 * @param l2
	 * @return
	 */
	private boolean subsumed(Literal l1, Literal l2, Map<Term, Term> equalVars) {
		boolean result = true;
		Term t1 = null, t2 = null;
		List<Term> termsl1 = l1.getArgs();
		List<Term> termsl2 = l2.getArgs();
		Set<Term> existingVars = new HashSet<Term>();
		
		if (l1.getPredicate().equals(l2.getPredicate())) {
			for (int i = 0; i < termsl2.size() && result; i++) {
				t2 = termsl2.get(i);
				if (t2.isBound()) {
					if (!existingVars.contains(t2)) {
						existingVars.add(t2);
						t1 = termsl1.get(i);
						if (!t1.equals(t2) && !(equalVars.containsKey(t2) && equalVars.get(t2).equals(t1)))// || (termsl1.get(i).equals(t) && newVars.contains(t)))
							result = false;
					}
				}
			}
		}
		else
			result = false;
		
	
		return result;
	}

	@Override
	protected void createTraceFile() {
		boolean end = false;
		int i = 0;
		String absolutePath = null, name = null;
		String fileName = "";
		String targetPredicate = this.targetNode.getDataNode().getPredicate().getName();
		File parentFile;
		
		
		String inputFile = "";
		try {
			inputFile = PropertiesStore.getProperty("knowledgeBase");
			parentFile = new File(inputFile).getParentFile().getAbsoluteFile();
			fileName = inputFile.substring(inputFile.lastIndexOf("/")+1, inputFile.lastIndexOf("."));
			
			inputFile = (fileName.compareToIgnoreCase(targetPredicate) == 0 ? targetPredicate:fileName + "_" + targetPredicate) + "_" + parentFile.getName() + "_" + this.heuristic.getStrategyTypeIn(); 

		} catch (FoilException e) {
			e.printStackTrace();
		}
		
		/**
		 * Creating output file
		 */
		
		File f = new File (outputDir);
		f.mkdirs();
		if (f.exists()) {
			name =  "/" + inputFile;
	
			while (!end) {
				absolutePath = this.outputDir + name + i + ".txt";
				File dst = new File(absolutePath);
				if (dst.exists())
					i++;
				else 
					end = true;
			}
			
			f = new File(absolutePath);
			try {
				if (!f.createNewFile())
					new FoilException("The file " + f.getAbsolutePath() + "could not be created");
				else  {
					this.trace = new OutputDataFile(f);
					Map<String, List<String>> consistentArguments = this.constantsData.getConsistentArguments();
					for (String predicateName : consistentArguments.keySet()) {
						this.trace.writeToFile("\nChecking arguments in " + predicateName + "\n");
						for (String s : consistentArguments.get(predicateName))
							this.trace.writeToFile("\t\t" + s + "\n");
					}
					
					Map<String, List<String>> finalOrders = this.constantsData.getFinalOrders();
					if (!finalOrders.isEmpty()) {
						this.trace.writeToFile("\n\tFinal order: \n");
						for (String predicateName : finalOrders.keySet()) {
							this.trace.writeToFile("\t" + predicateName + ":\n");
							StringBuffer b = new StringBuffer();
							for (String cValue : finalOrders.get(predicateName))
								b.append(cValue + ",");
							
							if (b.lastIndexOf(",") != -1) {
								String text = b.substring(0, b.lastIndexOf(",")) + "\n";
								this.trace.writeToFile("\t\t" + text + "\n");
							}
							
						}
						List<ConsistentPairs> cpList = this.constantsData.getConsistentPairs();
						for (ConsistentPairs cp : cpList) {
							int sign = cp.getSign();
							String s = "";
							if (sign == 0)
								s = "==";
							else if(sign == 1)
								s = "<";
							else if (sign == 2)
								s = ">";
							
							this.trace.writeToFile(cp.getPredicateName() + ":   " + (cp.getFirstArg()+1) + " " + s + " " + (cp.getSecondArg()+1) + "\n");
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
//		FileUtilities.writeToFile(f.getAbsolutePath(), "\n SCORING FUNCTION: " + heuristicType + "\n");
	}

	@Override
	protected boolean forceCut() {
		boolean result = false;
		int currentPathSize = this.currentPath.size();
		Node<Literal, InformationContext> lastNode = currentPathSize > 0 ? this.currentPath.get(this.currentPath.size()-1) : this.targetNode;
		InformationContext ic = lastNode.getInformationContext();
		int coveredPosBindings = ic.getOrigCoverage().getPositiveBindings().size();
		result = (Utilities.encode(coveredPosBindings, allTuples) - ic.getNewRuleBits()) < 0;
		return result;
	}
	
	
	/**
	 * Updates the coverage of the body of a rule. It is called, when some replacement of literals or arguments occurs 
	 * @param origCoverage
	 * @param newCoverage
	 * @param dataNode
	 * @return
	 */

	
	private void updateCoverage(Node<Literal, InformationContext> leaf, boolean partialCheck) {
		List<Node<Literal, InformationContext>> branch;
		InformationContext ic = null;
		Coverage origcr = null, newcr = null;
		Node<Literal,InformationContext> previousNode, node, head;
		List<Literal> literals = new ArrayList<Literal>();
		List<Node<Literal, InformationContext>> body = new ArrayList<Node<Literal,InformationContext>>();
		int startIndex =  1;
		
		branch = leaf.getFullPath();
		head = branch.get(0);
		
		if (partialCheck) {
			if (branch.size() > 2) {
				startIndex = branch.size()-2;
				previousNode = branch.get(startIndex);
				startIndex++;
				body.addAll(branch.subList(1, startIndex));
				for (int i = 1; i < startIndex; i++)
					literals.add(branch.get(i).getDataNode());
			}
			else
				previousNode = head;
			
			origcr = previousNode.getInformationContext().getOrigCoverage();
			newcr = previousNode.getInformationContext().getCoverage();
		}
		else {
			origcr = newcr = kbo.getOrigCoverage();
			previousNode = head;
			previousNode.getInformationContext().setOrigCoverage(origcr);
			previousNode.getInformationContext().setCoverage(newcr);
		}
		
		for (int i = startIndex; i < branch.size(); i++) {
			node = branch.get(i);
			body.add(node);
			literals.add(node.getDataNode());
			node.getInformationContext().setCoverage(bop.match(newcr, head.getDataNode(), literals));
			node.getInformationContext().setOrigCoverage(bop.origmatch(origcr, head,literals));
			
			//updating the confusion matrix list
			node.getInformationContext().setConfusionMatrixList(updateConfusionMatrixList(head,body));
			node.getInformationContext().setScore(heuristic.getHeuristicStrategy().score(node));
			node.getInformationContext().setGain(this.assess(node, previousNode.getInformationContext().getScore()));
			
			ic = node.getInformationContext();
			origcr = ic.getOrigCoverage();
			newcr = ic.getCoverage();
			
			previousNode = node;
		}
	}
	
	
	/**
	 * Updates the coverage of the body of a rule. It is made, when a replacement of the arguments occurs 
	 * @param origCoverage
	 * @param newCoverage
	 * @param dataNode
	 * @return
	 */
	
	private Node<Literal, InformationContext> updateCoverage(Node<Literal,InformationContext> node, Map<Variable, Term> positiveVT) {
		
		Node<Literal, InformationContext> result, head, origNode;
		List<Node<Literal, InformationContext>> branch;
		Coverage newcr = null, origcr = null;
		InformationContext ic = null;
		List<Literal> literals = new ArrayList<Literal>();
		
		branch = node.getFullPath();
		head = branch.get(0);
		origcr = newcr = this.updateCoverage(head.getInformationContext().getCoverage(), positiveVT);
		
		result = new Node<Literal, InformationContext>();
		result.setDataNode(head.getDataNode().clone());
		result.setInformationContext(head.getInformationContext().clone());
		result.getInformationContext().setOrigCoverage(origcr);
		result.getInformationContext().setCoverage(newcr);
		
		for (int i = 1; i < branch.size(); i++) {
			Node<Literal, InformationContext> n = new Node<Literal, InformationContext>();
			origNode = branch.get(i);
			n.setDataNode(origNode.getDataNode().clone());
			n.setInformationContext(origNode.getInformationContext().clone());
			literals.add(n.getDataNode());
			n.getInformationContext().setCoverage(bop.match(newcr, head.getDataNode(), literals));
			n.getInformationContext().setOrigCoverage(bop.origmatch(origcr, head,literals));
			
			ic = n.getInformationContext();
			origcr = ic.getOrigCoverage();
			newcr = ic.getCoverage();
			
			n.setParent(result);
			result.addChild(n);
			result = n;
		}
		return result;
	}
	
	private Coverage updateCoverage(Coverage cr, Map<Variable, Term> positiveVT) {
		Coverage result = new Coverage();
		Iterator<Variable> itVars = null;
		Iterator<Bindings> itBindings = null;
		boolean end = false;
		Bindings firstBinding = null;
		Set<Bindings> posBindings = null, negBindings = null;
		Map<Variable, Term> subSetPosVT = new HashMap<Variable, Term>();
		
		// Get one single binding to check if it contains some of the variables contained in positiveVC.
		itBindings = cr.getPositiveBindings().iterator();
		if (itBindings.hasNext()) {
			firstBinding = itBindings.next();
			itVars = positiveVT.keySet().iterator();
			while (itVars.hasNext()) {
				Variable v = itVars.next();
				if (firstBinding.containsVariable(v))
					subSetPosVT.put(v, positiveVT.get(v));
			}
		}
		
		// Checking the bindings and removed those that not satisfied the equalities contained in positiveVC
		
		posBindings = new HashSet<Bindings>();
		negBindings = new HashSet<Bindings>();
		
		// checking positive bindings
		for (Bindings b : cr.getPositiveBindings()) {
			itVars = subSetPosVT.keySet().iterator();
			end = false;
			while (itVars.hasNext() && !end) {
				Variable v = itVars.next();
				Term t = subSetPosVT.get(v);
				if (t instanceof Constant) {
					if (!b.getConstant(v).equals(t))
						end = true;
				}
				else {
					if (!b.getConstant(v).equals(b.getConstant(t)))
						end = true;
				}
					
			}
			if (!end)
				posBindings.add(b);
		}
		
		// checking negative bindings
		for (Bindings b : cr.getNegativeBindings()) {
			itVars = subSetPosVT.keySet().iterator();
			end = false;
			while (itVars.hasNext() && !end) {
				Variable v = itVars.next();
				Term t = subSetPosVT.get(v);
				if (t instanceof Constant) {
					if (!b.getConstant(v).equals(t))
						end = true;
				}
				else {
					if (!b.getConstant(v).equals(b.getConstant(t)))
						end = true;
				}
			}
			if (!end)
				negBindings.add(b);
		}
		
		// final Coverage
		result.setPositiveBindings(posBindings);
		result.setNegativeBindings(negBindings);
		
		return result;
	}
	
	private Node<Literal, InformationContext> cloneNode(Node<Literal, InformationContext> nodeToClone) {
		Node<Literal, InformationContext> result = new Node<Literal, InformationContext>();
		List<Node<Literal, InformationContext>> children = new ArrayList<Node<Literal,InformationContext>>(nodeToClone.getChildren());
		
		Literal l = nodeToClone.getDataNode().clone();
		InformationContext ic = nodeToClone.getInformationContext().clone();
		
		result.setDataNode(l);
		result.setInformationContext(ic);
		result.setParent(nodeToClone.getParent());
		result.setChildren(children);
		
		return result;
	}

	@Override
	protected Node<Literal, InformationContext> replacementCheck() throws IOException {
		Node<Literal, InformationContext> result = currentNode;
		Node<Literal, InformationContext> parent = null;
		List<Literal> literalList = null;
		InformationContext ic = null;
		Iterator<Node<Literal, InformationContext>> it = null;
		Node<Literal, InformationContext> savedPoint = null, root = null;
		ConfusionMatrix origCM_savedPoint = null;
		int tp_currentNode = 0, tp_savedPoint = 0;
		boolean end = false;
		if (!this.savePoints.isEmpty()) {
			Collections.sort(this.savePoints,fc); // esto cada vez que se guarda un save point
			it = this.savePoints.iterator();
			while (!end && it.hasNext()) {
				savedPoint = it.next();
				origCM_savedPoint = savedPoint.getInformationContext().getConfusionMatrixList().getOrigcm();
				if (origCM_savedPoint.getFalsePositives() == 0) { //check only for complete nodes (nodes that do not cover any negative binding
					tp_savedPoint = origCM_savedPoint.getTruePositives();
					tp_currentNode = result.getInformationContext().getConfusionMatrixList().getOrigcm().getTruePositives();
					
					if (tp_savedPoint > tp_currentNode) {
						
						List<Node<Literal, InformationContext>> body = new ArrayList<Node<Literal,InformationContext>>();
						
						end = true;
						result = savedPoint;
						ic = result.getInformationContext();
						parent = result.getParent();
						
						literalList = new ArrayList<Literal>();
						for (Node<Literal, InformationContext> node : result.getPath()){
							body.add(node);
							literalList.add(node.getDataNode());
						}
						
						root = body.get(0).getParent();
						// Updating the savedPoint getting all covered bindings (coverage)
						ic.setCoverage(bop.match(parent.getInformationContext().getCoverage(), result.getDataNode()));
						ic.setOrigCoverage(bop.origmatch(root.getInformationContext().getOrigCoverage(), root, literalList));
					
				
						this.trace.writeToFile("\n" + BranchOperations.pathToString(this.targetNode, this.currentPath) + " \nReplaced By Saved Rule: " + BranchOperations.pathToString(this.targetNode, body) + "\n");
					}
				}
			}
		}
		
//		System.out.println(result.getInformationContext().getOrigCoverage().toString());
//		System.out.println(result.getInformationContext().getCoverage().toString());
		
		return result;
	}

	@Override
	protected String printBranch(Node<Literal, InformationContext> node) {
		List<Node<Literal, InformationContext>> path = node.getFullPath();
		String result = "";
		boolean first = true;
		for (Node<Literal, InformationContext> n : path) {
			result += n.getDataNode().toString() + (first ? ":-" : ",");
			first = false;
		}
		if (result.lastIndexOf(",") != -1)
			result = result.substring(0, result.lastIndexOf(","));
		result += ".";
//		System.out.println(result);
		return result;
		
	}

//	private void printTree(int spaceNumber,Node<Literal,InformationContext> node) {
//		
//		for (int i = 0; i < spaceNumber; i++)
//			System.out.print(" ");
//		System.out.print(this.savePoints.contains(node) ? "SP: ":"");
//		System.out.println(node.getDataNode().toString() + " id: " + node.hashCode());
//		
//		spaceNumber += 4;
//		for (Node<Literal, InformationContext> n : node.getChildren()) {
//			printTree(spaceNumber,n);
//		}
//	}
	
	
	
//	private PrologRule regrowRule(List<Node<Literal, InformationContext>> nodeList, Node<Literal, InformationContext> head) {
//		
//		
//		PrologRule result = new PrologRule();
//		result.setHead(this.cloneNode(head));
//		result.setBody(new ArrayList<Node<Literal, InformationContext>>(nodeList));
//		Node<Literal, InformationContext> selectedNode = null, node = null;
//		Literal l = null;
//		Literal lhead = null;
//		InformationContext ic = null;
//		
//		if (nodeList.size() == 1) {
//	
//			selectedNode = nodeList.get(0);
//			l = selectedNode.getDataNode();
//			ic = selectedNode.getInformationContext();
//			lhead = head.getDataNode();
//			
//			List<Node<Literal, InformationContext>> bodyAux = null;
//			
//			// Checking if we can regrow the rule in production
//			if (!ic.isDeterminate()) {
//				if (!l.getPredicate().isTarget() && l.getMaxVar() < lhead.getPredicate().getArity() && 
//						result.getMaxVar() > lhead.getPredicate().getArity() && result.getDetLitNumber() != result.size()) {
//					// if the literal does not add any new variable means we can remove some previous literal with new variables that were not used in later literals
//					
//					List<Node<Literal, InformationContext>> newBody = new ArrayList<Node<Literal,InformationContext>>();
//					
//					bodyAux = result.getBody();
//					for (int i = bodyAux.size() - 1; i >= 0; i--) {
//						node = bodyAux.get(i);
//						l = node.getDataNode();
//						if (l.getMaxVar() > lhead.getMaxVar() && !node.getInformationContext().isDeterminate()) { // this literal added a new variable and it is not a determinate literal
//							// removing all children of this node because it is going to be deleted too
//							
//							this.prepareForRemoveNode(node, selectedNode, newBody); //updating the exploration tree
//							node.getChildren().clear();
//							
//							if (newBody.size() > 0)
//								node.addChild(newBody.get(0));
//							else 
//								node.addChild(selectedNode);
//							
//							// if the node that is going to be removed is determinate (now it is not allowed), remove from the list of explored determinate literals too
////							if (node.getInformationContext().isDeterminate()) {
////								this.exploredDetLitList.remove(node);
////								this.exploredDetLit = this.exploredDetLitList.size() > 0;
////							}
//							
//							// removing node
//							this.removeNode(node);
//		
////							System.out.println("Hijos de " + fatherNode.getDataNode().toString()); 
////							for (Node<Literal, InformationContext> child : fatherNode.getChildren()) {
////								System.out.println(child.getDataNode().toString());
////							}
//						}
//						else {
//							newBody.add(0, node);
//						}
//					}
//					result.setBody(newBody);
//				}
//			}
//		}
//		return result;
//	}
//
//	private void removeNode(Node<Literal, InformationContext> node) {
//	
//		Node<Literal, InformationContext> childNode = null, parentNode = null;
//		int indexChild = -1;
//		
//		childNode = node.getChildren().get(0);
//		parentNode = node.getParent();
//		
//		if ((indexChild = contains(parentNode.getChildren(), childNode.getDataNode())) >= 0) {
//			parentNode.getChildren().get(indexChild).setParent(null);
//			ArrayList<Node<Literal, InformationContext>> childToRemove = new ArrayList<Node<Literal,InformationContext>>();
//			for (Node<Literal, InformationContext> n : childNode.getChildren()) {
//				if (n.getDataNode().getMaxVar() > this.rule.getHead().getDataNode().getMaxVar()) {
//					childToRemove.add(n);
//				}
//			}
//			childNode.getChildren().removeAll(childToRemove);
//			childNode.setParent(parentNode);
//			parentNode.getChildren().set(indexChild, childNode);
//			
//		}
//		else {
//			childNode.setParent(parentNode);
//			parentNode.addChild(childNode);
//		}
//		
//		parentNode.getChildren().remove(node);
//		node.setChildren(null);
//		node.setParent(null);
//	}
//
//	private void prepareForRemoveNode(Node<Literal, InformationContext> node, Node<Literal, InformationContext> selectedNode, List<Node<Literal, InformationContext>> newBody) {
//	
//		for (Node<Literal, InformationContext> child : node.getChildren()) {
//			
//			// removing all children from everywhere (savePoints, and savePaths).
//			if (child.getDataNode().isPositive()) {
//				
//				// deleting the negatedNode associated to this node (child)
//				if (this.negatedNodes.containsKey(child.hashCode())){
//					Node<Literal, InformationContext> negatedChild = this.negatedNodes.get(child.hashCode());
//					
//					if (!negatedChild.equals(selectedNode) && this.contains(newBody, negatedChild.getDataNode()) < 0) {
//						if (this.savePoints.contains(negatedChild)) //deleting the child node from the savedPoints
//							this.savePoints.remove(negatedChild);
//						if (this.savePaths.contains(negatedChild)) //deleting the child node from the savedPaths
//							this.savePaths.remove(negatedChild);
//						
//						negatedChild.setParent(null);
//						this.negatedNodes.remove(negatedChild);
//					}
//				}
//				// deleting each positive child if it is not the selectedNode
//				if (!child.equals(selectedNode)) {
//						if (this.savePoints.contains(child)) //deleting the child node from the savedPoints
//							this.savePoints.remove(child);
//						if (this.savePaths.contains(child)) //deleting the child node from the savedPaths
//							this.savePaths.remove(child);
//						
//						child.setParent(null); // set the new parent because this node is going to be removed
//				}
//			}
//		}
//	}

//	private List<Node<Literal, InformationContext>> selectBestNodes(
//			List<Node<Literal, InformationContext>> nodeList,
//			FoilComparison comparator) {
//		
//		List<Node<Literal, InformationContext>> result = new ArrayList<Node<Literal,InformationContext>>();
//		Node<Literal, InformationContext> selectedNode = null;
//		int firstNodeIndex = 0;
//		
//		/**
//		 * STEP 1: Sort the nodes generated from the best one to the worst one 
//		 * according to several criteria as its Information Gain, if the node is Determinate, 
//		 * if it adds new variables, and so on.
//		 */
//		Collections.sort(nodeList, comparator);
//		
//		/**
//		 * STEP 2: Selecting the best one o the best ones if they are determinate literals. The selected nodes will take part 
//		 * of the rule in production
//		 */
//		
//		selectedNode = nodeList.get(firstNodeIndex);
//		
//		if (selectedNode.getInformationContext().isDeterminate()) {
//			if (!this.continueSearching(selectedNode)) {
//				while (selectedNode.getInformationContext().isDeterminate()) { //we are looking for a no determinate literal.
//					firstNodeIndex++;
////					this.currentGeneration.remove(firstNodeIndex);
//					selectedNode = nodeList.get(firstNodeIndex);
//				}
//				result.add(selectedNode);
//			}
//			else {
//				Node<Literal, InformationContext> previousNode = selectedNode.getParent();
////				List<Node<Literal, InformationContext>> children = previousNode.getChildren(); // watch out! children and this.currentGeneration point the same list of nodes
//				while (selectedNode.getInformationContext().isDeterminate() && this.continueSearching(selectedNode)) { //we include a list of determinate literal
//					result.add(selectedNode);
////					children.remove(selectedNode);
//					selectedNode.setParent(previousNode);
//					previousNode.addChild(selectedNode);
//					previousNode = selectedNode;
//					firstNodeIndex++;
//					selectedNode = nodeList.get(firstNodeIndex);
//				}
//			}
//		}
//		else {
//			result.add(selectedNode);
////			this.currentGeneration.remove(firstNodeIndex);
//		}
//		
//		return result;
//	}
	
	/**
	 * Removes the useless determinate literal, i.e when a determinate literal has not gain and its new variables
	 * are not used in the next literals of the rule
	 * @param newRule body of the rule
	 * @return body with useful nodes. The useless nodes are pruned from the body of the rule
	 */
//	private PrologRule removeUselessDETLit(PrologRule ruleToPrune) {
//		
//		PrologRule result = new PrologRule();
//		result.setHead(ruleToPrune.getHead());
//		boolean end = false, ok = true;
//		List<Node<Literal, InformationContext>> bodyToPrune = ruleToPrune.getBody();
//		List<Node<Literal, InformationContext>> bodyResult = new ArrayList<Node<Literal,InformationContext>>();
//		Set<Term> varsToRemove = new HashSet<Term>();
//		SwiProlog swi = new SwiProlog();
//		
//		PrologRule r = null, r1 = null, r2 = null;
//		Coverage cr1 = null, cr2 = null, origcr = null;
//		
//		Node<Literal, InformationContext> essentialNode = null , nextNode = null;
//		Node<Literal, InformationContext> head = null, node = null;
//		List<Term> essentialNodeArgs = null, nextNodeArgs = null;
//		InformationContext icEssentialNode = null;
//		
//		Set<Variable> existingVars = ruleToPrune.getHeadVariables();
//		Set<Term> newEssentialNodeVars = new HashSet<Term>();
//		
//		origcr = ruleToPrune.getHead().getInformationContext().getOrigCoverage();
//		
//		for (int i = 0; i < bodyToPrune.size() ; i++) {
//			essentialNode = bodyToPrune.get(i);
//			icEssentialNode = essentialNode.getInformationContext();
//			int negCovered = icEssentialNode.getOrigCoverage().getNegativeBindings().size();
//			int previousNegCovered = (i > 0 ? bodyToPrune.get(i-1).getInformationContext().getOrigCoverage().getNegativeBindings().size() : rule.getHead().getInformationContext().getOrigCoverage().getNegativeBindings().size());
//			ok = true;
//			
//			
//			r1 = new PrologRule();
//			r2 = new PrologRule();
//			
//			head = this.cloneNode(result.getHead());
//			
//			r1.setHead(head);
//			r2.setHead(head);
//			
//			for (int j = 0; j < bodyResult.size(); j++) {
//				node = this.cloneNode(bodyResult.get(j));
//				r1.addNode(node);
//				r2.addNode(node);
//			}
//			
//			r2.addNode(this.cloneNode(essentialNode));
//			for (int j = i+1; j < bodyToPrune.size() ; j++) {
//				node = this.cloneNode(bodyToPrune.get(j));
//				r1.addNode(node);
//				r2.addNode(node);
//			}
//			
//			if (icEssentialNode.isDeterminate()) {
//				//last condition means that the determinate literal has a little gain because does not cover some original negative bindings
//				end = false;
//				essentialNodeArgs = essentialNode.getDataNode().getArgs();
//				newEssentialNodeVars.clear();
//				
//				for (int k = 0; k < essentialNodeArgs.size(); k++) {
//					Term t = essentialNodeArgs.get(k);
//					if (!existingVars.contains(t))
//						newEssentialNodeVars.add(t);
//				}
//				
//				for (int j = i+1; j < bodyToPrune.size() && !end; j++) {
//					
//					nextNode = bodyToPrune.get(j);
//					nextNodeArgs = nextNode.getDataNode().getArgs();
//					
//					/**
//					 * Checking if any of the new variables is used in next literals
//					 * if some variable is used later, the determinate literal can not be dropped
//					 * otherwise, the determinate literal is dropped from the rule  
//					 */
//					
//					if (!nextNode.getInformationContext().isDeterminate()) { // if the next node is det check their coverages, not their new vars
//						for (int k = 0; k < nextNodeArgs.size() && !end; k++) {
//							Term t = nextNodeArgs.get(k);
//							if (newEssentialNodeVars.contains(t))
//								end = ok = true;
//						}
//					}
//				
//				}
//				
//				if (!end && negCovered < previousNegCovered) { // if the determinate literal has positive gain, check the coverage
//					
////					previousNode = (i > 0 ? bodyToPrune.get(i-1) : ruleToPrune.getHead());
//					nextNode = ((i+1) <= (bodyToPrune.size() - 1) ? bodyToPrune.get(i+1) : essentialNode);
//
//					if (!nextNode.equals(essentialNode)) {
//						
//						cr1 = swi.origmatch(r1.getHead().getInformationContext().getOrigCoverage(), r1);
//						cr2 = swi.origmatch(r2.getHead().getInformationContext().getOrigCoverage(), r2);
//						
////						Coverage cr1 = swi.match(previousNode.getInformationContext().getCoverage(), nextNode.getDataNode(), kBase);
////						cr1 = BindingsOperations.origmatch(previousNode.getInformationContext().getOrigCoverage(), cr1);
////						Coverage cr2 = nextNode.getInformationContext().getOrigCoverage();
//					
//						if (!nextNode.equals(essentialNode)) {
//							
//							if (cr1.getPositiveBindings().size() == cr2.getPositiveBindings().size() && 
//									cr1.getNegativeBindings().size() <= cr2.getNegativeBindings().size()) {
//								ok = false;
//								varsToRemove.addAll(newEssentialNodeVars);
//							}
//						}
//					}
//				}
//				else if (!end)
//					ok = false;
//			}
//			 
//			if (ok) {
////				for (Term term : varsToRemove) { //deleting new vars not used from the coverage
////					icEssentialNode.getCoverage().removeVar(term);
////				}
//				
//				r = new PrologRule();
//				r.setHead(result.getHead());
//				
//				for (int j = 0; j <= i; j++) {
//					r.addNode(this.cloneNode(bodyToPrune.get(j)));
//				}
//				
//				if (icEssentialNode.isDeterminate()) {
//					origcr = swi.origmatch(origcr, r);
//					essentialNode.getInformationContext().setOrigCoverage(origcr);
//					essentialNode.getInformationContext().setCoverage(swi.match(origcr, r));
//					
//				}
//				
//				bodyResult.add(essentialNode);
//			
//				existingVars.addAll(r.getNewVariables());
//			}
//		}
////		Collections.reverse(bodyResult);
//		result.setBody(bodyResult);
//		return result;
//	}
}