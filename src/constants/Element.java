package constants;

import java.util.Arrays;

public enum Element {
	//Ja, Jb, Jc, Jbc, Jac, Jab, Da, Db, Dc, spin
	Ni(0, 0.67, -0.05, 1.04, -0.11, 0.30, 0.339, 1.82, 0, 1.0), // Saturated magnetic moment of Ni-ions: 2.2 mu_B
	Co(0, 0.06935, 0.1858, 0.46, -0.07, 0, 1, 0, 1, 1.5), // Saturated magnetic moment of Co-ions, according to Kharchenko : 3.6 mu_B
	Fe(0, 0.3, 0.14, 0.77, 0.05, 0.14, 1.62, 0, 0.56, 2.0), // Saturated magnetic moment of Fe-ions, according to Gan Liang et al. : (5.43,5.22,4.95)
	TestCoup(0, 0, 0, -1, 0, 0, 0.0, 0.0, 0.0, 1.0), // For testing coupling
	TestAni(0, 0, 0, 0, 0, 0, 0.0, 0.0, 1.0, 1.0); // For testing anisotropy
	// Co: [0, 0.06935, 0.1858, 0.46, -0.07, 0], [1, 0, 1], 1.5 from my own experience, this work.
	//      Ja, Jb, Jc, Jbc, Jac, Jab [Da, Db, Dc]: 
	// Co: [0, 0.23, 0, 0.46, 0, 0], [0, 0, 0] from Ellen 2017
	
	// Co: [0.0453, 0.0549, 0, 0.3, -0.09, -0.1183], [2.0277, 0, 0.8576] from Wenjie Wan 2018 With DM interaction
	
	// Co: [0.0446, 0.0539, 0, 0.3, -0.0900, -0.1183], [2.1726, 0, 1.2518] from Wenjie Wan 2018 Without DM interaction
	// Co: [0, 0.06935, 0.1858, 0.45937, 0, 0], [None given] from Kharchenko 2010
	// Co: [] D = 2.5 meV => [D*2/3, -D*1/3, -D*1/3]=[1.66, -0.833, -0.833], from Vaknin et al 2002
	// Co: [0, 0.105, 0.194, 0.743, -0.163, -0.181], [0.718, 0, 0.802] from Wei Tian 2008, Was determined to be a mixture of Co and Ni.
	
	// Ni: [0, 0.67, -0.05, 1.04, -0.11, 0.30], [0.339, 1.82, 0] 
	// General information:
	// Co neel temperature = 21.6 K, which gives that all constants must be < 1.827 meV <- Is not true
	// (0.5828, 0.3646, 0.2541)
		
	
	public double spin;
//	private double Ja;
//	private double Jb;
//	private double Jc;
//	private double Jbc;
//	private double Jac;
//	private double Jab;
	
	public double[] Jlist;
	
	public double Dx;
	public double Dy;
	public double Dz;
	
	Element(double... constants){
		this.set(constants);
	}
	
	private void set(double[] constants) {
//		Ja = constants[0];
//		Jb = constants[1];
//		Jc = constants[2];
//		Jbc = constants[3];
//		Jac = constants[4];
//		Jab = constants[5];
		Jlist = Arrays.copyOfRange(constants, 0, 6);
//		Jlist = new double[] {Ja,Jb,Jc,Jbc,Jac,Jab};
		
		Dx = constants[6];
		Dy = constants[7];
		Dz = constants[8];
		
		spin = constants[9];
	}

	public void modify(int param, double value) {
		switch(param) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			this.Jlist[param] = value;
			break;
		case 6:
			this.Dx = value;
			break;
		case 7:
			this.Dy = value;
			break;
		case 8:
			this.Dz = value;
			break;
		}
	}
	
	public double getCoupling(Element at, int n) {
		return (this.Jlist[n] + at.Jlist[n])/2; //this.paramList + at.paramList;
	}

	public String paramString() {
		String s = "";
		s += "[ " + Jlist[0] + ", " + Jlist[1] + ", " + Jlist[2] + ", " + Jlist[3] + ", " + Jlist[4] + ", " + Jlist[5] + "], ";
		s += "[" + Dx + ", " + Dy + ", " + Dz + "], ";
		s += "Spin: " + spin;
		return s;
	}
}
