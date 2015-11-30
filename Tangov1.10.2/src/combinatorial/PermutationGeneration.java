package combinatorial;

import java.util.ArrayList;
import java.util.List;

public abstract class PermutationGeneration<T> implements ICombinatorial<T>
{
	protected int r;
	protected int n;
	protected int permutation[];

	public abstract boolean hasNext ( );

	public abstract int[] next ( );

	public abstract int count ( );

	public List<List<T>> getAllPermutations (List<T> elements)
	{
		List<List<T>> result;
		
		result = new ArrayList<List<T>>();
		
		while(hasNext())
		{		
			int []indices = next();
			
			List<T> listAux = new ArrayList<T>();

			for (int i = 0; i < indices.length; i++)
			{
				listAux.add(elements.get(indices[i]));
			}
			if (!listAux.isEmpty())
				result.add(listAux);
		}		
		
		return result;
	}
}