package configuration;

public class Constants {
	
	public final static String ATTRIBUTE_SEPARATOR = "_";
	public final static String SET_SEPARATOR = ";";
	
	public final static String TRAINING = "training";
	public final static String NODE = "node";
	public final static String CLAZZ = "clazz";
	public final static String NODE_CLAZZ = NODE + ATTRIBUTE_SEPARATOR + CLAZZ;
	public final static String NULL = "null";
	
	public final static String MISSING_VALUE = "?";
	
	public final static String ATTRIBUTES_CSV = "attributes.csv";
	public final static String RELATIONS_CSV = "relations.csv";

	public final static String RESULTS_FILE_BASENAME = "results";
	public final static String RESULTS_FILE_EXTENSION = "csv";
	
	public final static String RULE_FILE_BASENAME = "rule";
	public final static String RULE_FILE_EXTENSION = "bin";
	
	public static final double MINIMIUM_CONSISTENCY = 1.00;
}

