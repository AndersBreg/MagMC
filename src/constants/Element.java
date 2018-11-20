package constants;

public enum Element {
	//Ja, Jb, Jc, Jbc, Jac, Jab, Da, Db, Dc, spin
	Ni(0, 0.67, -0.05, 1.04, -0.11, 0.30, 0.339, 1.82, 0, 1.0),
	Co(0, 0.105, 0.194, 0.743, -0.163, -0.181, 0.718, 0, 0.802, 1.5),
	Fe(0, 0.3, 0.14, 0.77, 0.05, 0.14, 1.62, 0, 0.56, 2),
	Test(0, 0, 0, -1, 0, 0, 0.0, 0.0, -1.0, 1.0); // Without DM interactions
	//      Ja, Jb, Jc, Jbc, Jac, Jab [Da, Db, Dc]: 
	// Co: [0, -0.23, 0, -0.46, 0, 0], [0, 0, 0] from Ellen 2017
	// Co: [0, 0.105, 0.194, 0.743, -0.163, -0.181], [0.718, 0, 0.802] from Wei Tian 2008
	
	// Co: [0.0453, 0.0549, 0, 0.3, -0.09, -0.1183], [2.17, 0, 1.25] from Wenjie Wan 2018 With DM interaction
	
	// Co: [0.0446, 0.0539, 0, 0.3, -0.0900, -0.1183], [2.1726, 0, 1.2518] from Wenjie Wan 2018 Without DM interaction
	// Co: [0,-0.06935, -0.1858, -0.45937, 0, 0], [None given] from Kharchenko 2010
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

	public String toString() {
		String s = "";
		s += "[ " + Ja + ", " + Jb + ", " + Jc + ", " + Jbc + ", " + Jac + ", " + Jab + "], ";
		s += "[" + Dx + ", " + Dy + ", " + Dz + "]";
		return s;
	}
	
	int getId() {
		switch (this) {
		case Ni:
			return 0;
		case Co:
			return 1;
		case Test:
			return 2;
		default:
			return -1;
		}
	}
}
