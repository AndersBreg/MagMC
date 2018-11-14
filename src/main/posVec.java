package main;

public class posVec {

	public int index;
	public int X;
	public int Y;
	public int Z;
	public int basis;
	public Simulator_new parent;
	
	public posVec(int x, int y, int z, int basis) {	
		X = 2*x;
		Y = 2*y;
		Z = 2*z;
		this.basis = basis;
	}
	
	public void add(int basis) {
		
	}
	
	public int[] toCoord() {
		return new int[] {X, Y, Z};
	}
	public int toIndex() {
			switch(0) {
			case 0:
				return (((2*X + parent.param.nX) % parent.param.nX) + 
						((2*Y + parent.param.nY) % parent.param.nY) * parent.param.nX + 
						((2*Z + parent.param.nZ) % parent.param.nZ) * parent.param.nX * parent.param.nY) % parent.nAtoms;
			case 1:
				return (((2*X + parent.param.nX) % parent.param.nX) + 
						((2*Y + 1 + parent.param.nY) % parent.param.nY) * parent.param.nX + 
						((2*Z + 1 + parent.param.nZ) % parent.param.nZ) * parent.param.nX * parent.param.nY) % parent.nAtoms;
			case 2:
				return (((2*X + 1 + parent.param.nX) % parent.param.nX) + 
						((2*Y + parent.param.nY) % parent.param.nY) * parent.param.nX + 
						((2*Z + 1 + parent.param.nZ) % parent.param.nZ) * parent.param.nX * parent.param.nY) % parent.nAtoms;
			case 3:
				return (((2*X + 1 + parent.param.nX) % parent.param.nX) + 
						((2*Y + 1 + parent.param.nY) % parent.param.nY) * parent.param.nX + 
						((2*Z + parent.param.nZ) % parent.param.nZ) * parent.param.nX * parent.param.nY) % parent.nAtoms;
			}
			return (((2*X + parent.param.nX) % parent.param.nX) + 
					((2*Y + parent.param.nY) % parent.param.nY) * parent.param.nX + 
					((2*Z + parent.param.nZ) % parent.param.nZ) * parent.param.nX * parent.param.nY) % parent.nAtoms;

	}
	
	public int[] getNeighbours() {
		int[] list = new int[6];
		list[0] = (new posVec(this.X, this.Y, this.Z, 0)).toIndex();
		
		return list;
	}
}
