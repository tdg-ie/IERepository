package coverageComputation;
import thresholds.Thresholds;
import data.Sign;

/**
 * 
 * @author patri
 *
 */
public class AlphaBetaPruning {
	
	private boolean possibleT;
	private boolean possibleF;

	public AlphaBetaPruning() {
		this.possibleT = possibleF = true;
	}
	
	/**
	 * Check wether the node has to be pruned
	 * @param dataSC data related with the coverage
	 * @param tupleSign sign of the last covered tuple
	 * @param node nodo to check 
	 * @param thresholds of coverage. It contains the maximum number of negative tuples that can be covered by the node 
	 * and de minimum number of tuples that can be covered by the node so it will not be pruned
	 * @return true if the node have to be pruned, false otherwise
	 */
	public boolean checkPrune(ExtraCoverageData extra, Thresholds thresholds){
		
		int origFPos = 0, origTPos = 0;
		
		int currentNPos = extra.getCurrentNPos();  
		Sign tupleSign = extra.getTupleSign();
		Sign literalSign = extra.getLiteralSign();
		
		int currentNOrigPos = extra.getCurrentNOrigPos();
		int origPos = extra.getOrigPos();
		
		boolean result = false;
		int maxCover = 0;
		//tupleSign = false if the tuple is positive, true otherwise
		if (!extra.isDeterminate()) {
			// positive case
			if (tupleSign.equals(Sign.POSITIVE)) {
				if (literalSign.equals(Sign.POSITIVE)) {
					/** 
					 * If all remaining pos tuples go to NowFNeg, are there sufficient to make ~L viable? 
					 * Note: do not abandon ~L if it could lead to a saveable clause (assuming all remaining original
					 * pos tuples are covered by ~L)  
					 */
					
					origFPos = extra.getOrigTruePosNegatedLit();
					maxCover = origFPos + (currentNOrigPos - origPos);
					if (maxCover < thresholds.getMinSaveableCover()) {
						this.possibleF &= updatePossible(currentNPos - extra.getNowTruePosLit() < thresholds.getMinPos() - 1E-3);
						result = termTest();			
					}
				}
			
				else if (literalSign.equals(Sign.NEGATIVE)) {
					/**
					 * If all remaining pos tuples go to NowFPos, are there sufficient to make L viable?
					 * Note: don't kill L if it could lead to a saveable rule when all remaining
					 * original pos tuples covered by L
					 */
						origTPos = extra.getOrigTruePosLit();
						maxCover = origTPos + (currentNOrigPos - origPos);
						if (maxCover < thresholds.getMinSaveableCover()) {
							this.possibleT &= updatePossible(currentNPos - extra.getNowTruePosNegatedLit() < thresholds.getMinPos() - 1E-3);
							result = termTest();
						}
				}
			}
			
			// negative case
			else if (tupleSign.equals(Sign.NEGATIVE)) {
				if (literalSign.equals(Sign.POSITIVE)) {
					/**
					 * We already know the final number of NewTPos tuples. Are there now enough NewTNeg tuples
					 * to make the gain of L insufficient? (Note: since L matches a neg tuple, don't have to worry
					 * about saveable rule.)  
					 */
			
					this.possibleT &= updatePossible(extra.getNew_nowFalsePosLit() > thresholds.getNewTNegThresh() + 1E-3);
					result = termTest();
				
				}
				else if (literalSign.equals(Sign.NEGATIVE)) {
					/**
					 * We already know the final number of NowTNeg tuples. Are there already enough NowFNeg tuples
					 * to make the gain of ~L insuffient? (As above saveability not relevant.)
					 */
					
					this.possibleF &= updatePossible(extra.getNowFalsePosNegatedLit() > thresholds.getNowFNegThresh() + 1E-3);
					result = termTest();
				}
			}
		}

		return result;
	}
	
	private boolean updatePossible(boolean cond) {
		return !(cond == true);
	}


	private boolean termTest(){
	/**
	 * Make a pass through the tuples, terminating if it becomes clear that
	 * neigther L or ~L can achieve the minimum useful gain.
	 */
		return (!possibleT && !possibleF);
	}

	public boolean isPossibleT() {
		return possibleT;
	}

	public void setPossibleT(boolean possibleT) {
		this.possibleT = possibleT;
	}

	public boolean isPossibleF() {
		return possibleF;
	}

	public void setPossibleF(boolean possibleF) {
		this.possibleF = possibleF;
	}

//	public void reset() {
//		this.possibleF = this.possibleT = true;
//	}
}