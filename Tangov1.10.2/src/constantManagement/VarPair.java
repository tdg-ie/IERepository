package constantManagement;

import java.util.List;

import data.Variable;

public class VarPair {

	private Variable var1;
	private Variable var2;
	private int sign;
	
	public VarPair(Variable var1, Variable var2, int sign){
		this.var1 = var1;
		this.var2 = var2;
		this.sign = sign; // sign can be 0 (means equal), 1 (firstarg < secondArg), 2 (firstArg > secondArg).
	}

	public Variable getVar1() {
		return var1;
	}

	public Variable getVar2() {
		return var2;
	}
	
	public boolean equals(VarPair vp) {
		boolean result = false;
		boolean cond1 = vp.var1.equals(this.var1) && vp.var2.equals(this.var2) && this.sign == vp.sign;
		boolean cond2 = (vp.var1.equals(this.var2) && vp.var2.equals(this.var1) && ((this.sign == vp.sign && this.sign == 0) || (vp.sign != 0 && (this.sign == (vp.sign == 1? 2:1)))));
		
		if (cond1 || cond2)
			result = true;
		return result;
	}
	
	public boolean belongs(List<VarPair> lvp) {
		boolean result = false;
		int i = 0;
		while (i < lvp.size() && !result) {
			VarPair vp = lvp.get(i);
			if (this.equals(vp)) 
				result = true;
			i++;
		}
		return result;
	}

	public int getSign() {
		return sign;
	}

	public void setSign(int sign) {
		this.sign = sign;
	}
}