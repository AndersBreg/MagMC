package main;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import constants.*;
import math.Complex;

public class Simulator implements Runnable {

	/*
	 * Ni lat. const. = [10.02, 5.86, 4.68] Å; From Phys. Rev. 092413 (2009) 
	 * Ni lat. const. = [10.02, 5.83, 4.66] Å; From Phys. Rev. 054408 (2011); 
	 * Phys. Rev. 064421 (2017) and Phys. Rev. 064421 (2017) 
	 * Co lat. const. = [10.20, 5.92, 4.70] Å; From Phys. Rev. 104420 (2017) 
	 * Co lat. const. = [10.159, 5.9, 4.70]
	 * Å; From Phys. Rev. 104420 (2017)
	 */
	private static final String PREC = "%1.8e";

	public static final double a = 10.20f;
	public static final double b = 5.92f;
	public static final double c = 4.70f;
	public static final float scaling = 1;

	private static final double invBoltz = 11.6045; // inverse of the Boltzmann constant in units K / meV
	private static final double muB = 0.05788382; // mu_B the Bohr_magneton in units meV/T
	private static final double gx = 2; // the g-factor of the electron
	private static final double gy = 2; // the g-factor of the electron
	private static final double gz = 2; // the g-factor of the electron
	public Crystal crys = Crystal.FCC;
	public MyVector[] basis = crys.get();
	public int nBasis = basis.length;

	public int nAtoms;
	public Element[][][] atomType;
	public MyVector[][][] spins;
	public MyVector[][][] positions;
	public int step = 0;

	/**
	 * Parameter values: #steps, nX, nY, nZ
	 */
	public Parameters param;
	public List<Variables> varList;
	public Variables curVar;
	public int progress;

	// Output values
	public double currentEnergySingle;
	private double energyTotal;

	private double energy_Sum = 0;
	private double energy_SumSq = 0;
	private double energy_SumCube = 0;
//	private double energy_SumQuad = 0;
	private double[][] curBaseProj = new double[4][3];
	private double[][] baseProj_Sum = new double[4][3];
	private double[][] baseProj_SumSq = new double[4][3];
	private double[][] baseProj_SumQuad = new double[4][3];

	private ArrayList<Double> energyMeans;
	private ArrayList<Double[][]> baseProjMeans;

	private Random rand;

	private MyVector mid;
	// public File outputFile;
	public PrintStream output;

	private double delta;
	public long nReject = 0;
	public long nAccept = 0;

	private long seed = 0;

	private final boolean periodicBoundaries;

	/**
	 * Parameter values: #steps, nX, nY, nZ, Temperature, Hx, Hy, Hz
	 * 
	 * @throws FileAlreadyExistsException
	 */
	public Simulator(Parameters newParam, List<Variables> newVarList, PrintStream out)
			throws FileAlreadyExistsException {
		this.param = newParam;
		this.varList = newVarList;
		this.periodicBoundaries = true;
		setup();
		output = out;
//		System.out.println("No configuration specified, generates new.");
		newConfig();
	}
	public Simulator(Parameters newParam, List<Variables> newVarList, PrintStream out, boolean periodic)
			throws FileAlreadyExistsException {
		this.param = newParam;
		this.varList = newVarList;
		this.periodicBoundaries = periodic;
		setup();
		output = out;
//		System.out.println("No configuration specified, generates new.");
		newConfig();
	}

	public Simulator(Parameters newParam, List<Variables> newVarList, PrintStream out, File configFile)
			throws IOException {
		this.param = newParam;
		this.varList = newVarList;
		this.periodicBoundaries = true;
		setup();
		output = out;
		loadConfig(configFile);
	}
	
	public Simulator(Parameters newParam, List<Variables> newVarList, PrintStream out, File configFile, boolean periodic)
			throws IOException {
		this.param = newParam;
		this.varList = newVarList;
		this.periodicBoundaries = periodic;
		setup();
		output = out;
		loadConfig(configFile);
	}

//	public Simulator() {
//		this.param = new Parameters();
//		setup();
//	}

