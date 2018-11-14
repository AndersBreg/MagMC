package constants;

import main.MyVector;

public enum BasisState {
	Ax (new MyVector[] { new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f),
			new MyVector(-1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f) } ),
	Cx ( new MyVector[] { new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f),
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f) } ),
	Gx ( new MyVector[] { new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f),
			new MyVector(-1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f) } ),
	Fx ( new MyVector[] { new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f),
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f) } ),
	
	Ay (Ax),
	Cy (Cx),
	Gy (Gx),
	Fy (Fx),

	Az (Ay),
	Cz (Cy),
	Gz (Gy),
	Fz (Fy);
	
	private final MyVector[] basis;
	
	private BasisState(MyVector[] p) {
		basis = p;
	}
	private BasisState(BasisState p) {
		basis = permute(p.basis);
	}
	
	private static MyVector[] permute(MyVector[] ax2) {
		MyVector[] out = new MyVector[ax2.length];
		for (int i = 0; i < ax2.length; i++) {
			out[i] = new MyVector(ax2[i].z, ax2[i].x, ax2[i].y);
		}
		return out;
	}

}
