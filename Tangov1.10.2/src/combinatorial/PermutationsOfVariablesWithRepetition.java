package combinatorial;

import java.util.NoSuchElementException;

public class PermutationsOfVariablesWithRepetition<T> extends PermutationGeneration<T>
{	
	
	private int count;
	
	public PermutationsOfVariablesWithRepetition (int n, int r)
	{
		this.n = n;
		this.r = r;
		this.count = this.count();
		
		this.permutation = new int[n];
		for (int i = 0; i < n; i++) {
			this.permutation[i] = 0;
		}
	}

	public boolean hasNext ( )
	{
		return this.count > 0;
	}

	public int[] next ( )
	{
		
		if (permutation.length != 0)
		{			
			if(!hasNext())
				throw new NoSuchElementException();
			boolean exit = false, ok = false;
			while (!ok) {
				exit = false;
				for (int i = permutation.length - 1; i >= 0 && !exit; i--) {
					if (++permutation[i] >= (r== 1 ? r+1 : r))
						permutation[i] = 0;
					else {
						exit = true;
						if (numberOfNewVarsIndex() == this.r)
							ok = true;
					}
					
				}
			}
		}
		
		this.count--;
		return permutation;
	}
	

	private int numberOfNewVarsIndex() {
		int result = 0;
		for (int i = 0; i < permutation.length; i++) {
			if (permutation[i] == 0)
				result++;
		}
		return result;
	}

	public int count ( )
	{
		return MathUtilities.numCombinationsR(n, r);
	}	
}