	public void setup() {
		nAtoms = param.nX * param.nY * param.nZ * nBasis;

		positions = new MyVector[2 * param.nX][2 * param.nY][2 * param.nZ];
		atomType = new Element[2 * param.nX][2 * param.nY][2 * param.nZ];
		spins = new MyVector[2 * param.nX][2 * param.nY][2 * param.nZ];

		if (seed != 0) {
			rand = new Random(seed);
		} else {
			rand = new Random();
		}

		mid = new MyVector(param.nX * .5, param.nY * .5, param.nZ * .5);
		mid = scaleVec(a, b, c, mid);
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = it.next();
			index = normIndex(index);
			MyVector position = new MyVector(index[0], index[1], index[2]);
			position = scaleVec(a / 2, b / 2, c / 2, position);
			positions[index[0]][index[1]][index[2]] = position.sub(mid).mult(scaling);
		}

		delta = 0.4;// + 0.6 * Math.tanh(param.temp - 12);
	}

	@Override
	public void run() {

		PrintStream out = new PrintStream(output);
		printParam(out);
		out.println("Output: ");
		out.println("Temp, Bx, By, Bz, E, E_sq, " + //
		"Fx, Fx_sq, Fx_quad, Fy, Fy_sq, Fy_quad, Fz, Fz_sq, Fz_quad, " + // 
		"Cx, Cx_sq, Cx_quad, Cy, Cy_sq, Cy_quad, Cz, Cz_sq, Cz_quad, " + //
		"Ax, Ax_sq, Ax_quad, Ay, Ay_sq, Ay_quad, Az, Az_sq, Az_quad, " + //
		"Gx, Gx_sq, Gx_quad, Gy, Gy_sq, Gy_quad, Gz, Gz_sq, Gz_quad, rejects, accepts");

		progress = 0;
		
		for (Iterator<Variables> iterator = varList.iterator(); iterator.hasNext();) {
			nReject = 0;
			nAccept = 0;
			curVar = (Variables) iterator.next();
			simulate(out, curVar);
			progress += 1;
		}
	}
	
	private void printParam(PrintStream out) {
		Element[] allElem = Element.values();
		// First line prints parameter names:
		out.println("MonteCarloSteps, nX, nY, nZ, Ni frac, Co frac, Fe frac, Mn frac, Periodic boundaries?, total steps");
//		String[] names = Parameters.paramNames;
//		for (int i = 0; i < names.length; i++) {
//			out.print(names[i] + ", ");
//		}
//		for (int i = 0; i < allElem.length; i++) {
//			out.print(allElem[i].toString() + " frac, ");
//		}
//		out.print("Periodic boundaries?");
//		out.println();

		// On second line prints the parameter values:
		long[] parameters = param.getValues();
		for (int i = 0; i < parameters.length; i++) {
			out.print(parameters[i] + ", ");
		}
		for (int i = 0; i < allElem.length; i++) {
			out.print(getFraction(allElem[i])+", ");
		}
		out.println(periodicBoundaries + ", " + this.param.nSteps);
		//Writes the parameters used for the different elements on the next lines:
		for (int i = 0; i < allElem.length; i++) {
			out.println(allElem[i].toString() + " param: " + allElem[i].paramString());
		}
	}

	private void simulate(PrintStream out, Variables vars) {
		
		currentEnergySingle = calcTotalEnergy(vars);
		energyTotal = calcTotalEnergy(vars);
		curBaseProj = allProjectionsTotal();
		nReject = 0;
		nAccept = 0;
		step = 0;
		while (step < param.nSteps) {
			step += 1;

			iterate(vars);

			try {
				Thread.sleep(0, 0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		flush(out, vars);
	}

	private void flush(PrintStream out, Variables vars) {
		double energy_Mean = energy_Sum / param.nSteps;
		double energy_Sq = energy_SumSq / param.nSteps;
		double energy_Cube = energy_SumCube / param.nSteps;
//		double energy_Quad = (energy_SumQuad / param.nSteps);

		double[][] baseProj_Mean = new double[4][3];
		double[][] baseProj_Sq = new double[4][3];
		double[][] baseProj_Quad = new double[4][3];
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				baseProj_Mean[state][coord] = baseProj_Sum[state][coord] / param.nSteps;
				baseProj_Sq[state][coord] = baseProj_SumSq[state][coord] / param.nSteps;
				baseProj_Quad[state][coord] = baseProj_SumQuad[state][coord] / param.nSteps;
			}
		}

		printVals(out, vars, energy_Mean, energy_Sq, baseProj_Mean, baseProj_Sq, baseProj_Quad, nReject, nAccept);

		energy_Sum = 0;
		energy_SumSq = 0;
		energy_SumCube = 0;
//		energy_SumQuad = 0;
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				baseProj_Sum[state][coord] = 0;
				baseProj_SumSq[state][coord] = 0;
				baseProj_SumQuad[state][coord] = 0;
			}
		}
	}

	private void printVals(PrintStream out, Variables vars, double energy_Mean, double energy_Sq,
			double[][] baseProj_Mean, double[][] baseProj_Sq, double[][] baseProj_Quad, long nReject2, long nAccept2) {
		out.print(String.format(PREC, vars.temp));
		out.print(", ");
		out.print(String.format(PREC, vars.B.x));
		out.print(", ");
		out.print(String.format(PREC, vars.B.y));
		out.print(", ");
		out.print(String.format(PREC, vars.B.z));
		out.print(", ");
		out.print(String.format(PREC, energy_Mean));
		out.print(", ");
		out.print(String.format(PREC, energy_Sq));
		out.print(", ");

		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				out.print(String.format(PREC, baseProj_Mean[state][coord]));
				out.print(", ");
				out.print(String.format(PREC, baseProj_Sq[state][coord]));
				out.print(", ");
				out.print(String.format(PREC, baseProj_Quad[state][coord]));
				out.print(", ");
			}
		}
		out.print(nReject);
		out.print(", ");
		out.print(nAccept);
		out.print(", ");

		out.println();		
	}
	
	private void printVals(PrintStream out, Variables vars, double energy_Mean, double energy_Var,
			double[][] baseProjMean, double[][] baseProjVar, long nReject2, long nAccept2) {

		out.print(String.format(PREC, vars.temp));
		out.print(", ");
		out.print(String.format(PREC, vars.B.x));
		out.print(", ");
		out.print(String.format(PREC, vars.B.y));
		out.print(", ");
		out.print(String.format(PREC, vars.B.z));
		out.print(", ");
		out.print(String.format(PREC, energy_Mean));
		out.print(", ");
		out.print(String.format(PREC, energy_Var));
		out.print(", ");

		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				out.print(String.format(PREC, baseProjMean[state][coord]));
				out.print(", ");
				out.print(String.format(PREC, baseProjVar[state][coord]));
				out.print(", ");
			}
		}
		out.print(nReject);
		out.print(", ");
		out.print(nAccept);
		out.print(", ");

		out.println();
	}

	private void iterate(Variables vars) {

		int[] index = chooseRandomAtom(rand);
		MyVector old = getSpinDirection(index).copy();

		// Before change
		double oldEnergy = calcAtomEnergy(index, vars);
		double[][] projOld = allProjectionsSingle(index);

		// Create a new sample / make a small change / proposal spin:
//		setSpinDirection(index, markovSurface(getSpinDirection(index)));
		setSpinDirection(index, randomTurn(getSpinDirection(index)));
//		setSpinDirection(index, randomVector());

		// Calculate energy and other values with new vector:
		double newEnergy = calcAtomEnergy(index, vars);

		double delE = newEnergy - oldEnergy;
		if (rand.nextDouble() <= Math.min(1, Math.exp(-delE * invBoltz / vars.temp))) {
			currentEnergySingle = currentEnergySingle + delE;
			double[][] projNew = allProjectionsSingle(index);
			updateBaseProj(projOld, projNew);
			nAccept += 1;
		} else {
			setSpinDirection(index, old);
			double[][] projNew = allProjectionsSingle(index);
			updateBaseProj(projOld, projNew);
			nReject += 1;
		}
	}

	/* Energy calculations: */
	private double calcTotalEnergy(Variables vars) {
		double E = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] localIndex = it.next();
			E += calcFieldEnergy(localIndex, vars.B);
			E += calcAniEnergy(localIndex);
			E += calcCouplingEnergy(localIndex);
		}
		return E;
	}

	private double calcAtomEnergy(int[] index, Variables vars) {
		double E = 0;

		// Field energy:
		E += calcFieldEnergy(index, vars.B);

		// Single-ion anisotropy:
		E += calcAniEnergy(index);

		// NN couplings
		E += 2 * calcCouplingEnergy(index);

		return E;
	}

	private double calcCouplingEnergy(int[] indexA) {
		double E = 0;
		MyVector spinA = getSpinDirection(indexA).mult(getAtom(indexA).spin);
		Element atomA = getAtom(indexA);

		int[][][] allNeighbours = crys.getNNIndices().clone();
		for (int nJ = 0; nJ < allNeighbours.length; nJ++) {
			int[][] nb_with_specific_J = allNeighbours[nJ];

			for (int n = 0; n < nb_with_specific_J.length; n++) {
				int[] spec_J_nb = nb_with_specific_J[n];
				if (periodicBoundaries || isInside(addIndex(indexA, spec_J_nb))) {
					int[] indexB = normIndex(addIndex(indexA, spec_J_nb));				
					Element atomB = getAtom(indexB);
					MyVector spinB = getSpinDirection(indexB).mult(getAtom(indexB).spin);
					E += atomA.getCoupling(atomB, nJ) * spinA.dot(spinB) / 2;
				}
			}
		}
		return E;
	}

	private boolean isInside(int[] ind) {
		if (ind[0] < 0 || ind[1] < 0 || ind[2] < 0 || ind[0] >= (2 * param.nX) || ind[1] >= (2 * param.nY)
				|| ind[2] >= (2 * param.nZ)) {
			return false;
		} else {
			return true;
		}
	}

	private double calcFieldEnergy(int[] index, MyVector B) {
		MyVector S = getSpinDirection(index).mult(getAtom(index).spin); // Spin vector S
		MyVector mu = scaleVec(muB * gx, muB * gy, muB * gz, S); // muB in meV/T so mu also in meV/T
		double E = -mu.dot(B); // B in T so E in meV
		return E;
	}

	private double calcAniEnergy(int[] index) {
		double E = 0;
		MyVector S = getSpinDirection(index).mult(getAtom(index).spin);
		E += getAtom(index).Dx * S.x * S.x;
		E += getAtom(index).Dy * S.y * S.y;
		E += getAtom(index).Dz * S.z * S.z;
		return E;
	}

	/* Basis state projections */
	public double projTotal(BasisState basisState) {
		double proj = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] atomIndex = (int[]) it.next();
			proj += projAtom(atomIndex, basisState);
		}
		return proj;
	}

	public double projAtom(int[] index, BasisState basisState) {
		MyVector spin = getSpinDirection(index);
		double proj = basisState.projOnBasis(index, spin);
		return proj / nAtoms;
	}

	public Complex projAtomIncom(int[] index, BasisStateIncom basisState) {
		MyVector spin = getSpinDirection(index);
		Complex proj = new Complex(basisState.projOnBasis(index, spin), 0);
		return proj.mult(1 / nAtoms);
	}

	private void updateBaseProj(double[][] projOld, double[][] projNew) {
		double energyPrAtom = currentEnergySingle / nAtoms;
		energy_Sum += energyPrAtom;
		energy_SumSq += energyPrAtom*energyPrAtom;
		// TODO check that the cube calculation is correct!
		energy_SumCube += energyPrAtom*energyPrAtom*energyPrAtom;
//		energy_SumQuad += Math.pow(currentEnergySingle / nAtoms, 4);

		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				double diff = projNew[state][coord] - projOld[state][coord];
				curBaseProj[state][coord] = curBaseProj[state][coord] + diff;
				// TODO Might try to take the absolute value of this instead
				baseProj_Sum[state][coord] += curBaseProj[state][coord];
				baseProj_SumSq[state][coord] += curBaseProj[state][coord] * curBaseProj[state][coord];
				baseProj_SumQuad[state][coord] += Math.pow(curBaseProj[state][coord], 4);
			}
		}
		// double fX = projBasisStateTotal(Crystal.Fx); // Alternative use
		// calcMagnetization(0);
		// double fY = projBasisStateTotal(Crystal.Fy);
		// double fZ = projBasisStateTotal(Crystal.Fz);
		// double fX = projTotal(BasisStateAlt.Fx );
		// double fY = projTotal(BasisStateAlt.Fy );
		// double fZ = projTotal(BasisStateAlt.Fz );

		// double cX = projBasisStateTotal(Crystal.Cx);
		// double cY = projBasisStateTotal(Crystal.Cy);
		// double cZ = projBasisStateTotal(Crystal.Cz);
		// double cX = projTotal(BasisStateAlt.Cx );
		// double cY = projTotal(BasisStateAlt.Cy );
		// double cZ = projTotal(BasisStateAlt.Cz );

		// double aX = projBasisStateTotal(Crystal.Ax);
		// double aY = projBasisStateTotal(Crystal.Ay);
		// double aZ = projBasisStateTotal(Crystal.Az);
		// double aX = projTotal(BasisStateAlt.Ax);
		// double aY = projTotal(BasisStateAlt.Ay);
		// double aZ = projTotal(BasisStateAlt.Az);

		// double gX = projBasisStateTotal(Crystal.Gx);
		// double gY = projBasisStateTotal(Crystal.Gy);
		// double gZ = projBasisStateTotal(Crystal.Gz);
		// double gX = projTotal(BasisStateAlt.Gx);
		// double gY = projTotal(BasisStateAlt.Gy);
		// double gZ = projTotal(BasisStateAlt.Gz);

		/* Equivalent to baseProjSum and baseProjSumSq */
		// sumFX += fX;
		// sumFY += fY;
		// sumFZ += fZ;
		//
		// sumCX += cX;
		// sumCY += cY;
		// sumCZ += cZ;
		//
		// sumAX += aX;
		// sumAY += aY;
		// sumAZ += aZ;
		//
		// sumGX += gX;
		// sumGY += gY;
		// sumGZ += gZ;
		//
		// sumFX_Sq += fX*fX;
		// sumFY_Sq += fY*fY;
		// sumFZ_Sq += fZ*fZ;
		//
		// sumCX_Sq += cX*cX;
		// sumCY_Sq += cY*cY;
		// sumCZ_Sq += cZ*cZ;
		//
		// sumAX_Sq += aX*aX;
		// sumAY_Sq += aY*aY;
		// sumAZ_Sq += aZ*aZ;
		//
		// sumGX_Sq += gX*gX;
		// sumGY_Sq += gY*gY;
		// sumGZ_Sq += gZ*gZ;
	}

	private double[][] allProjectionsSingle(int[] index) {
		double[][] projections = new double[4][3];
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				projections[state][coord] = projAtom(index, BasisState.getState(state, coord));
			}
		}
		return projections;
	}

	private double[][] allProjectionsTotal() {
		double[][] projections = new double[4][3];
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				projections[state][coord] = projTotal(BasisState.getState(state, coord));
			}
		}
		return projections;
	}

	// // Calculates the mean of an array
	// private static double Mean(List<T> L) {
	// double S = 0;
	// int n = L.size();
	// for (Iterator iterator = L.iterator(); iterator.hasNext();) {
	// double E = (double) iterator.next();
	// S += E;
	// }
	// return (double) (S/n);
	// }
	//
	// // Calculates the mean of an array
	// private static double[][] Mean(List<T[][]> L) {
	// int n = L.size();
	// double[][] S = new double[4][3];
	// for (Iterator<double[][]> iterator = L.iterator(); iterator.hasNext();) {
	// double[][] proj = (double[][]) iterator.next();
	// for (int i = 0; i < proj.length; i++) {
	// for (int j = 0; j < proj[0].length; j++) {
	// S[i][j] += proj[i][j]/n;
	// }
	// }
	// }
	// return S;
	// }

	/* All things random */
	/**	Method for generating a random unitvector uniformly on the unitsphere.
	 * */
	private MyVector randomVector() {
		double X = rand.nextGaussian();
		double Y = rand.nextGaussian();
		double Z = rand.nextGaussian();
		MyVector vec = new MyVector(X, Y, Z);
		vec.normalize();
		return vec;
	}

	/**
	 * Generates a new vector a little different from the previous
	 */
	private MyVector markovSurface(MyVector S) {
		MyVector out;
		MyVector eps = randomVector();
		double sigma = S.dot(eps);
		eps = eps.sub(S.mult(sigma));
		eps = eps.normalize();
		double gamma = (2 * rand.nextDouble() - 1) * delta;
		out = MyVector.add(S, eps.mult(gamma)).normalize();
		return out;
	}
	/**
	 * Turns a vector in a random direction, an angle between 0 and pi.
	 * */
	private MyVector randomTurn(MyVector S) {
		MyVector out;
		MyVector normal = randomVector();
		double sigma = S.dot(normal);
		normal = normal.sub(S.mult(sigma));
		normal = normal.normalize();
		double theta = Math.PI*rand.nextDouble();
		out = S.mult(Math.cos(theta)).add(normal.mult(Math.sin(theta)));
		return out.normalize();
	}

	private int[] chooseRandomAtom(Random rand) {
		int X = rand.nextInt(2 * param.nX);
		int Y = rand.nextInt(2 * param.nY);
		int W = rand.nextInt(param.nZ);
		int Z;
		if ((X + Y) % 2 == 0) {
			Z = 2 * W;
		} else {
			Z = 2 * W + 1;
		}
		int[] index = new int[] { X, Y, Z };
		return index;
	}

	/**
	 * Scales the vector by the diagonal matrix diag(a,b,c)
	 */
	public MyVector scaleVec(double phiX, double phiY2, double phiZ, MyVector vec) {
		return new MyVector(phiX * vec.x, phiY2 * vec.y, phiZ * vec.z);
	}

	/* Indexing */
	public int[] normIndex(int[] ind) {
//		if (periodicBoundaries) {
			return new int[] { (ind[0] + 2 * param.nX) % (2 * param.nX), (ind[1] + 2 * param.nY) % (2 * param.nY),
					(ind[2] + 2 * param.nZ) % (2 * param.nZ) };
//		} 
//		else {
//			if (ind[0] < 0 || ind[1] < 0 || ind[2] < 0 || 
//					ind[0] >= (2 * param.nX) || 
//					ind[1] >= (2 * param.nY) || 
//					ind[2] >= (2 * param.nZ)) {
//				return null;
//			} else {
//				return ind;
//			}
//		}
	}

	public int[] addIndex(int[] local, int[] global) {
		return new int[] { global[0] + local[0], global[1] + local[1], global[2] + local[2] };
	}

	/* Iterators */
	public Iterator<int[]> iterateUnitCells() {
		List<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < 2 * param.nX; i += 2) {
			for (int j = 0; j < 2 * param.nY; j += 2) {
				for (int k = 0; k < 2 * param.nZ; k += 2) {
					if (crys.isValid(i, j, k)) {
						list.add(normIndex(new int[] { i, j, k }));
					} else {
						System.out.print("index not valid: " + i + ", " + j + ", " + k);
					}
				}
			}
		}
		return list.iterator();
	}

	public Iterator<int[]> iterateAtoms() {
		List<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < 2 * param.nX; i++) {
			for (int j = 0; j < 2 * param.nY; j++) {
				for (int k = 0; k < param.nZ; k++) {
					int z;
					if ((i + j) % 2 == 0) {
						z = 2 * k;
					} else {
						z = 2 * k + 1;
					}
					if (crys.isValid(i, j, z)) {
						list.add(normIndex(new int[] { i, j, z }));
					} else {
						System.out.print("index not valid: " + i + ", " + j + ", " + z);
					}
				}
			}
		}
		return list.iterator();
	}

	/* Getters and setters */
	private void setAtom(int[] index, Element elem) {
		atomType[index[0]][index[1]][index[2]] = elem;
	}

	public Element getAtom(int[] index) {
		return atomType[index[0]][index[1]][index[2]];
	}

	public MyVector getSpinDirection(int[] index) {
		return spins[index[0]][index[1]][index[2]];
	}

	private void setSpinDirection(int[] index, MyVector newSpin) {
		spins[index[0]][index[1]][index[2]] = newSpin;
	}

	private double calcMagnetization(int coord) {
		double mu = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = (int[]) it.next();
			mu += getSpinDirection(index).getCoord(coord);
		}
		return mu / nAtoms;
	}

	/* Generates and sets spin-configuration */
	private void newConfig() {
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = it.next();
			setSpinDirection(index, randomVector());
			setAtom(index, param.baseElem);
		}
	}

	public void setElementFraction(Element el, double frac) throws Exception {
		if (frac > 1.0)
			throw new Exception();
		while (getFraction(el) < frac) {
			int[] index = chooseRandomAtom(rand);
			setAtom(index, el);
		}
	}

	public double getFraction(Element el) {
		int count = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = (int[]) it.next();
			if (getAtom(index) == el) {
				count++;
			}
		}
		return (double) count / (double) nAtoms;
	}

	public void configFromBasisState(BasisState state) {
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] atomIndex = (int[]) it.next();
			MyVector spin = new MyVector(0, 0, 0);
			spin.setCoord(state.getSpin(atomIndex), state.coord);
			setSpinDirection(atomIndex, spin);
		}
	}

	public void setInitialConfig(MyVector[] list) {
		Iterator<int[]> it = iterateUnitCells();
		while (it.hasNext()) {
			int[] cellIndex = it.next();
			for (int i = 0; i < crys.getBasisNB().length; i++) {
				int[] localIndex = crys.getBasisNB()[i];
				int[] totalIndex = addIndex(localIndex, cellIndex);
				setSpinDirection(normIndex(totalIndex), list[i]);
			}
		}
	}

	public void loadConfig(File filename) throws IOException {
		Scanner in = new Scanner(filename);		
		in.nextLine();
		StringTokenizer paramLine = new StringTokenizer(in.nextLine(), ", ");
		paramLine.nextToken();
		param.nX = Integer.parseInt(paramLine.nextToken());
		param.nY = Integer.parseInt(paramLine.nextToken());
		param.nZ = Integer.parseInt(paramLine.nextToken());
		while(!in.nextLine().startsWith("Config:")) {
			
		}
		int i = 0;
		while (in.hasNextLine()) {
			String line = in.nextLine();

			StringTokenizer st = new StringTokenizer(line);
			int[] index = new int[3];
			index[0] = Integer.parseInt(st.nextToken());
			index[1] = Integer.parseInt(st.nextToken());
			index[2] = Integer.parseInt(st.nextToken());
			double x = Double.parseDouble(st.nextToken());
			double y = Double.parseDouble(st.nextToken());
			double z = Double.parseDouble(st.nextToken());
			MyVector vec = new MyVector(x, y, z);
			setSpinDirection(index, vec);

			Element elem = Element.valueOf(st.nextToken());
			setAtom(index, elem);
			i += 1;
		}
		in.close();
		System.out.println("Loaded in " + i + " spin orientations.");
	}

	public void saveConfig(PrintStream out) {

//		System.out.println("Prints end configuration to path: " + configFile.getAbsolutePath());
		printParam(out);
		out.println("Config:");
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = it.next();
			MyVector spin = getSpinDirection(index);
			out.print(index[0] + " ");
			out.print(index[1] + " ");
			out.print(index[2] + " ");
			out.print(spin.x + " ");
			out.print(spin.y + " ");
			out.print(spin.z + " ");
			out.println(getAtom(index));
		}
		out.close();
	}

}
