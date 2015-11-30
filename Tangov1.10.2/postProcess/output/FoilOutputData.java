package output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import knowledgeBase.BindingsOperations;


import utilities.ThreadMonitor;

import data.Literal;
import data.Term;
import data.Variable;
import dataCoverage.Bindings;
import dataCoverage.Coverage;
import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import branch_and_bound.engine.BranchOperations;

public class FoilOutputData {

	private Coverage ruleSetCoverage = null;
	private int evaluatedNodesNumber = 0;
	private int prunedNodesNumber = 0;
	private int backTrackingNumber = 0;
	private int determinateNumber = 0;
	private int ruleNodesNumberBeforePrune = 0;
	private int rulePrunedNodesNumber = 0;
	private int totPos = 0;
	private int totNeg = 0;
	private double complexityRule = 0;
	private Set<String> predicateNameSet = null;
	private Set<String> variableSet = null;
	private Set<String> constantSet = null;
	private List<List<Node<Literal,InformationContext>>> branches;
	
	public FoilOutputData () {
		this.determinateNumber = 0;
		this.evaluatedNodesNumber = 0;
		this.ruleNodesNumberBeforePrune = 0;
		this.backTrackingNumber = 0;
		this.determinateNumber = 0;
		this.rulePrunedNodesNumber = 0;
		this.complexityRule = 0;
		this.totPos = 0;
		this.totNeg = 0;
		
		this.ruleSetCoverage = new Coverage();
		this.predicateNameSet = new HashSet<String>();
		this.variableSet = new HashSet<String>();
		this.constantSet = new HashSet<String>();
		this.branches = new ArrayList<List<Node<Literal,InformationContext>>>();
		
	}
	
	public int getEvaluatedNodesNumber() {
		return evaluatedNodesNumber;
	}

	public void setEvaluatedNodesNumber(int nodesNumber) {
		this.evaluatedNodesNumber = nodesNumber;
	}
	
	public void incEvaluatedNodesNumber() {
		this.evaluatedNodesNumber++;
	}

	public void setInstNumber(int totPos, int totNeg ) {
		this.totPos = totPos;
		this.totNeg = totNeg;
	}

	public void setRuleSet(List<Node<Literal,InformationContext>> finalNodes) {
		Literal l = null;
		List<Term> args = null;
		this.complexityRule = 0;
		
		for (Node<Literal,InformationContext> node : finalNodes) {
			this.branches.add(node.getFullPath());
		}
		for (List<Node<Literal,InformationContext>> branch : branches) {
			
			Node<Literal, InformationContext> lastNode = branch.get(branch.size()-1);
			InformationContext ic = lastNode.getInformationContext();
			this.complexityRule += ic.getNewRuleBits();
			
			Coverage origCoverage = ic.getOrigCoverage();
			
			for (Bindings b : origCoverage.getPositiveBindings()) {
				if (!BindingsOperations.belongs(b,this.ruleSetCoverage.getPositiveBindings()))
					this.ruleSetCoverage.addPositiveBinding(b);
			}
			
			for (Bindings b : origCoverage.getNegativeBindings()) {
				if (!BindingsOperations.belongs(b,this.ruleSetCoverage.getNegativeBindings()))
					this.ruleSetCoverage.addNegativeBinding(b);
			}
			
			this.determinateNumber += BranchOperations.getDetLitNumber(branch);
			
			if (branch.size() > 1) {
				for (Node<Literal, InformationContext> node: branch.subList(1, branch.size())) {
					l = node.getDataNode();
					this.predicateNameSet.add(l.getPredicate().getName());
		//				if (node.getInformationContext().isDeterminate())
		//					this.determinateNumber++;
					
					args = l.getArgs();
					for (Term t : args) {
						if (t instanceof Variable)
							variableSet.add(t.getName());
						else 
							constantSet.add(t.getValue());
					}
				}
			}
			
			//adding vars and constants of the head
			l = branch.get(0).getDataNode();
			args = l.getArgs();
			for (Term t : args) {
				if (t instanceof Variable)
					variableSet.add(t.getName());
				else 
					constantSet.add(t.getValue());
			}
		}
	}

	public List<List<Node<Literal,InformationContext>>> getBranches() {
		return this.branches;
	}
	
	public Coverage getRuleSetCoverage() {
		return this.ruleSetCoverage;
	}
	
	public double getComplexityRuleSet() {
		return this.complexityRule;
	}
	
	public int getRuleNumber() {
		return this.branches.size();
	}
	
