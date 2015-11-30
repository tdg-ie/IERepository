package combinatorial;

import java.util.NoSuchElementException;

public class CombinationsWithoutRepetition<T> extends CombinationGeneration<T>
{

	public CombinationsWithoutRepetition (int r, int n )
	{
		if (r > n)
			throw new IllegalArgumentException(
					"only r <= n combinations without repetition exist, cannot pick more than present");
		this.r = r;
		this.n = n;
		combination = new int[r];
		for (int i = 0; i < combination.length; i++)
			combination[i] = i;

		combination[combination.length - 1]--;
	}

	public int count ( )
	{
		return MathUtilities.numCombinationsR(n, r);
	}

	public boolean hasNext ( )
	{
		for (int i = combination.length - 1; i >= 0; i--)
			if (combination[i] < (i + n) - combination.length)
				return true;

		return false;
	}

	public int[] next ( )
	{
		for (int i = combination.length - 1; i >= 0; i--)
			if (combination[i] < (i + n) - combination.length)
			{
				combination[i]++;
				for (int j = i + 1; j < combination.length; j++)
					combination[j] = combination[j - 1] + 1;

				return combination;
			}

		throw new NoSuchElementException();
	}
}