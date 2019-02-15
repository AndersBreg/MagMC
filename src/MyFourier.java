import java.util.Iterator;

import constants.BasisState;
import main.MyVector;
import main.Simulator;

import math.Complex;

public class MyFourier {
	
//	public Simulator sim;
//	
//	public int coord = 2;
//	
//	public double projOnBasis(int[] index, MyVector spin) {
//		int[] newIndex = BasisState.norm(index);
//		return spin.getCoord(this.coord) * getSpin(newIndex);
//	}
//	
//	public double Fourier(int[] vec, int stateIndex, int coord) {
//		double[][][] output;
//		
//		Iterator<int[]> it = sim.iterateUnitCells();
//		int[][] basis = sim.crys.getBasisNB();
//		BasisState basisState = BasisState.getState(stateIndex, coord);
//		while (it.hasNext()) {
//			int[] cellIndex = it.next();
//			double k = 0;
//			Complex phi = new Complex(0, Math.PI * k);
//			for (int i = 0; i < basis.length; i++) {
//				int[] atomIndex = sim.addIndex(basis[i], cellIndex);
//				int dir = basisState.getSpin(atomIndex);
//				if (dir == -1) {
//					
//				}
//				MyVector spin = sim.getSpinDirection(atomIndex);
//				basisState.projOnBasis(atomIndex, spin)*Complex.exp();
//			}
//		}
//		return coord;
//	}
}