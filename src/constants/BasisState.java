package constants;

import main.MyVector;

public enum BasisState {

	// Commensurate basis structures:
	/*
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
	*/

	Ax ( new int[] {1, 1, -1, -1}, 0),
	Cx ( new int[] {1, -1, 1, -1}, 0),
	Gx ( new int[] {1, -1, -1, 1}, 0),
	Fx ( new int[] {1, 1, 1, 1}, 0),
	
	Ay (Ax),
	Cy (Cx),
	Gy (Gx),
	Fy (Fx),

	Az (Ay),
	Cz (Cy),
	Gz (Gy),
	Fz (Fy);
	
	public final int[][][] basisSpins;
	public final int coord;
	
	private final static int[][] indices = new int[][] {
		{0,0,0},{0,1,1},{1,0,1},{1,1,0}
	};

	private int[] asList() {
		int[] newSpins = new int[4];
		for (int i = 0; i < indices.length; i++) {
			int[] index = indices[i];
			newSpins[i] = basisSpins[index[0]][index[1]][index[2]];
		}
		return newSpins;
		
//		return new MyVector[] { basisSpins[0][0][0], basisSpins[0][1][1], basisSpins[1][0][1], basisSpins[1][1][0] };
	}
	
	public int index(int i) {
		return this.asList()[i];
	}
	
	public int getSpin(int[] index) {
		int[] localIndex = norm(index);
		return basisSpins[localIndex[0]][localIndex[1]][localIndex[2]];
	}
	
	public static BasisState getState(int state, int coord) {
		switch(state) {
		case 0:
			switch(coord) {
			case 0:
				return Fx;
			case 1:
				return Fy;
			case 2:
				return Fz;
			}
		case 1:
			switch(coord) {
			case 0:
				return Cx;
			case 1:
				return Cy;
			case 2:
				return Cz;
			}
		case 2:
			switch(coord) {
			case 0:
				return Ax;
			case 1:
				return Ay;
			case 2:
				return Az;
			}
		case 3:
			switch(coord) {
			case 0:
				return Gx;
			case 1:
				return Gy;
			case 2:
				return Gz;
			}
		}
		throw new ArrayIndexOutOfBoundsException("The given base state does not exist.");
	}

	private BasisState(int[] spins, int coord) {
		basisSpins = new int[2][2][2];
		basisSpins[0][0][0] = spins[0];
		basisSpins[0][1][1] = spins[1];
		basisSpins[1][0][1] = spins[2];
		basisSpins[1][1][0] = spins[3];
		this.coord = coord;
	}
	
	private BasisState(BasisState p) {
		this.basisSpins = p.basisSpins;
		this.coord = (p.coord+1) % 3;
	}
	
	public double projOnBasis(int[] index, MyVector spin) {
		int[] newIndex = norm(index);
		return spin.getCoord(this.coord)*getSpin(newIndex);
	}

	public static int[] norm(int[] index) {
		int[] newIndex = new int[3];		
		newIndex[0] = index[0] % 2;
		newIndex[1] = index[1] % 2;
		newIndex[2] = index[2] % 2;
		return newIndex;
	}
	
//	private static int[][][] permute(BasisStateAlt state) {
//		final int[][][] out = new int[2][2][2]; 
//		for (int i = 0; i < indices.length ; i++) {
//			int[] index = indices[i];
//			int spin = state.getSpin(index);
//			out[index[0]][index[1]][index[2]] = new MyVector(spin.z, spin.x, spin.y);
//		}
//		return out;
//	}

}
