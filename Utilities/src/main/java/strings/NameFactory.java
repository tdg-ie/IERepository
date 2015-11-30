package strings;


public class NameFactory {
	
	private static int counter = 0;
	
	public static String freshVariable(String baseName) {
		String result;
		
		counter++;		
		result = String.format("%s%04d", baseName, counter);
		
		return result;
	}
		
}
