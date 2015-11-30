package datasets;

import helpers.InstanceHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import configuration.Constants;

public class DataSet extends AbstractTable {

	private Instances positives;
	private Instances negatives;
	private List<ContextTuple> contexts;
	private Map<String, Integer> clazzCount; // Class --> Counter
	private Remove filter;
	
	
	@Override
	public void setInstances(Instances instances) {
		Instance instance;
		String clazz;
		Integer counter;

		super.setInstances(instances);
		
		clazzCount = new HashMap<String, Integer>();
		positives = new Instances(getInstances(), 0, 0);
		negatives = new Instances(getInstances(), 0, 0);
		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);
			
			clazz = InstanceHelper.getClazz(instance);
			if (clazz.equals(Constants.NULL))
				negatives.add(instance);
			else
				positives.add(instance);

			counter = clazzCount.get(clazz);
			if (counter == null)
				counter = 1;
			else
				counter++;
			clazzCount.put(clazz, counter);
		}
	}
	
	public Instances getPositives() {
		return positives;
	}

	public Instances getNegatives() {
		return negatives;
	}

	public Filter getFilter() {
		return filter;
	}

	public List<ContextTuple> getContexts() {
		return contexts;
	}

	public void setContexts(List<ContextTuple> contexts) {
		StringBuilder indices;
		String comma, attribute;
		int index;

		try {
			this.contexts = contexts;

			indices = new StringBuilder();
			comma = "";
			for (ContextTuple Context : contexts) {
				attribute = Context.getTarget();
				index = getAttributeDefinition(attribute).index();
				indices.append(comma);
				indices.append(index + 1);
				comma = ",";

				if (!Context.isDefault()) {
					attribute = String.format("%s%s%s", Context.getTarget(), Constants.ATTRIBUTE_SEPARATOR, Constants.CLAZZ);
					index = getAttributeDefinition(attribute).index();
					indices.append(comma);
					indices.append(index + 1);
				}
			}

			filter = new Remove();
			filter.setAttributeIndices(indices.toString());
			filter.setInputFormat(getInstances());
			
			getInstances().setRelationName(contexts.toString());
		} catch (Throwable oops) {
			throw new RuntimeException(oops);
		}
	}
	
}
