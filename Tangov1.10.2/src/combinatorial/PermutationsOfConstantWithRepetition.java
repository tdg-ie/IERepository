package combinatorial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import data.Type;

public class PermutationsOfConstantWithRepetition<T> extends PermutationGeneration<T>
{	
	private List<Type> types= null;
	private Map<String, Integer> constantSetSize = null;
	TreeSet<Integer> numbers = new TreeSet<Integer>(); 
	private List<int[]> intervals = null;
	
	public PermutationsOfConstantWithRepetition (List<Type> types, Map<String, Integer> constantSetSize)
	{
		this.types = types;
		this.constantSetSize = constantSetSize;
		
		this.r = types.size();
		this.n = this.count();
		
		permutation = new int[r];
		Arrays.fill(permutation, 0);
	}

	public boolean hasNext ( )
	{
		return !this.getIntervals().isEmpty();
	}

	public int[] next ( )
	{
		int j = r-1;
		if (permutation.length != 0)
		{			
			if(!hasNext())
				throw new NoSuchElementException();
			
			this.intervals = this.getIntervals();
			
			if (!this.intervals.isEmpty()) {
				int intervalIndex = (int) ((Math.random() * this.intervals.size()));
				int[] indexTable = this.intervals.get(intervalIndex);
				int min = indexTable[0];
				int max = indexTable[1];
				int number = (int) (Math.random() * (max - min)) + min;
				this.numbers.add(number);
				
				int dividend = number;
				for(int i = types.size()-1; i >= 0; i--) {
					int divisor = this.constantSetSize.get(types.get(i).getTypeName());
					int remainder = this.permutation[j] = dividend % divisor;
					int divisionratio = dividend / divisor;
					this.permutation[j] = remainder;
					j--;
					dividend = divisionratio;	
				}
			}
		}
		return permutation;
	}

	private List<int[]> getIntervals() {
		List<int[]> result = new ArrayList<int[]>();
		int[] minmax = null;
		int firstIndex = 0;
		int lastIndex = this.n;
		
		if (!this.numbers.isEmpty()) {
			for (int i : numbers) {
				if (i == firstIndex) {
					firstIndex = i+1;
				}
				lastIndex = i; //else
				if (lastIndex - firstIndex > 0) {
					minmax = new int[2];
					minmax[0] = firstIndex;
					minmax[1] = lastIndex;
					result.add(minmax);
					firstIndex = lastIndex+1;
				}
			}
			if (firstIndex < this.n) {
				minmax = new int[2];
				minmax[0] = firstIndex;
				minmax[1] = this.n;
				result.add(minmax);
			}
		}
		else {
			minmax = new int[2];
			minmax[0] = firstIndex;
			minmax[1] = lastIndex;
			result.add(minmax);
		}
		
		return result;
	}

	public int count ( )
	{
		int result = 1;
		for (Type t : types) {
			if (constantSetSize.containsKey(t.getTypeName()))
				result *= constantSetSize.get(t.getTypeName());
			else 
				result = 0;
		}
		return result;
	}	
}