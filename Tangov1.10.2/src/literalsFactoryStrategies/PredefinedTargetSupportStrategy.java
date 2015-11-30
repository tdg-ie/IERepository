package literalsFactoryStrategies;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import data.Predicate;


public class PredefinedTargetSupportStrategy implements Comparator<Predicate>,IPredicateSortStrategies {

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
	 * 		zero if obj1 < obj2
	 * 		positive if obj1 > obj2
	 *   
	 */
	public int compare(Predicate p1, Predicate p2) {
		int result = 0;
		if (p1.isPredefined()) {
			if (!p2.isPredefined())
				result = -1;
		}
		else if (p1.isTarget()) {
			if (p2.isPredefined())
				result = 1;
			else if (p2.isTarget()) {
				if (p1.getArity() < p2.getArity())
					result = -1;
				else if (p1.getArity() > p2.getArity())
					result = 1;
			}
			else if (p2.isSupport())
				result = -1;	
		}
		else if (p1.isSupport()) {
			if (!p2.isSupport())
				result = 1;
			else {
				if (p1.getArity() < p2.getArity())
					result = -1;
				else if (p1.getArity() > p2.getArity())
					result = 1;
			}	
		}
		return result;
	}
}