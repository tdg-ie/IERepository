package combinatorial;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class CombinationsWithRepetition<T> extends CombinationGeneration<T>
{
	public CombinationsWithRepetition (int r, int n )
	{
		this.r = r;
		this.n = n;
		combination = new int[r];
		Arrays.fill(combination, 0);
		combination[combination.length - 1]--;
	}
	
	public int count()
    {
        return MathUtilities.numCombinationsR((n + r) - 1, r);
    }

	public boolean hasNext ( )
	{
		for (int i = combination.length - 1; i >= 0; i--)
			if (combination[i] + 1 < n)
				return true;

		return false;
	}

	public int[] next ( )
	{
		for (int i = combination.length - 1; i >= 0; i--)
			if (combination[i] + 1 < n)
			{
				combination[i]++;
				for (int j = i + 1; j < combination.length; j++)
					combination[j] = combination[j - 1];

				return combination;
			}

		throw new NoSuchElementException();
	}	
}