package datasets;

import helpers.InstanceHelper;

import java.util.Collection;
import java.util.HashSet;

import weka.core.Instance;
import configuration.Constants;


public class RelationSet extends AbstractTable {

	public Collection<String> getRelation(String node, String relation) {
		Collection<String> result;
		Instance instance;
		String value, nodes[];
				
		instance = getInstance(node);
		value = InstanceHelper.getAttribute(instance, relation);
		
		result = new HashSet<String>();
		if (!value.equals(Constants.NULL)) {
			nodes = value.split(Constants.SET_SEPARATOR);
			for (int i = 0; i < nodes.length; i++)
				result.add(nodes[i]);
		}
		
		return result;
	}

}
