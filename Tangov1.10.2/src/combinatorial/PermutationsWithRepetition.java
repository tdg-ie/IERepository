package combinatorial;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class PermutationsWithRepetition<T> extends PermutationGeneration<T>
{	
	public PermutationsWithRepetition (int r, int n )
	{
		this.r = r;
		this.n = n;
		permutation = new int[r];
		Arrays.fill(permutation, 0);
		if (permutation.length > 0)
			permutation[permutation.length - 1]--;
	}

	public boolean hasNext ( )
	{
		for (int i = permutation.length - 1; i >= 0; i--)
			if (permutation[i] + 1 < n)
				return true;

		return false;
	}

	public int[] next ( )
	{
		if (permutation.length != 0)
		{			
			if(!hasNext())
				throw new NoSuchElementException();
			boolean exit = false;
			for (int i = permutation.length - 1; i >= 0 && !exit; i--)
				if (++permutation[i] >= n)
					permutation[i] = 0;
				else
					exit = true;
		}

		return permutation;
	}

	public int count ( )
	{
		return (int) Math.pow(n, r);
	}	
}