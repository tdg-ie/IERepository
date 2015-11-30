package coverageComputation;

import java.util.ArrayList;
import java.util.List;

import branch_and_bound.data.InformationContext;
import branch_and_bound.data.Node;
import data.Literal;
import utilities.Utilities;

public class Bits {
	
	//bits consumed by a specified Predicate
	public static double ldiLiteral(int nRelations, int nTryArgs, boolean negatedLitAllowed){
		return Utilities.round((negatedLitAllowed ? 1:0) + Utilities.log2(nRelations) + Utilities.log2(nTryArgs +1E-3));
	}
	
	public static double ldiRule(List<Node<Literal,InformationContext>> nodes){
		double result = 0;
		int NLiterals = nodes.size(), detLitNumber = 0;
		List<Node<Literal,InformationContext>> nodesAux = new ArrayList<Node<Literal,InformationContext>>();
		Node<Literal, InformationContext> n = null;
		InformationContext icNode = null;
		for (int i = 0; i < NLiterals; i++) {
			n = nodes.get(i);
			nodesAux.add(n);
			icNode = n.getInformationContext();
			if (!icNode.isDeterminate()) {
				// ExtraBits = LitBits - log2(nlit - ndetLit +1E-3)
				//ruleBits = max(0, ExtraBits)
				result += icNode.getBits() - Utilities.log2(nodesAux.size() - detLitNumber + 1E-3);
			}
			else 
				detLitNumber++;
		}
		return Utilities.round(result);
	}
}