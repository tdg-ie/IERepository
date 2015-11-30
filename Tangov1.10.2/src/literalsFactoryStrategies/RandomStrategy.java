package literalsFactoryStrategies;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import data.Predicate;


public class RandomStrategy implements IPredicateSortStrategies {

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
		Collections.shuffle(result);
		return Collections.unmodifiableList(result);
	}
}