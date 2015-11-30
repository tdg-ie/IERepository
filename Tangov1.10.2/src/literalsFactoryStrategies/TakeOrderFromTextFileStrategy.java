package literalsFactoryStrategies;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import data.Predicate;


public class TakeOrderFromTextFileStrategy implements Comparator<Predicate>,IPredicateSortStrategies {

	private String fileName = "C:/Users/boss/Desktop/IEWorkArea/OrderedListOfFeatures.txt";
	private File inputFile;
	private Map<String, Integer> predicatesSorted;
	
	public TakeOrderFromTextFileStrategy() {
		inputFile = new File(fileName);
		if (!inputFile.exists()){
			System.err.print("Error, the file with the predicates sorting does not exists.");
			System.exit(0);
		}
		else {
			predicatesSorted = new HashMap<String, Integer>();
			try {
				int index, lineNumber = 1;
				LineIterator lines = FileUtils.lineIterator(inputFile);
				while (lines.hasNext()){
					String line = lines.nextLine();
					
					if (!line.startsWith("//")){
						if (line.contains("(")){
							index = line.indexOf("(");
							line = line.substring(0, index);
						}
						if (line.contains("_var_"))
							line = line.substring(0, line.indexOf("_var_"));
						else if (line.contains("_value_"))
							line = line.substring(0, line.indexOf("_value_"));
						predicatesSorted.put(line, lineNumber);
					}
					lineNumber++;	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	/**
	 * Order predicates:
	 *  predefined predicates
	 *  target predicates
	 *  support Predicates are sorted by its arity being the first those that
	 *  has lower arity
	 */
	public List<Predicate> sortPredicates(List<Predicate> predicates) {
		List<Predicate> result = new ArrayList<Predicate>(predicates);
		Collections.sort(result, this);
		return Collections.unmodifiableList(result);
	}

	@Override
	/**
	 * Compares this object with the specified object for order.
	 * 		negative if obj1 < obj2
	 * 		zero if obj1 == obj2
	 * 		positive if obj1 > obj2
	 *   
	 */
	public int compare(Predicate p1, Predicate p2) {
		int result = 0;
		int index1, index2;
		String predicateName1 = p1.getName();
		String predicateName2 = p2.getName();
		
		if (predicateName1.contains("("))
			predicateName1 = predicateName1.substring(0, predicateName1.indexOf("("));
		if (predicateName1.contains("_var_"))
			predicateName1 = predicateName1.substring(0, predicateName1.indexOf("_var_"));
		else if (predicateName1.contains("_value_"))
			predicateName1 = predicateName1.substring(0, predicateName1.indexOf("_value_"));
		
		if (predicateName2.contains("("))
			predicateName2 = predicateName2.substring(0, predicateName2.indexOf("("));
		if (predicateName2.contains("_var_"))
			predicateName2 = predicateName2.substring(0, predicateName2.indexOf("_var_"));
		else if (predicateName2.contains("_value_"))
			predicateName2 = predicateName2.substring(0, predicateName2.indexOf("_value_"));
		
		if (!this.predicatesSorted.containsKey(predicateName1))
			index1 = Integer.MAX_VALUE;
		else 
			index1 = this.predicatesSorted.get(predicateName1);
		if (!this.predicatesSorted.containsKey(predicateName2)) 
			index2 = Integer.MAX_VALUE;
		else
			index2 = this.predicatesSorted.get(predicateName2);
		
		if (index1 < index2)
			result = -1;
		else if (index1 > index2) 
			result = 1;
//		else {
//			if (p1.isPredefined() && p2.isPredefined()) {
//				if (predicateName1.equals("EQUAL") && !predicateName2.equals("EQUAL"))
//					result = 1;
//				else if (!predicateName1.equals("EQUAL") && predicateName2.equals("EQUAL"))
//					result = -1;
//				else if (predicateName1.equals("GT") && predicateName2.equals("GE"))
//					result = 1;
//				else if (predicateName1.equals("GE") && predicateName2.equals("GT"))
//					result = -1;
//				else if (predicateName1.equals("LT") && predicateName2.equals("LE"))
//					result = 1;
//				else if (predicateName1.equals("LE") && predicateName2.equals("LT"))
//					result = -1;
//				else if (predicateName1.equals("LT") && predicateName2.equals("GE"))
//					result = 1;
//				else if (predicateName1.equals("LE") && predicateName2.equals("GT"))
//					result = -1;
//				else if (predicateName1.equals("GT") && predicateName2.equals("LE"))
//					result = 1;
//				else if (predicateName1.equals("GE") && predicateName2.equals("LT"))
//					result = -1;
//			}
//			else if (p1.isPredefined() && !p2.isPredefined())
//				result = -1;
//			else if (!p1.isPredefined() && p2.isPredefined())
//				result = 1;
//		}
		
		return result;
	}
}