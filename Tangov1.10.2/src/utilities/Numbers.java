package utilities;

public class Numbers {
	
	public static boolean isNumber(String n) {
		boolean result = false;
//		if (n.endsWith("_integer") || n.endsWith("_long") 
//				|| n.endsWith("_float") || n.endsWith("_double"))
//			result = true;
//		else 
			
		if (Numbers.isInteger(n) || Numbers.isDouble(n) 
				|| Numbers.isFloat(n) || Numbers.isLong(n)) 
			result = true;
		return result;
	}
	
	public static boolean isNumeric(String s) {
		boolean result = true;
		if (s == null || s.isEmpty())
			result = false;
		else {
			int i = 0;
			if (s.charAt(i) == '-') {
				if (s.length() > 1)
					i++;
				else 
					result = false;
			}
			for (;i < s.length() && result; i++) {
				if (!Character.isDigit(s.charAt(i)))
					result = false;
			}
		}
		return result;
	}
	
	public static String numberType(String s) {
		String result = "";
		
		if (isInteger(s))
			result = "_integer";
		else if (isLong(s))
			result = "_long";
		else if (isFloat(s))
			result = "_float";
		else if (isDouble(s))
			result = "_double";
 
		return result;
	}
	
	private static boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	private static boolean isLong(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	private static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	private static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
}