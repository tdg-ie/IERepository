package constantManagement;

public class ConsistentPairs {
	private String predicateName;
	private int firstArg;
	private int secondArg;
	private int sign;
	
	public int getFirstArg() {
		return firstArg;
	}
	public int getSecondArg() {
		return secondArg;
	}
	
	public void setFirstArg(int firstArg) {
		this.firstArg = firstArg;
	}
	public void setSecondArg(int secondArg) {
		this.secondArg = secondArg;
	}

	public String getPredicateName() {
		return predicateName;
	}
	public void setPredicateName(String predicateName) {
		this.predicateName = predicateName;
	}
	public int getSign() {
		return sign;
	}
	public void setSign(int sign) {
		this.sign = sign;
	}
}