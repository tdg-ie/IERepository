package strings;

import java.util.HashMap;
import java.util.Map;

public class StringCache {
	
	private static Map<String, String> cache = new HashMap<String, String>();
	
	public static void print() {
		for (String text : cache.values())
			System.out.printf("%s%n", text);
	}
	
	public static String create(String text) {		
		String result;
				
		result = update(text);
		
		return result;
	}
	
	public static String create(String format, Object... params) {
		String result;
		String text;
		
		text = String.format(format, params);
		result = update(text);		
		
		return result;
	}
	
	protected static String update(String text) {
		String result;
		
		result = cache.get(text);
		if (result == null) {
			cache.put(text, text);
			result = text;
		}
		
		return result;		
	}

}