	public int getDifferentPredicateNumber() {
		return this.predicateNameSet.size();
	}
	
	public int getVariableNumber() {
		return this.variableSet.size();
	}
	
	public int getConstantNumber() {
		return this.constantSet.size();
	}

	public int getRuleNodesNumberBeforePrune() {
		return ruleNodesNumberBeforePrune;
	}

	public void incRuleNodesNumberBeforePrune(int ruleNodesNumberBeforePrune) {
		this.ruleNodesNumberBeforePrune += ruleNodesNumberBeforePrune;
	}
	
	public void setRuleNodesNumberBeforePrune(int ruleNodesNumberBeforePrune) {
		this.ruleNodesNumberBeforePrune = ruleNodesNumberBeforePrune;
	}

	public int getBackTrackingNumber() {
		return backTrackingNumber;
	}

	public void incBackTrackingNumber() {
		this.backTrackingNumber++;
	}
	
	public int getDeterminateNumber() {
		return this.determinateNumber;
	}

	public int getRulePrunedNodesNumber() {
		return rulePrunedNodesNumber;
	}

	public void setRulePrunedNodesNumber(int rulePrunedNodesNumber) {
		this.rulePrunedNodesNumber += rulePrunedNodesNumber;
	}

	public int getPrunedNodesNumber() {
		return prunedNodesNumber;
	}

	public void setPrunedNodesNumber(int prunedNodesNumber) {
		this.prunedNodesNumber = prunedNodesNumber;
	}
	
	public void incPruneNodesNumber() {
		this.prunedNodesNumber++;
	}
	
	public String print() {
		
		StringBuffer result = new StringBuffer();
		int posSize = 0;
		int negSize = 0;
		int size;
//		MDL mdl = new MDL();
		double bits = 0;
		String s = "\n\nSet of rules found: \n"; 
		
		result.append(s);
//		System.out.print(s);
		for (List<Node<Literal,InformationContext>> branch : this.branches) {
			size = branch.size();
			Node<Literal, InformationContext> head = branch.get(0);
			List<Node<Literal, InformationContext>> body = (size > 1)? branch.subList(1, size) : new ArrayList<Node<Literal,InformationContext>>();
			result.append(BranchOperations.pathToString(head, body) + "\n");
			
			if (size > 1)
				bits = body.get(body.size()-1).getInformationContext().getNewRuleBits();
			else
				bits = head.getInformationContext().getNewRuleBits();
			
			s = "Bits used " + bits + "\n";
//			s = "Bits used " + mdl.ldiRule(r) + "\n";
//			System.out.print(s);
			result.append(s);
		}
		
		posSize = this.ruleSetCoverage.getPositiveBindings().size();
		negSize = this.ruleSetCoverage.getNegativeBindings().size();
		s = "\nCoverage of the rules set [ pos: " + posSize + "/" + totPos + " neg: " + negSize + "/" + totNeg + "]\n";
//		System.out.print(s);
		result.append(s);
		result.append(this.ruleSetCoverage.toString() + "\n\n");
		
		
		s = "Time spent: ";
		System.out.print(s);
		result.append(s);
		long timeSpent = ThreadMonitor.getCpuTime();
//		long timeSpent = ThreadMonitor.getUserTime();
		result.append(timeSpent + "\t");
		System.out.println(timeSpent);
		result.append("nanoseconds \n");
		
//		s = "Time spent: ";
//		System.out.print(s);
//		result.append(s);
//		Time timeSpent = this.endTime.minus(this.startTime);
//		result.append(timeSpent.print() + "\t");
//		result.append("milliseconds " + Time.minus(this.startTime, this.endTime) + "\n");
		
		s = "Number of nodes that have been evaluated: " + this.evaluatedNodesNumber + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of nodes that have been pruned: " + this.prunedNodesNumber + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of backtrackings made: " + this.backTrackingNumber + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of determinate literals: " + this.determinateNumber + "\n";
//		System.out.println();
		result.append(s);
		
		s = "Number of nodes before the ruleset has been pruned: " + this.ruleNodesNumberBeforePrune + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of nodes pruned during the prune of the ruleset:  " + this.rulePrunedNodesNumber + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Complexity of the ruleset: " + this.complexityRule + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of diferent predicates: " + predicateNameSet.size() + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of variables used: " + this.variableSet.size() + "\n";
//		System.out.print(s);
		result.append(s);
		
		s = "Number of constant used: " + this.constantSet.size() + "\n";
//		System.out.print(s);
		result.append(s);
		
		return result.toString();
	}
}
