package constants;

public enum Element {
	//Ja, Jb, Jc, Jbc, Jac, Jab, Da, Db, Dc, spin
	Ni(0, 0.67, -0.05, 1.04, -0.11, 0.30, 0.339, 1.82, 0, 1.0), // Saturated magnetic moment of Ni-ions: 2.2 mu_B
	Co(0, 0.06935, 0.1858, 0.46, -0.07, 0, 2, 0, 2, 1.5), // Saturated magnetic moment of Co-ions, according to Kharchenko : 3.6 mu_B
	Fe(0, 0.3, 0.14, 0.77, 0.05, 0.14, 1.62, 0, 0.56, 2.0), // Saturated magnetic moment of Fe-ions, according to Gan Liang et al. : (5.43,5.22,4.95)
	TestCoup(0, 0, 0, -1, 0, 0, 0.0, 0.0, 0.0, 1.0), // For testing coupling
	TestAni(0, 0, 0, 0, 0, 0, 0.0, 0.0, 1.0, 1.0); // For testing anisotropy
	//      Ja, Jb, Jc, Jbc, Jac, Jab [Da, Db, Dc]: 
	// Co: [0, 0.23, 0, 0.46, 0, 0], [0, 0, 0] from Ellen 2017
	// Co: [0, 0.105, 0.194, 0.743, -0.163, -0.181], [0.718, 0, 0.802] from Wei Tian 2008
	
	// Co: [0.0453, 0.0549, 0, 0.3, -0.09, -0.1183], [2.0277, 0, 0.8576] from Wenjie Wan 2018 With DM interaction
	
	// Co: [0.0446, 0.0539, 0, 0.3, -0.0900, -0.1183], [2.1726, 0, 1.2518] from Wenjie Wan 2018 Without DM interaction
	// Co: [0, 0.06935, 0.1858, 0.45937, 0, 0], [None given] from Kharchenko 2010
	// Co: [] D = 2.5 meV => [D*2/3, -D*1/3, -D*1/3]=[1.66, -0.833, -0.833], from Vaknin et al 2002
	// Co neel temperature = 21.6 K, which gives that all constants must be < 0.827 meV
	// (0.5828, 0.3646, 0.2541)
	
	public double spin;
	public double Ja;
	public double Jb;
	public double Jc;
	public double Jbc;
	public double Jac;
	public double Jab;
	
	public double[] Jlist;
	
	public double Dx;
	public double Dy;
	public double Dz;
	
	Element(double... constants){
		Ja = constants[0]; 
		Jb = constants[1];
		Jc = constants[2];
		Jbc = constants[3];
		Jac = constants[4];
		Jab = constants[5];
		Jlist = new double[] {Ja,Jb,Jc,Jbc,Jac,Jab};
		Dx = constants[6];
		Dy = constants[7];
		Dz = constants[8];
		
		spin = constants[9];
	}
	
	public double getCoupling(Element at, int n) {
		return (this.Jlist[n] + at.Jlist[n])/2; //this.paramList + at.paramList;
	}

	public String paramString() {
		String s = "";
		s += "[ " + Ja + ", " + Jb + ", " + Jc + ", " + Jbc + ", " + Jac + ", " + Jab + "], ";
		s += "[" + Dx + ", " + Dy + ", " + Dz + "], ";
		s += "Spin: " + spin;
		return s;
	}
	
	int getId() {
		switch (this) {
		case Ni:
			return 0;
		case Co:
			return 1;
		case TestCoup:
			return 2;
		default:
			return -1;
		}
	}
}
