package constantManagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jpl.Query;

import knowledgeBase.KnowledgeBaseFactory;
import knowledgeBase.KnowledgeBaseOperations;
import knowledgeBase.PrologUtilities;



import combinatorial.CombinationsWithoutRepetition;
import combinatorial.ICombinatorial;
import data.Constant;
import data.Predicate;
import data.Sign;
import data.Term;
import data.Type;

public class OrderedConstants {

	public static OrderedConstantsTO putConstantInOrder() {
		
		KnowledgeBaseFactory kBase = KnowledgeBaseFactory.getInstance();
		KnowledgeBaseOperations kbo = kBase.getKnowledgeBaseOperations();

		OrderedConstantsTO result = new OrderedConstantsTO();
		Map<String, List<String>> consistentArguments = new HashMap<String, List<String>>();
		Map<String,List<String>> finalOrders = new HashMap<String, List<String>>();
		List<Predicate> predicateList = kbo.getDefinedPredicates();
		predicateList.addAll(kbo.getPredefinedPredicates());
		Hashtable<String, Predicate> predicateSet = kbo.getPredicateSet();
		//constant set ordered by type
		List<Constant> constantList = null;
		//constant list ordered by its frequency
		List<ConstantFrequency> orderedConstantList = null;
		//criteria to order the constant list
		Comparator<ConstantFrequency> sortingCriteria = new OrderedConstantsComparator();
		//get consistent arguments for support predicates and goal predicates
		Map<String, List<ConsistentPairs>> consistentArgumentsMap = checkConsistence(predicateList, kbo);
		List<ConsistentPairs> consistentArgumentsList = null;
		Iterator<String> typesIt = null;
		String typeName = null;
		
		/* order table
		 * table[i][j] = true if i appears before j in each tuple and j never appears before i.
		 * table[i][j] = false in otherwise
		 */
		boolean[][] table = null;

		//for each type of constants it creates a orderly table
		OrderedConstantsTransferObject oTO = null;
		
		
		typesIt = kbo.getTypeSet().iterator();
		while (typesIt.hasNext()) {
			typeName = typesIt.next();
			System.gc();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (consistentArgumentsMap.containsKey(typeName)) {
				
				consistentArgumentsList = consistentArgumentsMap.get(typeName);
				constantList = kbo.getConstantSet(typeName);
				
				oTO = createOrderTable(constantList,consistentArgumentsList, kbo);
				
				if (oTO.isSorted()) {
					
					table = oTO.getOrderdlyTable();
//					consistentArgumentsList = oTO.getConsistentArgumentsList();

					List<Constant> orderedConstants = new ArrayList<Constant>();
					
					//to get the constants number belonging to specific type
					int size = constantList.size(); 
					float[] thresholdsTable = new float[size];
					
					
					// to fill thresholdsTable
					for (int index1 = 0; index1 < thresholdsTable.length; index1++) {
						thresholdsTable[index1] = countEntries(index1, table) + index1 / (float) size;
					}
					
					orderedConstantList = new ArrayList<ConstantFrequency>();
					//it gets the constant list of a specified type and it updates the frecuency of each constant
					for (int index1 = 0; index1 < thresholdsTable.length; index1++) {
						orderedConstantList.add(new ConstantFrequency(constantList.get(index1), thresholdsTable[index1]));
					}
				
					Iterator<ConsistentPairs> consistentArgsList = consistentArgumentsList.iterator();
					while (consistentArgsList.hasNext()) {
						ConsistentPairs cp = consistentArgsList.next();
						String pName = cp.getPredicateName();
						String line;	
						if (oTO.getConsistentPredicates().contains(cp.getPredicateName()))
							line = "\tArguments " + (cp.getFirstArg() + 1) + ", " + (cp.getSecondArg() + 1) + " are consistent";
						else 
							line = "\tArguments " + (cp.getFirstArg() + 1) + ", " + (cp.getSecondArg() + 1) + " are not consistent";
						
						List<String> argList;
						if (consistentArguments.containsKey(pName))
							argList = consistentArguments.get(pName);
						else
							argList = new ArrayList<String>();
						argList.add(line);
						consistentArguments.put(pName, argList);
					}
						
					Collections.sort(orderedConstantList, sortingCriteria);
					
					List<String> constants = new ArrayList<String>();
					if (orderedConstantList.size() > 0) {
						for (int index1 = 0; index1 < orderedConstantList.size(); index1++) {
							orderedConstants.add(orderedConstantList.get(index1).getConstant());
							constants.add(orderedConstantList.get(index1).getConstant().getValue());
						}
						finalOrders.put(orderedConstantList.get(0).getConstant().getType().getTypeName(), constants);
					}
					
					consistentArgumentsList = oTO.getConsistentArgumentsList();
					//Puts arguments pairs in order
					for (int i = 0; i < consistentArgumentsList.size(); i++) {
						ConsistentPairs consistentArgs = consistentArgumentsList.get(i);
						int firstArg = consistentArgs.getFirstArg();
						int secondArg = consistentArgs.getSecondArg();
						String predicateName = consistentArgs.getPredicateName();
						ConsistentPairs resultArgs = null;
						Iterator<List<Constant>> it = null;
						if (predicateName.startsWith("~"))
							it = kbo.consultFacts(predicateName.substring(1), Sign.NEGATIVE).iterator();
						else
							it = kbo.consultFacts(predicateName, Sign.POSITIVE).iterator();
						if (it.hasNext()) {
							List<Constant> tuple = it.next();
							Constant value1 = tuple.get(firstArg);
							Constant value2 = tuple.get(secondArg);
							int index1 = value1.belongs(orderedConstants);
							int index2 = value2.belongs(orderedConstants);
							
							resultArgs = new ConsistentPairs();
							resultArgs.setPredicateName(predicateName);
							resultArgs.setFirstArg(firstArg);
							resultArgs.setSecondArg(secondArg);
							
							// setting the sign, one by default (index1 < index2)
							if (index1 == index2) 
								resultArgs.setSign(0);
							else if (index1 < index2)
								resultArgs.setSign(1);
							else if (index1 > index2)
								resultArgs.setSign(2);
							
						}
						result.addConsistentPair(resultArgs);
					}
					
				}// end of if (oTO.isOrderdly())
				
				// WE HAVE TO CHECK THAT, WE ONLY CONTROL IF THEY ARE EQUAL OR NOT
				consistentArgumentsList = checkIntensionalOrder(consistentArgumentsList, kbo);
				String fileName = kbo.getFileName();
				for (ConsistentPairs cp : consistentArgumentsList) {
					String predicateName = cp.getPredicateName();
					int arity = predicateSet.get(predicateName).getArity();
					if (!PrologUtilities.isFact(predicateName,arity, fileName)) {
						result.addConsistentPair(cp);
					}
				}
			}
		}
		result.setConsistentArguments(consistentArguments);
		result.setFinalOrders(finalOrders);
		return result;
	}
	
