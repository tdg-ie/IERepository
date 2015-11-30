package helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import configuration.Constants;
import configuration.RollerConfiguration;
import datasets.ContextTuple;
import datasets.DataSet;
import datasets.RelationSet;

public class DataSetHelper {
	
	public static DataSet computeExpansion(DataSet dataSet, ContextTuple newcontext) {
		DataSet result;
		
		result = computeExpansion(dataSet, null, newcontext);
		
		return result;
	}

	public static DataSet computeExpansion(DataSet dataSet, DataSet additional, ContextTuple newcontext) {
		DataSet result;
		Instances newInstances;
		List<ContextTuple> newContexts;

		newInstances = computeMetadata(dataSet, newcontext);
		copyInstancesInto(dataSet, newInstances, newcontext);
		if (additional != null)
			copyInstancesInto(additional, newInstances, newcontext);

		newContexts = new ArrayList<ContextTuple>(dataSet.getContexts());
		newContexts.add(newcontext);
		
		newInstances.setRelationName(newContexts.toString());

		result = new DataSet();
		result.setInstances(newInstances);
		result.setContexts(newContexts);
		
		return result;
	}
	
	protected static Instances computeMetadata(DataSet dataSet, ContextTuple newContext) {
		Instances result;
		DataSet trainingSet;
		Instances oldInstances;
		Attribute oldAttribute, newAttribute;
		String newName;

		trainingSet = RollerConfiguration.getTrainingSet();
		oldInstances = dataSet.getInstances();
		result = new Instances(oldInstances, 0, 0);
		for (String attributeName : trainingSet.getAttributes(true)) {
			oldAttribute = trainingSet.getAttributeDefinition(attributeName);
			newName = oldAttribute.name().replace(Constants.NODE, "");
			newName = String.format("%s%s", newContext.getTarget(), newName);
			newAttribute = oldAttribute.copy(newName);
			result.insertAttributeAt(newAttribute, result.numAttributes());
		}

		return result;
	}
	
	public static void copyInstancesInto(DataSet dataSet, Instances newInstances, ContextTuple newContext) {
		Instances oldInstances;
		Instance oldInstance, newInstance, targetInstance;
		String source, relation, sourceNode;
		Collection<String> targetNodes;
		RelationSet relations;
		DataSet trainingSet;

		source = newContext.getSource();
		relation = newContext.getRelation();
		oldInstances = dataSet.getInstances();
		
		trainingSet = RollerConfiguration.getTrainingSet();
		relations = RollerConfiguration.getRelations();
				
		for (int i = 0; i < oldInstances.numInstances(); i++) {
			oldInstance = oldInstances.instance(i);
			sourceNode = InstanceHelper.getAttribute(oldInstance, source);

			if (!sourceNode.equals(Constants.MISSING_VALUE)) {
				
				targetNodes = relations.getRelation(sourceNode, relation);
				
				for (String targetNode : targetNodes) {
					newInstance = new Instance(newInstances.numAttributes());
					newInstance.setDataset(newInstances);
					InstanceHelper.copyInstanceInto(newInstance, oldInstance);
					
					targetInstance = trainingSet.getInstance(targetNode);
					if (targetInstance != null)
						InstanceHelper.appendInstanceTo(newInstance, targetInstance);
					
					newInstances.add(newInstance);
				}
			}			
		}
	}

}
