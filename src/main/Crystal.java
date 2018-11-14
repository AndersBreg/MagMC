package main;

public enum Crystal {

	SC ( new MyVector[] { 
			new MyVector(0, 0, 0) } ),
	BCC ( new MyVector[] { 
			new MyVector(0.0f, 0.0f, 0.0f), new MyVector(0.5f, 0.5f, 0.5f)} ),
	FCC ( new MyVector[] { 
			new MyVector(0.0f, 0.0f, 0.0f), new MyVector(0.0f, 0.5f, 0.5f), 
			new MyVector(0.5f, 0.0f, 0.5f), new MyVector(0.5f, 0.5f, 0.0f) });
	
	// Commensurate basis structures:
	public static final MyVector[] Ax = new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f),
			new MyVector(-1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f) };
	public static final MyVector[] Cx = new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f),
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f) };
	public static final MyVector[] Gx = new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f),
			new MyVector(-1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f) };
	public static final MyVector[] Fx = new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f),
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f) };
	
	public static final MyVector[] Ay = permute(Ax);
	public static final MyVector[] Cy = permute(Cx);
	public static final MyVector[] Gy = permute(Gx);
	public static final MyVector[] Fy = permute(Fx);

	public static final MyVector[] Az = permute(Ay);
	public static final MyVector[] Cz = permute(Cy);
	public static final MyVector[] Gz = permute(Gy);
	public static final MyVector[] Fz = permute(Fy);

	private final MyVector[] basis;
	private final int nBasis;
	
	Crystal(MyVector[] basis){
		this.basis = basis;
		this.nBasis = basis.length;
	}
	
	public MyVector[] get(){
		return basis;
	}
	
	public boolean isValid(int x, int y, int z) {
		if (this == FCC && (x + y + z) % 2 == 0) {
			return true;
		} else if (this == SC) {
			return false;
		}
		return false;
	}

	public boolean isValid(int[] X) {
		return isValid(X[0], X[1], X[2]);
	}
	
	public int[][] getBasisNB() {
		return new int[][] {{0,0,0},{0,1,1},{1,0,1},{1,1,0}};
	}

	public int[][][] getNNIndices() {
		switch(this) {
			case FCC:
				return new int[][][] {
					{{2,0,0},{-2,0,0}},
					{{0,2,0},{0,-2,0}},
					{{0,0,2},{0,0,-2}},
					{{0,1,1},{0,-1,1},{0,-1,-1},{0,1,-1}},
					{{1,0,1},{1,0,-1},{-1,0,-1},{-1,0,1}},
					{{1,1,0},{-1,1,0},{-1,-1,0},{1,-1,0}}
				};
			case BCC:
				return new int[][][] {
					{{0,1,1},{0,-1,1}},
					{{1,0,1},{}},
					{{1,1,0},{}}
				};
			case SC:
				return new int[][][] {
					{{1,0,0},{-1,0,0}},
					{{0,1,0},{0,-1,0}},
					{{0,0,1},{0,0,-1}}
				};	
		}
		return null;
	}
	
	private static MyVector[] permute(MyVector[] vecList) {
		MyVector[] out = new MyVector[vecList.length];
		for (int i = 0; i < vecList.length; i++) {
			out[i] = new MyVector(vecList[i].z, vecList[i].x, vecList[i].y);
		}
		return out;
	}

}
