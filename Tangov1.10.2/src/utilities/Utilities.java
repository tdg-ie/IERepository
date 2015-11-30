package utilities;

public class Utilities {
	
	private static final double log2e = 1.44269;
	private static final double log2sqrt2Pi = 1.32575;
	private static final double ln2 = Math.log(2);
	private static double[] logFact = null;
	private static int nDec = 6;
	
	static{
		
		logFact = new double[1000];
		logFact[0] = 0.0;
		for (int i = 1; i < 1000; i++) {
			logFact[i] = logFact[i-1] + log2(i);
		}
	}

	public static double log2(double d){
		double result = -1;
		result = Math.log(d)/ln2;
		return result;
	}
	
	public static long factorial (int n)
	{
		long r;
		for (r = 1L; n > 0; r *= n--)
			;
		return r;
	}
	
	public static double comb(int n, int m){
		return (factorial(n) /(double)(factorial(m)*factorial(n-m)));
	}
	
	public static double logComb(int n, int r) {
		return (log2Fact(n) - log2Fact(r) - log2Fact((n)-(r)));
	}

	
	/*  Find log (base 2) factorials using tabulated values and Stirling's
	approximation (adjusted)  */
	
	private static double  log2Fact(int n) {
		return ( n < 1000 ? logFact[n] :
			(n+0.5) * log2(n) - n * log2e + log2sqrt2Pi - (n*0.7623)/820000 );
	}
	
	public static double max(double a, double b) {
		return ((a > b) ? a:b);
	}
	
	public static double round(double number) {
		return Math.round(number*Math.pow(10,nDec))/Math.pow(10,nDec);
	}
	
	public static double encode(int p,int allTuples) {
		double result = ((allTuples > 0) ? (1.1*(Utilities.log2(allTuples) + Utilities.logComb(allTuples, p))) : 0.0);
		return Utilities.round(result);
	}
	
	public static int argPair(int i, int j) {
		return (((i)*(i-1))/2 + j);
	}
}