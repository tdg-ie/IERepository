package data;

public class Constant extends Term{
	
	private static int constantCounter = 0;
	
	public Constant(String value, Type type){
		this.value = value;
		this.name = "C" + constantCounter;
		constantCounter++;
		this.termType = type;
	}
	
	@Override
	//two constants are equal if they have the same value and type
	public boolean equals(Term t){
		boolean result = false;
		if (t!= null && this.termType.getTypeName().equals(t.getType().getTypeName()) && this.value.equalsIgnoreCase(t.getValue()))
			result = true;
//		if (this.type.getTypeName().equals(t.getType().getTypeName()) && this.value.equals(t.getValue()))
//			result = true;
		return result;
	}

	@Override
	public void setType(Type type) {
		this.termType = type;
	}

	@Override
	public int compareTo(Term t) {
		return this.value.compareTo(t.value);
	}
}