package combinatorial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import data.Type;
import data.Variable;

public class PermutationsOfVariablesWithoutRepetition<T> extends PermutationGeneration<T>
{	
	private List<Variable> vars= null;
	private int varsSize = 0;
	private List<Variable> newVarsList= null;
	private Map<String, List<Variable>> varSet = new HashMap<String, List<Variable>>();
	private List<Type> types = null;
	private boolean[] isNewVar = null;
	private int j;
	
	public PermutationsOfVariablesWithoutRepetition (List<Variable> vars, List<Variable> newVars, List<Type> types)
	{
		this.vars = vars;
		this.newVarsList = newVars;
		this.types = types;
		
		varSet = new HashMap<String, List<Variable>>();
		for (Variable v : this.vars) {
			String typeName = v.getType().getTypeName();
			if (varSet.containsKey(typeName))
				varSet.get(typeName).add(v);
			else {
				List<Variable> varList = new ArrayList<Variable>();
				varList.add(v);
				varSet.put(typeName, varList);
			}
		}
		
		this.r = types.size();
		this.n = this.count();
		this.varsSize = this.vars.size();
		
		permutation = new int[r];
		isNewVar = new boolean[r];
		isNewVar[0] = false;
		for (int i = 1; i < isNewVar.length; i++) {
			isNewVar[i] = true;
		}
		
		j = permutation.length-1;
		
		if (r == 1)
			permutation[0] = -1;
		else {
			for (int i = permutation.length-1; i >= 0; i--) {
				permutation[i] = this.varsSize + (i-1);
			}
			permutation[0] = searchForIndex(0,0, true); //search for the first existing compatible variable
			permutation[permutation.length-1] = this.varsSize + this.newVarsList.size();
		}
	}

	public boolean hasNext ( )
	{
		return this.n > 0;
	}

	public int[] next ( )
	{
		boolean ok = false;
		int value = -1;
		
		if (permutation.length != 0)
		{			
			if(!hasNext())
				throw new NoSuchElementException();
			
			while (!ok) {
				
				if (j != 0) {
					if (this.isNewVar[j]) {
						this.permutation[j]--;
						if (this.permutation[j] < this.varsSize) {
							if ((value = this.searchForIndex(0, j, true)) < this.varsSize) {
								permutation[j] = value;
								this.isNewVar[j] = false;
							}
							else {
								j--;
								reset(j);
							}
						}
					}
					else {
						if ((value = this.searchForIndex(permutation[j]+1, j, true)) != this.varsSize)
							permutation[j] = value;
						else {
							j--;
							reset(j);
						}
					}
						
				}
				else {
					this.permutation[j]++;
				}
								
				ok = true;
				for (int i = 0; i < permutation.length && ok; i++) {
					if (permutation[i] < varsSize) {
						if (!vars.get(permutation[i]).getType().equals(this.types.get(i)))
							ok = false;
					}
				}
				ok &= !checkRepetitiousNewVars();
			}
		}		
		rewritePermutation();
		this.n--;
		return permutation;
	}
	
	private void reset(int j2) {
		boolean end = false;
		int v = -1;
		if (j2 <= 0){
			this.permutation[j2]++;
		}
		else {
			while ((j2 >= 0) && (v = this.searchForIndex(this.isNewVar[j2] ? 0 : this.permutation[j2]+1, j2, true)) == this.varsSize) 
				j2--;
			if (j2>=0) {
				if (j2 == 0 && permutation[j2] >= varsSize) {
					for (int i = 1; i < permutation.length; i++) {
						permutation[i] = varsSize + 1;
						this.isNewVar[i] = true;
					}
					end = true;
				}
				else {
					this.permutation[j2] = v;
					this.isNewVar[j2] = false;
				}
			}
			else if (this.r > 1) {//means first index has to be a new variable
				j2 = 0;
				this.isNewVar[j2] = true;
				this.permutation[j2] = this.varsSize;
				j2++;
				this.isNewVar[j2] = false;
				this.permutation[j2] = this.searchForIndex(0, j2, true);
			}
		}
		if (!end) {
			int newVars = 0;
			for (int i = 0; i < j2+1; i++){
				if (this.isNewVar[i])
					newVars++;
			}
			for (int i = j2+1; i < this.r; i++){
				this.permutation[i] = this.varsSize + (i - j2 - 1) + newVars;
				this.isNewVar[i] = true;
			}
		}
		j = this.r-1;
	}

	private void rewritePermutation() {
		int newVarsNumber = 0;
		for (int i = 0; i < permutation.length; i++) {
			if (permutation[i] >= varsSize) {
				if (permutation[i] > (varsSize + newVarsNumber))
					permutation[i] = varsSize + newVarsNumber;
				newVarsNumber++;
			}
		}
	}
	

	private boolean checkRepetitiousNewVars() {
		boolean result = false;
		int index1 = 0, index2 = 0;
		for(int i = 0; i < this.permutation.length && !result; i++) {
			index1 = permutation[i];
//			if (index1 >= this.varsSize)
				for(int j = i+1; j < this.permutation.length && !result; j++) {
					index2 = permutation[j];
					if (index1 == index2 && (this.isNewVar[i] || this.isNewVar[j]))
						result = true;
				}
		}
		return result;
	}

