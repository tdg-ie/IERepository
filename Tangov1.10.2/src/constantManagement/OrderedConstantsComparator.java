package constantManagement;

import java.util.Comparator;


public class OrderedConstantsComparator implements Comparator<ConstantFrequency> {

	/**
	 * Compares two arguments to order them. 
	 * Returns 
	 * 		a negative integer if the first argument is less than the second one, 
	 * 		zero, if the first one is equal than the second one.
	 * 		or a positive integer if the first one is greater than the second one.
	 */

	@Override
	public int compare(ConstantFrequency arg0, ConstantFrequency arg1) {
		int result = 0;
		if (arg0.getFreq() < arg1.getFreq())
			result = -1;
		else if (arg0.getFreq() > arg1.getFreq())
			result = 1;
		return result;
	}
}