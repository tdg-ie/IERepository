package combinatorial;

import java.util.NoSuchElementException;

public class PermutationsWithoutRepetition<T> extends PermutationGeneration<T>
{
	private boolean first;

	public PermutationsWithoutRepetition (int r, int n )
	{
		if (r > n)
			throw new IllegalArgumentException(
					"only r <= n permutations without repetition exist");
		this.r = r;
		this.n = n;
		permutation = new int[r];
		for (int i = 0; i < permutation.length; i++)
			permutation[i] = i;

		if (r < n)
		{
			throw new UnsupportedOperationException("r < n not yet implemented");
		}
		else
		{
			first = true;
			return;
		}
	}

	private boolean isVeryFirst ( )
	{
//		if (!first)
//			;
		return first;
	}

	public int count ( )
	{
		return MathUtilities.numPermutationsR(n, r);
	}

	public boolean hasNext ( )
	{
		return isVeryFirst() || permute((int[]) (int[]) permutation.clone());
	}

	public int[] next ( )
	{
		if (isVeryFirst())
		{
			first = false;
			return permutation;
		}
		int old[] = (int[]) (int[]) permutation.clone();
		if (permute(permutation))
			return permutation;
		System.arraycopy(old, 0, permutation, 0, old.length);
		if (r == n)
			throw new NoSuchElementException("no more elements for r=n in "
					+ this);
		else
			throw new UnsupportedOperationException("r < n not yet implemented");
	}

	public static boolean permute (int permutation[])
	{
		int first = 0;
		int last = permutation.length;
		if (first == last)
			return false;
		int i = first;
		if (++i == last)
			return false;
		i = last;
		i--;
		do
		{
			int ii = i--;
			if (permutation[i] < permutation[ii])
			{
				int j;
				for (j = last; permutation[i] >= permutation[--j];)
					;
				int T = permutation[i];
				permutation[i] = permutation[j];
				permutation[j] = T;
				reverse(permutation, ii, last);
				return true;
			}
		}
		while (i != first);
		reverse(permutation, first, last);
		return false;
	}

	private static void reverse (int permutation[], int first, int last)
	{
		while (first < last)
		{
			int T = permutation[first];
			permutation[first++] = permutation[--last];
			permutation[last] = T;
		}
	}	
}