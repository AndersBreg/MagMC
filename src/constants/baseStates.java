package constants;

import main.MyVector;

public enum baseStates {

	
	Ax(new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f),
			new MyVector(-1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f) }),
	Gx(new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f),
			new MyVector(-1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f) }),
	Cx(new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f),
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(-1.0f, 0.0f, 0.0f) }),
	Fx(new MyVector[] { 
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f),
			new MyVector(1.0f, 0.0f, 0.0f), new MyVector(1.0f, 0.0f, 0.0f) });
	
	private MyVector[][][] basisSpins = new MyVector[2][2][2];
	
	baseStates(MyVector[] spins){
		basisSpins[0][0][0] = spins[0];
		basisSpins[0][1][1] = spins[1];
		basisSpins[1][0][1] = spins[2];
		basisSpins[1][1][0] = spins[3];
	}
	
	public double projOn(int[] index, MyVector spin){
//		new int[] newIndex = new int[] {index[0] % 2, index[1] % 2, index[2] % 2};
		return spin.dot(this.basisSpins[index[0] % 2][index[1] % 2][index[2] % 2]);
	}
}