	private static List<ConsistentPairs> checkIntensionalOrder(
			List<ConsistentPairs> consistentArgumentsList, KnowledgeBaseOperations kBase) {
		List<ConsistentPairs> result = new ArrayList<ConsistentPairs>();
		ConsistentPairs consistentPair = null;
		String predicateName = null;
		
		Hashtable<String, Predicate> predicateSet = kBase.getPredicateSet(); 
		Predicate p = null;
		Query query = null;
		Set<Integer> existingVarsPositions = null;
		
		for (ConsistentPairs cp : consistentArgumentsList) {
			predicateName = cp.getPredicateName();
			p = predicateSet.get(predicateName);
			existingVarsPositions = new HashSet<Integer>();
			existingVarsPositions.add(cp.getFirstArg());
			existingVarsPositions.add(cp.getSecondArg());
			query = PrologUtilities.setofQuery(p,existingVarsPositions);
			if (!query.hasSolution()) {
				consistentPair = new ConsistentPairs();
				consistentPair.setFirstArg(cp.getFirstArg());
				consistentPair.setSecondArg(cp.getSecondArg());
				consistentPair.setPredicateName(predicateName);
				result.add(consistentPair);
			}
			query.close();
		}
		return result;
	}

	/**
	 * Gets a pair arguments list which are consistent
	 * @param predicateList each predicate where it evaluates the consistence between its arguments
	 * @return a list of consistent arguments pairs for each predicate
	 */
	private static Map<String, List<ConsistentPairs>> checkConsistence(List<Predicate> predicateList, KnowledgeBaseOperations kBase) {
		Map<String, List<ConsistentPairs>> result = new HashMap<String, List<ConsistentPairs>>();
		List<ConsistentPairs> consistentPairsList = null;
		ConsistentPairs consistentPairs = null;
		ICombinatorial<int[]> comb = null;
		int[] indexArray = null;
		List<Type> predicateArgs = null;
		String targetPredicateName = kBase.getTargetLiteral().getPredicate().getName();
		
		for (Predicate predicate : predicateList) { //for each predicate
			//it doesn't study predefined predicates or predicates with only one argument
			if (!predicate.isPredefined() && predicate.getArity() >= 2) {
				//it generates all combinations without repetitions between the arguments in a specific predicate
				comb = new CombinationsWithoutRepetition<int[]>(2, predicate.getArity());
				//it gets the type of the arguments list
				predicateArgs = predicate.getArgsType();
				while (comb.hasNext()) {
					indexArray = comb.next ();
					//checks if each pair of arguments got in the combinations has the same type
					if (predicateArgs.get(indexArray[0]).equals(predicateArgs.get(indexArray[1]))) {
						String typeName = predicateArgs.get(indexArray[0]).getTypeName();
						if (result.containsKey(typeName))
							consistentPairsList = result.get(typeName);
						else 
							consistentPairsList = new ArrayList<ConsistentPairs>();
						
						consistentPairs = new ConsistentPairs();
						consistentPairs.setPredicateName(predicate.getName());
						consistentPairs.setFirstArg(indexArray[0]);
						consistentPairs.setSecondArg(indexArray[1]);
						consistentPairsList.add(consistentPairs);
						result.put(typeName, consistentPairsList);
						
						if (predicate.getName().equals(targetPredicateName) && !kBase.consultFacts(targetPredicateName, Sign.NEGATIVE).isEmpty()) {
							consistentPairs = new ConsistentPairs();
							consistentPairs.setPredicateName("~" + predicate.getName());
							consistentPairs.setFirstArg(indexArray[0]);
							consistentPairs.setSecondArg(indexArray[1]);
							consistentPairsList.add(consistentPairs);
							result.put(typeName, consistentPairsList);
						}
					}
				}
			}
		}//end for
		return result;
	}
	
