package helpers;

import configuration.Constants;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;


public class InstanceHelper {
	
	public static String getNode(Instance instance) {
		String result;

		result = getAttribute(instance, Constants.NODE);
		
		return result;
	}
	
	public static String getClazz(Instance instance) {
		String result;

		result = getAttribute(instance, Constants.NODE_CLAZZ);
		
		return result;
	}

	public static String getAttribute(Instance instance, String name) {
		String result;
		Instances instances;
		Attribute attribute;
		
		instances = instance.dataset();
		attribute = instances.attribute(name);
		result = instance.stringValue(attribute);
		
		return result;		
	}
	
	public static void copyInstanceInto(Instance target, Instance source) {
		copyInstanceInto(target, source, 0);
	}
	
	public static void copyInstanceInto(Instance target, Instance source, int position) {
		double value;
		
		for (int i = 0; i < source.numAttributes(); i++) {
			value = source.value(i);
			target.setValue(position + i, value);
		}
	}
	
	public static void appendInstanceTo(Instance target, Instance source) {
		int position;
		
		position = target.numAttributes() - source.numAttributes();
		copyInstanceInto(target, source, position);
	}

}
