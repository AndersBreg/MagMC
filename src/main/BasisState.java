package main;

import processing.core.PVector;

public enum BasisState {
	Ax (new PVector[] { new PVector(1.0f, 0.0f, 0.0f), new PVector(1.0f, 0.0f, 0.0f),
			new PVector(-1.0f, 0.0f, 0.0f), new PVector(-1.0f, 0.0f, 0.0f) } ),
	Cx ( new PVector[] { new PVector(1.0f, 0.0f, 0.0f), new PVector(-1.0f, 0.0f, 0.0f),
			new PVector(1.0f, 0.0f, 0.0f), new PVector(-1.0f, 0.0f, 0.0f) } ),
	Gx ( new PVector[] { new PVector(1.0f, 0.0f, 0.0f), new PVector(-1.0f, 0.0f, 0.0f),
			new PVector(-1.0f, 0.0f, 0.0f), new PVector(1.0f, 0.0f, 0.0f) } ),
	Fx ( new PVector[] { new PVector(1.0f, 0.0f, 0.0f), new PVector(1.0f, 0.0f, 0.0f),
			new PVector(1.0f, 0.0f, 0.0f), new PVector(1.0f, 0.0f, 0.0f) } ),
	
	Ay (Ax),
	Cy (Cx),
	Gy (Gx),
	Fy (Fx),

	Az (Ay),
	Cz (Cy),
	Gz (Gy),
	Fz (Fy);
	
	private final PVector[] basis;
	
	private BasisState(PVector[] p) {
		basis = p;
	}
	private BasisState(BasisState p) {
		basis = permute(p.basis);
	}
	
	private static PVector[] permute(PVector[] ax2) {
		PVector[] out = new PVector[ax2.length];
		for (int i = 0; i < ax2.length; i++) {
			out[i] = new PVector(ax2[i].z, ax2[i].x, ax2[i].y);
		}
		return out;
	}

}
