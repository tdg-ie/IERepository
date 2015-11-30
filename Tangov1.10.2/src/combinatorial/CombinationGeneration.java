package combinatorial;

import java.util.ArrayList;
import java.util.List;

public abstract class CombinationGeneration<T> implements ICombinatorial<T>
{
	protected int r;
	protected int n;
	protected int combination[];

	public abstract boolean hasNext ( );

	public abstract int[] next ( );

	public abstract int count ( );
	
	public List<List<T>> getAllCombinations (List<T> name)
	{
		List<List<T>> result;

		result = new ArrayList<List<T>>();

		while (hasNext())
		{
			int[] indices = next();

			List<T> listAux = new ArrayList<T>();

			for (int i = 0; i < indices.length; i++)
			{
				listAux.add(name.get(indices[i]));
			}
			if (!listAux.isEmpty())
				result.add(listAux);
		}

		return result;
	}
}