package coverageComputation;

import data.Sign;

public class ExtraCoverageData {
	
	private int currentNOrigPos;
	private int currentNPos; 
	private int currentNNeg;
	private int origPos;
	private Sign tupleSign;
	private Sign LiteralSign;
	private boolean determinate;
	private int origTruePosLit;
	private int origTruePosNegatedLit;
	private int nowTruePosLit;
	private int nowTruePosNegatedLit;
	private int new_nowFalsePosLit;
	private int nowFalsePosNegatedLit;
	

	public ExtraCoverageData() {
		this.currentNOrigPos = 0;
		this.currentNPos = 0;
		this.origPos = 0;
		this.tupleSign = Sign.POSITIVE;
		this.LiteralSign = Sign.POSITIVE;
		this.determinate = true;
	}

	public int getCurrentNOrigPos() {
		return currentNOrigPos;
	}

	public void setCurrentNOrigPos(int currentNOrigPos) {
		this.currentNOrigPos = currentNOrigPos;
	}

	public int getCurrentNPos() {
		return currentNPos;
	}

	public void setCurrentNPos(int currentNPos) {
		this.currentNPos = currentNPos;
	}

	public Sign getTupleSign() {
		return tupleSign;
	}

	public void setTupleSign(Sign tupleSign) {
		this.tupleSign = tupleSign;
	}

	public Sign getLiteralSign() {
		return LiteralSign;
	}

	public void setLiteralSign(Sign literalSign) {
		LiteralSign = literalSign;
	}

	public boolean isDeterminate() {
		return determinate;
	}
	
	public void setDeterminate(boolean determinate) {
		this.determinate = determinate;
	}

	public int getOrigPos() {
		return origPos;
	}

	public void setOrigPos(int origPos) {
		this.origPos = origPos;
	}

	public int getOrigTruePosLit() {
		return origTruePosLit;
	}

	public void setOrigTruePosLit(int origTruePosLit) {
		this.origTruePosLit = origTruePosLit;
	}

	public int getOrigTruePosNegatedLit() {
		return origTruePosNegatedLit;
	}

	public void setOrigTruePosNegatedLit(int origTruePosNegatedLit) {
		this.origTruePosNegatedLit = origTruePosNegatedLit;
	}

	public int getNowTruePosLit() {
		return nowTruePosLit;
	}

	public void setNowTruePosLit(int nowTruePosLit) {
		this.nowTruePosLit = nowTruePosLit;
	}

	public int getNowTruePosNegatedLit() {
		return nowTruePosNegatedLit;
	}

	public void setNowTruePosNegatedLit(int nowTruePosNegatedLit) {
		this.nowTruePosNegatedLit = nowTruePosNegatedLit;
	}

	public int getNew_nowFalsePosLit() {
		return new_nowFalsePosLit;
	}

	public void setNew_nowFalsePosLit(int new_nowFalsePosLit) {
		this.new_nowFalsePosLit = new_nowFalsePosLit;
	}

	public int getNowFalsePosNegatedLit() {
		return nowFalsePosNegatedLit;
	}

	public void setNowFalsePosNegatedLit(int nowFalsePosNegatedLit) {
		this.nowFalsePosNegatedLit = nowFalsePosNegatedLit;
	}

	public int getCurrentNNeg() {
		return currentNNeg;
	}

	public void setCurrentNNeg(int currentNNeg) {
		this.currentNNeg = currentNNeg;
	}
}