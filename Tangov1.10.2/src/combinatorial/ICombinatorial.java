package combinatorial;

public interface ICombinatorial<T>
{
	public abstract int count ( );

	public abstract boolean hasNext ( );

	public abstract int[] next ( );
}