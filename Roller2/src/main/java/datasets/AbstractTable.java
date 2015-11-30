//	public Attribute getAttribute(int index) {
//		Attribute result;
//
//		result = instances.attribute(index);
//
//		return result;
//	}

//	public Set<String> getClazzes() {
//		Set<String> result;
//
//		result = clazzMap.keySet();
//
//		return result;
//	}

//	public Instance getInstance(int index) {
//	Instance result;
//
//	result = instances.instance(index);
//
//	return result;
//}

//public String getAttribute(String node, String attribute) {
//	String result;
//	Instance instance;
//
//	instance = instanceMap.get(node);
//	result = getAttribute(instance, attribute);
//
//	return result;
//}

//public String getAttribute(Instance instance, String attribute) {
//	String result;
//	int index;
//	
//	if (instance == null)
//		result = null;
//	else { 
//		index = attributeMap.get(attribute);
//		result = instance.stringValue(index);
//	}
//	
//	return result;
//}

//public String getClazz(Instance instance) {
//	String result;
//	int index;
//
//	index = (int) instance.classValue();
//	result = instances.attribute(clazzIndex).value(index);
//
//	return result;
//}

package datasets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import configuration.Constants;

public abstract class AbstractTable {

	private Instances instances;

	private int nodeIndex;
	private int clazzIndex;

	private Map<String, Instance> instanceMap; // Node --> Instance
	private Map<String, Integer> attributeMap; // Attribute --> Index
	private Map<String, Integer> clazzMap; // Clazz --> Index

	private List<String> userAttributes;
	private List<String> allAttributes;

	public Instances getInstances() {
		return instances;
	}

	public void setInstances(Instances instances) {
		Instance instance;
		Attribute attribute;
		String node, clazz, name;

		this.instances = instances;

		attributeMap = new HashMap<String, Integer>();
		userAttributes = new ArrayList<String>();
		allAttributes = new ArrayList<String>();
		nodeIndex = -1;
		clazzIndex = -1;
		for (int i = 0; i < instances.numAttributes(); i++) {
			attribute = instances.attribute(i);
			name = attribute.name();
			attributeMap.put(name, i);
			allAttributes.add(name);
			if (name.equals(Constants.NODE))
				nodeIndex = i;
			else if (name.equals(Constants.NODE_CLAZZ))
				clazzIndex = i;
			else
				userAttributes.add(name);
		}
		if (nodeIndex == -1)
			throw new RuntimeException(String.format("'%s' attribute not defined!", Constants.NODE));
		if (clazzIndex == -1)
			throw new RuntimeException(String.format("'%s' attribute not defined!", Constants.NODE_CLAZZ));
		instances.setClassIndex(clazzIndex);

		instanceMap = new HashMap<String, Instance>();
		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);
			node = instance.stringValue(nodeIndex);
			instanceMap.put(node, instance);
		}

		clazzMap = new HashMap<String, Integer>();
		attribute = instances.attribute(clazzIndex);
		for (int i = 0; i < attribute.numValues(); i++) {
			clazz = attribute.value(i);
			clazzMap.put(clazz, i);
		}
	}

	public Set<String> getClazzes() {
		Set<String> result;

		result = clazzMap.keySet();

		return result;
	}

	public Instance getInstance(int i) {
		Instance result;

		result = instances.instance(i);

		return result;
	}

	public Attribute getAttributeDefinition(String name) {
		Attribute result;

		result = instances.attribute(name);

		return result;
	}

	public List<String> getAttributes(boolean all) {
		List<String> result;

		if (all)
			result = allAttributes;
		else
			result = userAttributes;

		return result;
	}

	public Instance getInstance(String node) {
		Instance result;

		result = instanceMap.get(node);

		return result;
	}

	public int numInstances() {
		int result;

		result = instances.numInstances();

		return result;
	}

	public int numAttributes() {
		int result;

		result = instances.numAttributes();

		return result;
	}

	public int numClazzes() {
		int result;

		result = clazzMap.size();

		return result;
	}

}
