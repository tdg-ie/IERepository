package constantManagement;

import data.Constant;

public class ConstantFrequency{

	private Constant c;
	private float freq;
	
	public ConstantFrequency(Constant c, float freq) {
		this.c = c;
		this.freq = freq;
	}

	public Constant getConstant() {
		return c;
	}

	public float getFreq() {
		return freq;
	}
	
//	@Override
//	public int compareTo(Float arg0) {
//		int result = -1;
//		if (this.freq > arg0)
//			result = 1;
//		else if (this.freq == arg0)
//			result = 0;
//		return result;
//	}
}