	/**
	 * Gets a table that contains the order of the constants 
	 * table[i][j] = true if i appears before j in each tuple and j never appears before i.
	 * table[i][j] = false in otherwise
	 *  
	 * @param constantSet set of constants to put them in order
	 * @param consistentArguments list of consistent arguments pair
	 * @param kBase to do the consults to the knowledgeBase
	 * @return a ConstantSortTransferObject that contains
	 * 		- an attribute that indicates if the arguments are in order
	 * 		- a table with the order in which the constants appear
	 */
	public static OrderedConstantsTransferObject createOrderTable(List<Constant> constantList, 
			List<ConsistentPairs> consistentArguments, KnowledgeBaseOperations kBase) {
		
		OrderedConstantsTransferObject result = new OrderedConstantsTransferObject();
		OrderedConstantsTransferObject aux = null;
		List<ConsistentPairs> trueConsistentArguments = new ArrayList<ConsistentPairs>(); 
		boolean[][] orderlyTable = null, previousOrderlyTable = null;
		Map<String, boolean[][]> orderlyTableByPredicate = new HashMap<String, boolean[][]>();
		Set<String> consistentPredicates = new HashSet<String>();
		Set<String> predicatesToRemove = new HashSet<String>();
	
		boolean end = false;
		String predicateName = null;
		int i = 0;
		
//		for (Constant c : constantList) {
//			System.out.print(c.getValue() + "  ");
//		}
//		System.out.println();
		
//		String targetPredicate = kBase.getTargetLiteral().getPredicate().getName();
		
		// Iterating over each consistent arguments pair
		while (i < consistentArguments.size()) {
			ConsistentPairs consistentPair = consistentArguments.get(i);
			predicateName = consistentPair.getPredicateName();
			
			if (!predicatesToRemove.contains(predicateName)) {
				if (!end || !orderlyTableByPredicate.containsKey(predicateName)) { //creo que esto se puede quitar
					//loading positive bindings of a given predicate
					end = false;
					
					List<List<Constant>> tuples = null;
					
					if (predicateName.startsWith("~")) {
						//porque ya no calculamos las ctes que vengan en predicados definidos mediante un conjunto de reglas
//						tuples = kBase.consultTuples(predicateName.substring(1), Sign.NEGATIVE, constantList); 
						tuples = kBase.consultFacts(predicateName.substring(1), Sign.NEGATIVE);
//						tuples = kBase.consultTuplesFromRules(predicateName.substring(1), Sign.NEGATIVE);
					}
					else
						//porque ya no calculamos las ctes que vengas en predicados definidos mediante un conjunto de reglas
						tuples = kBase.consultFacts(predicateName, Sign.POSITIVE);
//						tuples = kBase.consultTuples(predicateName, Sign.POSITIVE, constantList);
//						tuples = kBase.consultTuplesFromRules(predicateName, Sign.POSITIVE);
					
					Iterator<List<Constant>> it = tuples.iterator();
					if (tuples.isEmpty())
						end = true;
					else {
						
						if (orderlyTableByPredicate.containsKey(predicateName)) {
							previousOrderlyTable = orderlyTableByPredicate.get(predicateName);
							orderlyTable = orderlyTableByPredicate.get(predicateName).clone();
						}
						else {
							//loading constants of a specific type
							orderlyTable = new boolean[constantList.size()][constantList.size()];
							for (int j = 0; j < orderlyTable.length; j++) {
								Arrays.fill(orderlyTable[j], false);
							}
						}
						
						//iterating over positive bindings of a given predicate
						while (it.hasNext() && !end) {
//							String s = "<";
							List<Constant> tuple = it.next();
							Term constant1 = tuple.get(consistentPair.getFirstArg());
							Term constant2 = tuple.get(consistentPair.getSecondArg());
//								s += constant1.getValue() + ", " + constant2.getValue() + ">";
//								System.out.print(s);
							int index1 = constant1.belongs(constantList);
							int index2 = constant2.belongs(constantList);
							if (index1 != index2) {
								if (orderlyTable[index2][index1] == false)
									orderlyTable[index1][index2] = true;
								else
									end = true;
							}
							else
								end = true;
//							if (orderlyTable[index2][index1] == false) {
//								orderlyTable[index1][index2] = true;
//							}
//							else if (index1 != index2)
//								end = true;	
						}
							
						if (!end) {
							aux = transitiveClosure(orderlyTable);
							orderlyTable = aux.getOrderdlyTable();
							end = !aux.isSorted();
							orderlyTableByPredicate.put(predicateName, orderlyTable);
							
//								for (int j = 0; j < orderlyTable.length; j++) {
//									for (int j2 = 0; j2 < orderlyTable[j].length; j2++) {
//										System.out.println("[" + j + "][" + j2 + "] = " + orderlyTable[j][j2]);
//									}
//								}
							consistentPredicates.add(predicateName);
							trueConsistentArguments.add(consistentPair);
						}
						else {
							if (previousOrderlyTable == null) {
								if (orderlyTableByPredicate.containsKey(predicateName))
									orderlyTableByPredicate.remove(predicateName);
								if (consistentPredicates.contains(predicateName))
									consistentPredicates.remove(predicateName);
								predicatesToRemove.add(predicateName);
							}
							else 
								orderlyTable = previousOrderlyTable;
						}
					}
				}
			}
			i++;
		}
		result = checkConsistenceTables(orderlyTableByPredicate);
		result.setConsistentArgumentsList(trueConsistentArguments);
		if (result.isSorted())
			result.setConsistentPredicates(consistentPredicates);		
		
		return result;
	}
	
	
	private static OrderedConstantsTransferObject checkConsistenceTables(
			Map<String, boolean[][]> orderlyTableByPredicate) {
		OrderedConstantsTransferObject result = new OrderedConstantsTransferObject();
		
		boolean[][] orderlyTable = null, aux = null;
		String nextPredicateName = null;
		int i = 0, j = 0;
		boolean end = orderlyTableByPredicate.size() <= 0;
		Entry<String, boolean[][]> mapEntry = null;
		Map<String,Boolean> reverseOrders = new HashMap<String,Boolean>();
		
		Iterator<Entry<String, boolean[][]>> it = orderlyTableByPredicate.entrySet().iterator();
		String predicateName;
		while (it.hasNext() && !end) {
			
			if (nextPredicateName == null) {
				mapEntry = it.next();
				predicateName = mapEntry.getKey();
				orderlyTable = mapEntry.getValue();
				if (!reverseOrders.containsKey(predicateName))
					reverseOrders.put(predicateName, false);
			}
			
			if (it.hasNext()) {
				mapEntry = it.next();
				nextPredicateName = mapEntry.getKey();
				aux = mapEntry.getValue();
				if (!reverseOrders.containsKey(nextPredicateName))
					reverseOrders.put(nextPredicateName, false);
				//checks order
				i = 0;
				while (i < orderlyTable.length && !end) {
					j = 0;
					while (j < orderlyTable[i].length && !end){
						if (orderlyTable[i][j]) {
							if (aux[j][i] && !reverseOrders.get(nextPredicateName)) {
								aux = reverse(aux);
								reverseOrders.put(nextPredicateName, true);
							}
							else if (reverseOrders.get(nextPredicateName) && aux[j][i])
								end = true;
						}
						j++;
					}
					i++;
				}
			}
			
			if (!end && (nextPredicateName != null)) { //means both tables are consistent
				
				i = 0;
				while (i < aux.length) {
					j = 0;
					while (j < aux[i].length){
						if (aux[i][j])
							orderlyTable[i][j] = true;
						j++;
					}
					i++;
				}
			}
		}
		result.setSorted(!end);
		result.setOrderdlyTable(orderlyTable);
		return result;
	}

