package constants;

import main.MyVector;

public enum Crystal {

	SC ( new MyVector[] { 
			new MyVector(0, 0, 0) }, 
		new int[][][] {
			{{1,0,0},{-1,0,0}},
			{{0,1,0},{0,-1,0}},
			{{0,0,1},{0,0,-1}}
		}
	),
	BCC ( new MyVector[] { 
			new MyVector(0.0f, 0.0f, 0.0f), //
			new MyVector(0.5f, 0.5f, 0.5f)
			},
		new int[][][] {
			{{2,0,0},{-2,0,0}},
			{{0,2,0},{0,-2,0}},
			{{0,0,2},{0,0,-2}},
			{{1,1,1},{1,1,-1},{1,-1,1},{-1,1,1},{1,-1,-1},{-1,1,-1},{-1,-1,1},{-1,-1,-1}},
		}
	),
	FCC(new MyVector[] { //
			new MyVector(0.0f, 0.0f, 0.0f), new MyVector(0.0f, 0.5f, 0.5f), //
			new MyVector(0.5f, 0.0f, 0.5f),	new MyVector(0.5f, 0.5f, 0.0f) 
		}, //
		new int[][][] { 
			{ { 2, 0, 0 }, { -2, 0, 0 } }, 
			{ { 0, 2, 0 }, { 0, -2, 0 } }, 
			{ { 0, 0, 2 }, { 0, 0, -2 } },
			{ { 0, 1, 1 }, { 0, -1, 1 }, { 0, -1, -1 }, { 0, 1, -1 } },
			{ { 1, 0, 1 }, { 1, 0, -1 }, { -1, 0, -1 }, { -1, 0, 1 } },
			{ { 1, 1, 0 }, { -1, 1, 0 }, { -1, -1, 0 }, { 1, -1, 0 } } 
		}
	);
	
	private final MyVector[] basis;
	private final int nBasis;
	public final int[][][] neighbours;
	
	Crystal(MyVector[] basis, int[][][] neighbours){
		this.basis = basis;
		this.nBasis = basis.length;
		this.neighbours = neighbours;
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

	public final int[][][] getNNIndices() {
		return neighbours;
	}
	
	private static MyVector[] permute(MyVector[] vecList) {
		MyVector[] out = new MyVector[vecList.length];
		for (int i = 0; i < vecList.length; i++) {
			out[i] = new MyVector(vecList[i].z, vecList[i].x, vecList[i].y);
		}
		return out;
	}

}
