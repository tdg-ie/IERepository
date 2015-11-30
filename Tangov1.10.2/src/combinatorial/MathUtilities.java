package combinatorial;

public final class MathUtilities
{

	private MathUtilities ( )
	{
	}

	public static long factorial (int n)
	{
		long r;
		for (r = 1L; n > 0; r *= n--)
			;
		return r;
	}

	public static int numCombinationsR (int n, int r)
	{
		return (int) (factorial(n) / (factorial(r) * factorial(n - r)));
	}

	public static int numPermutationsR (int n, int r)
	{
		return (int) (factorial(n) / factorial(n - r));
	}
}