	private static boolean[][] reverse(boolean[][] orderlyTable) {
		boolean[][] result = null;
		int length = orderlyTable.length;
		result = new boolean[length][length];
		
		for (int i = 0; i < result.length; i++) {
			Arrays.fill(result[i], false);
		}
		
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				if (orderlyTable[j][i])
					result[i][j] = true;
			}
		}
		
		return result;
	}

	/**
	 * checks if orderlyTable is still consistent by transitive closure: 0 < 1, 1 < a => 0 < a
	 * @param orderlyTable table with the order of the appearance of the constants
	 * @return a ConstantSortTransferObject where it indicates if the orderlyTable is ordered and
	 * the orderlyTable with the closure transitives
	 */
	private static OrderedConstantsTransferObject transitiveClosure(boolean[][] orderlyTable) {
		OrderedConstantsTransferObject result = new OrderedConstantsTransferObject();
		int index1 = 0, index2 = 0;
		boolean end = false;
		int length = orderlyTable.length;
		boolean[][] table = new boolean[length][length];
		for (int i = 0; i < orderlyTable.length; i++) {
			table[i] = Arrays.copyOf(orderlyTable[i], length);
		}
		int j;
		while (index1 < table.length && !end) {
			index2 = 0;
			while (index2 < table[index1].length && !end) {
				if (table[index1][index2] && index1 != index2) { 
					j = 0;
					while (j < table.length && !end) {
						if (table[j][index1] && j != index1) {
							if (!table[index1][j])
								table[j][index2] = true;
							else
								end = true;
						}
						j++;
					}
				}	
				index2++;
			}
			index1++;
		}
		result.setSorted(!end);
		result.setOrderdlyTable(table);
//		System.out.println();
//		for (j = 0; j < table.length; j++) {
//			for (int j2 = 0; j2 < table[j].length; j2++) {
//				System.out.println("[" + j + "][" + j2 + "] = " + table[j][j2]);
//			}
//		}
		return result;
	}
	
	private static int countEntries(int k, boolean[][] orderTable){
		int result = 0;
		for (int i = 0; i < orderTable[k].length; i++) {
			if (orderTable[k][i])
				result++;
		}
		return result;
	}
}

//IMPRIMIR TABLAS
//for (int i = 0; i < table.length; i++) {
//	for (int j = 0; j < table[i].length; j++) {
//		System.out.println("table["+i+"]["+j+"]= " + table[i][j]);
//	}
//}
//int patri = 0;
//for (Constant c: constantList) {
//	System.out.println("constant " + patri + " = " + c.getValue());
//	patri++;
//}

//for (Constant c: constantList) {
//	System.out.println("Constante = " + c.getValue());
//}
//for (int i = 0; i < table.length; i++) {
//	for (int j = 0; j < table[i].length; j++) {
//		System.out.println("Table["+ i + "][" + j + "] = " + table[i][j]);
//	}
//}