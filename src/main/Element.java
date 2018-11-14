package main;

public enum Element {
	Ni(0, 0.67, -0.05, 1.04, -0.11, 0.3, 0.339, 1.82, 0, 1.0), 
	Co(0.04, 0.05, 0, 0.35, -0.0728, -0.1131, 2.0277, 0, 0.8576, 1.5),
	Test(0, 0, 0, 0, 0, 0, 0, 0, 0.0, 1.0); // Without DM interactions

	// (0.5828, 0.3646, 0.2541)
	
	double spin;
	double Ja;
	double Jb;
	double Jc;
	double Jbc;
	double Jac;
	double Jab;
	
	double[] Jlist;
	
	double Dx;
	double Dy;
	double Dz;
	
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
	
	double getCoupling(Element at, int n) {
		return (this.Jlist[n] + at.Jlist[n])/2; //this.paramList + at.paramList;
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
