package constants;

import main.MyVector;
import math.Complex;

public enum BasisStateIncom {
	
	
	Ax ( new double[] {0, 0, 1, 1}, 0),
	Cx ( new double[] {0, 1, 0, 1}, 0),
	Gx ( new double[] {0, 1, 1, 0}, 0),
	Fx ( new double[] {0, 0, 0, 0}, 0),
	
	Ay (Ax),
	Cy (Cx),
	Gy (Gx),
	Fy (Fx),

	Az (Ay),
	Cz (Cy),
	Gz (Gy),
	Fz (Fy);
	
	public final double[][][] basisSpins;
	public final int coord;
	
	private final static int[][] indices = new int[][] {
		{0,0,0},{0,1,1},{1,0,1},{1,1,0}
	};

	private double[] asList() {
		double[] newSpins = new double[4];
		for (int i = 0; i < indices.length; i++) {
			int[] index = indices[i];
			newSpins[i] = basisSpins[index[0]][index[1]][index[2]];
		}
		return newSpins;
		
//		return new MyVector[] { basisSpins[0][0][0], basisSpins[0][1][1], basisSpins[1][0][1], basisSpins[1][1][0] };
	}
	
	public double index(int i) {
		return this.asList()[i];
	}
	
	public double getSpin(int[] index) {
		int[] localIndex = norm(index);
		return basisSpins[localIndex[0]][localIndex[1]][localIndex[2]];
	}
	
	public static BasisStateIncom getState(int state, int coord) {
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

	private BasisStateIncom(double[] spins, int coord) {
		basisSpins = new double[2][2][2];
		basisSpins[0][0][0] = spins[0];
		basisSpins[0][1][1] = spins[1];
		basisSpins[1][0][1] = spins[2];
		basisSpins[1][1][0] = spins[3];
		this.coord = coord;
	}

	private BasisStateIncom(BasisStateIncom p) {
		this.basisSpins = p.basisSpins;
		this.coord = (p.coord+1) % 3;
	}
	
	public double projOnBasis(int[] index, MyVector spin) {
		// TODO Check these for correctness
		int[] newIndex = norm(index);
		double factor = (Complex.exp(new Complex(0, Math.PI * this.getSpin(newIndex)))).re;
		return factor*spin.getCoord(this.coord);
	}
	
	public Complex projOnBasisIncom(int[] index, MyVector spin, double deltaK) {
		// TODO Check these for correctness
		int[] newIndex = norm(index);
		Complex factor = Complex.exp(new Complex(0, Math.PI * (this.getSpin(newIndex) + deltaK)));
		return factor.mult(spin.getCoord(this.coord));
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
