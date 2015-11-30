package strings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.TimeZone;

public class Formatter {

	public static String toSize(long bytes) {
		String result;
		int exponent;
		char prefix;

		if (bytes < 1024)
			result = String.format("%d B", bytes);
		else {
			exponent = (int) (Math.log(bytes) / Math.log(1024));
			prefix = "KMGTPE".charAt(exponent - 1);
			result = String.format("%.2f %ciB",
					bytes / Math.pow(1024, exponent), prefix);
		}

		return result;
	}

	public static String toTime(long milliseconds) {
		String result;
		long offset, time;

		offset = TimeZone.getDefault().getRawOffset();
		time = milliseconds - offset;

		result = String.format("%tT.%1$tL", time);

		return result;
	}

	public static String toEncodedValue(String value) {
		String result;
		String actualValue, encoding;		

		if (value == null)
			result = "null";
		else {
			try {
				if (value.equals("''") || value.equals(""))
					actualValue = "<![CDATA[]]>";
				else if (value.length() >= 3 && value.startsWith("'") && value.endsWith("'"))
					actualValue = value.substring(1, value.length() - 1);
				else
					actualValue = value;
				encoding = Charset.defaultCharset().name();
				result = URLEncoder.encode(actualValue, encoding);				
			} catch (UnsupportedEncodingException oops) {
				// It's impossible that the default encoding does not exist!								
				throw new RuntimeException(oops);				
			}
		}

		return result;
	}

}
