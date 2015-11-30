package data;

public class Variable extends Term {
	
	private static int variableCounter = 0;
	
	public Variable(Type type){
		this.name = "X" + variableCounter;
		variableCounter++;
		this.termType = type;
	}
	
//	public Variable(String name, Type type){
//		this.name = name;
//		this.termType = type;
//	}
	
	public Variable(){
		this.name = "X" + variableCounter;
		variableCounter++;
		this.termType = new Type();
	}
	
	public static void resetExistingVariableCounter(int counter) {
		variableCounter = counter;
	}
	
	@Override
	public boolean equals(Term t){
		boolean result = false;
		if (this.name.equals(t.getName()) && this.termType.getTypeName().equals(t.getType().getTypeName()))
			result = true;
		return result;
	}

	@Override
	public void setType(Type type) {
		this.termType = type;
	}

	@Override
	public int compareTo(Term t) {
		return this.name.compareTo(t.name);
	}
}