package data;

public class Type {
	
	private String typeName;
	private static String withoutType = "0";
	
	public Type(String typeName){
		this.typeName = typeName;
	}
	
	public Type(){
		this.typeName = withoutType;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public boolean hasType(){
		return (!typeName.equals(withoutType));
	}
	
	public boolean equals(Type t){
		return this.typeName.equals(t.typeName);
	}
}
