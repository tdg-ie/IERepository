package datasets;

import java.util.ArrayList;
import java.util.List;

public class ContextTuple {

	private String target;
	private String relation;
	private String source;
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getRelation() {
		return relation;
	}
	
	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String target) {
		this.source = target;
	}

	private static ContextTuple defaultContext;
	
	static {
		defaultContext = new ContextTuple();
		defaultContext.setTarget("node");
		defaultContext.setRelation(null);
		defaultContext.setSource(null);
	}

	public static ContextTuple defaultContext() {
		return defaultContext;
	}
	
	public static List<ContextTuple> defaultContexts() {
		List<ContextTuple> result;
		
		result = new ArrayList<ContextTuple>();
		result.add(defaultContext);
		
		return result;
	}
	
	public boolean isDefault() {
		boolean result;
		
		result = (this == defaultContext);
		
		return result;
	}
		
	public String toString() {
		String result;
		
		if (source == null)
			result = target;
		else
			result = String.format("%s = %s(%s)", target, relation, source);
		
		return result.toString();
	}

}