	private int searchForIndex(int argIndex, int typeIndex, boolean fordward) {
		int result = 0;
		boolean end = false;
		int i = 0;
		Type t = this.types.get(typeIndex);
		
		if (fordward) {
			for (i = argIndex; i < varsSize && !end; i++) {
				if (this.vars.get(i).getType().equals(t)) {
					result = i;
					end = true;
				}
			}
			if (!end && ((argIndex + 1) >= varsSize)) {
				result = varsSize; 
				end = true;
			}
		}
		else
			for (i = argIndex; i >= 0 && !end; i--) {
				if (this.vars.get(i).getType().equals(t)) {
					result = i;
					end = true;
				}
			}
			
		if (!end) {
			result = varsSize + this.newVarsList.size() - (permutation.length - typeIndex);
		}
		
		return result;
	}

	@Override
	public int count ( )
	{
		int result = 0, total = 1, number = 0;
		PermutationGeneration<Integer> combGenerator = null;
		String typeName = null;
		int[] newVarsIndex = null;
		
		// all terms are existing vars
		total = 1;
		for (int i = 0; i < this.types.size() && total > 0; i++) {
			typeName = this.types.get(i).getTypeName();
			number = 0;
			if (varSet.containsKey(typeName))
				number = varSet.get(typeName).size();
			total *= number;
		}
		result = total;
		
		// generating permutations with "i" new variables
		for (int i = 1; i < this.types.size(); i++) {
			combGenerator = new PermutationsOfVariablesWithRepetition<Integer>(this.r, i);
			while (combGenerator.hasNext()) {
				newVarsIndex = combGenerator.next();
				total = 1;
				for (int j = 0; j < newVarsIndex.length && total > 0; j++) {
					number = 0;
					typeName = this.types.get(j).getTypeName();
					if (newVarsIndex[j] != 0) {//means no new vars
						if (varSet.containsKey(typeName))
							number = varSet.get(typeName).size();
						total *= number;
					}
						
				}
				result += total;
			}
//			result += total;
		}
		return result;
		
//		int result = 0, total = 1, number = 0;
//		boolean ok = true;
//		Map<String, Integer> addedType = new HashMap<String, Integer>();
//		PermutationGeneration<Integer> combGenerator = null;
//		int[] newVarsIndex = null;
//		
//		// all terms are existing vars
//		String typeName = this.types.get(0).getTypeName();
//		total = 1;
//		for (int i = 0; i < this.types.size() && ok; i++) {
//			typeName = this.types.get(i).getTypeName();
//			if (varSet.containsKey(typeName)) {
//				number = varSet.get(typeName).size(); // +1 new var which can be added
//				if (addedType.containsKey(typeName)) {
//					number -= addedType.get(typeName);
//					addedType.put(typeName, addedType.get(typeName)+1);
//				}
//				else 
//					addedType.put(typeName, 1);
//				
//				total *= number;
//			}
//			else 
//				ok = false;
//		}
//		if (ok)
//			result += total;
//		
//		
//		// generating permutations with "i" new variables
//		for (int i = 1; i < this.types.size(); i++) {
//			combGenerator = new PermutationsOfVariablesWithRepetition<Integer>(this.r, i);
//			while (combGenerator.hasNext()) {
//				ok = true;
//				newVarsIndex = combGenerator.next();
//				addedType = new HashMap<String, Integer>();
//				total = 1;
//				for (int j = 0; j < newVarsIndex.length && ok; j++) {
//					typeName = this.types.get(j).getTypeName();
//					if (newVarsIndex[j] != 0) {//means no new vars
//						if (varSet.containsKey(typeName)) {
//							number = varSet.get(typeName).size();
//							if (addedType.containsKey(typeName)) {
//								number -= addedType.get(typeName);
//								addedType.put(typeName, addedType.get(typeName)+1);
//							}
//							else
//								addedType.put(typeName, 1);
//						}
//						else
//							ok = false;
//						
//						total *= number;
//					}
//				}
//				if (ok)
//					result += total;
//			}
//		}
//		return result;
	}
	
//	public int count ( )
//	{
//		int result = 0, total = 1, number = 0;
//		boolean ok = true;
//		PermutationGeneration<Integer> combGenerator = null;
//		int[] newVarsIndex = null;
//		
//		// all terms are existing vars
//		String typeName = "";
//		total = 1;
//		for (int i = 0; i < this.types.size() && ok; i++) {
//			typeName = this.types.get(i).getTypeName();
//			if (varSet.containsKey(typeName)) {
//				number = varSet.get(typeName).size(); // +1 new var which can be added
//				total *= number;
//			}
//			else 
//				ok = false;
//		}
//		if (ok)
//			result += total;
//		
//		
//		// generating permutations with "i" new variables
//		for (int i = 1; i < this.types.size(); i++) {
//			combGenerator = new PermutationsOfVariablesWithRepetition<Integer>(this.r, i);
//			while (combGenerator.hasNext()) {
//				ok = true;
//				newVarsIndex = combGenerator.next();
//				total = 1;
//				for (int j = 0; j < newVarsIndex.length && ok; j++) {
//					typeName = this.types.get(j).getTypeName();
//					if (newVarsIndex[j] != 0) {//means no new vars
//						if (varSet.containsKey(typeName)) {
//							number = varSet.get(typeName).size();
//						}
//						else
//							ok = false;
//						
//						total *= number;
//					}
//				}
//				if (ok)
//					result += total;
//			}
//		}
//		return result;
//	}
}