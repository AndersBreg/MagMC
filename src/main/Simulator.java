package main;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.logging.*;

import constants.BasisState;
import constants.Crystal;
import constants.Element;

public class Simulator implements Runnable {

	/*
	 * Ni lat. const. = [10.02, 5.86, 4.68] Å; From Phys. Rev. 092413 (2009) 
	 * Ni lat. const. = [10.02, 5.83, 4.66] Å; From Phys. Rev. 054408 (2011); 
	 * Phys. Rev. 064421 (2017) and Phys. Rev. 064421 (2017)
	 * Co lat. const. = [10.20, 5.92, 4.70] Å; From Phys. Rev. 104420 (2017)
	 * Co lat. const. = [10.159, 5.9, 4.70] Å; From Phys. Rev. 104420 (2017)
	 */
	private static final String PREC = "%1.8e";
	
	public static final float a = 10.02f;
	public static final float b = 5.86f;
	public static final float c = 4.68f;
	public static final float scaling = 1;

	private static final double invBoltz = 11.6045; // inverse of the Boltzmann constant in units K / meV
	private static final double muB = 0.05788381751; // mu_B the Bohr_magneton in units meV/T
	private static final double g = 2; // the gyromagnetic factor of the electron
	public Crystal crys = Crystal.FCC;
	public MyVector[] basis = crys.get();
	public int nBasis = basis.length; 

	public int nAtoms;
	public Element[][][] atomType;
	public MyVector[][][] spins;
	public MyVector[][][] positions;
	public int step = 0;

	/**
	 * Parameter values: Temperature, #steps, nX, nY, Hx, Hy, Hz
	 */
	public Parameters param;

	// Output values
	public double energySingle;
	private double energyTotal;
	
	private double energy_Sum = 0;
	private double energy_SumSq = 0;
	
//	private double sumFX;
//	private double sumFY;
//	private double sumFZ;
//	private double sumCZ;
//	private double sumCX;
//	private double sumCY;
//	private double sumAX;
//	private double sumAY;
//	private double sumAZ;
//	private double sumGX;
//	private double sumGY;
//	private double sumGZ;
//	private double sumFX_Sq;
//	private double sumFY_Sq;
//	private double sumFZ_Sq;
//	private double sumCZ_Sq;
//	private double sumCX_Sq;
//	private double sumCY_Sq;
//	private double sumAX_Sq;
//	private double sumAY_Sq;
//	private double sumAZ_Sq;
//	private double sumGX_Sq;
//	private double sumGY_Sq;
//	private double sumGZ_Sq;
	
	private double[][] baseProj = new double[4][3];
	private double[][] baseProj_Sum = new double[4][3];
	private double[][] baseProj_SumSq = new double[4][3];
	
	private Random rand;
	
	private MyVector mid;
	public File outputFile;
	
	private static final String defaultDir = "C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\";
	
	private double delta;
	public int nRejects = 0;

	private long seed = 0;
	/**
	 * Parameter values: #steps, nX, nY, nZ, Temperature, Hx, Hy, Hz
	 * @throws FileAlreadyExistsException 
	 */
	public Simulator(Parameters newParam, File file) throws FileAlreadyExistsException {
		this.param = newParam;
		setup();
		outputFile = file;
		System.out.println("No configuration specified, generates new.");
		newConfig();
	}
	
	public Simulator(Parameters newParam, File file, File configFile) throws IOException {
		this.param = newParam;
		setup();
		outputFile = file;
		loadConfig(configFile);
	}
	
	public Simulator() {
		this.param = new Parameters();
		setup();
	}
	
	public void setup() {
		nAtoms = param.nX * param.nY * param.nZ * nBasis;

		positions = new MyVector[2*param.nX][2*param.nY][2*param.nZ];
		atomType = new Element[2*param.nX][2*param.nY][2*param.nZ];
		spins = new MyVector[2*param.nX][2*param.nY][2*param.nZ];

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
//		outputFile = new File(param.dir + filename);
		Path path = outputFile.toPath();

		outputFile = path.toAbsolutePath().toFile();

		try {
			System.out.println("Prints data to path: " + outputFile.getCanonicalPath() );
			PrintStream out = new PrintStream(outputFile);
			// Prints parameter names:
			String[] names = Parameters.getNames();
			for (int i = 0; i < names.length; i++) {
				out.print(names[i] + ", ");
			}
			out.print("Ni frac, Co frac, Fe frac, ");
			out.println();
			
			// Prints the parameter values:
			double[] parameters = param.asList();
			for (int i = 0; i < parameters.length; i++) {
				out.print(parameters[i] + ", ");
			}
			out.print(getFraction(Element.Ni) + ", ");
			out.print(getFraction(Element.Co) + ", ");
			out.println(getFraction(Element.Fe) + ", ");
			out.println("Ni param: " + Element.Ni.paramString());
			out.println("Co param: " + Element.Co.paramString());
			out.println("Fe param: " + Element.Fe.paramString());
			out.println("Output: ");
			out.println("energy, en. var, "
					+ "FX, FX var, FY, FY var, FZ, FZ var, "
					+ "Cx, Cx var, Cy, Cy var, Cz, Cz var, "
					+ "Ax, Ax var, Ay, Ay var, Az, Az var, "
					+ "Gx, Gx var, Gy, Gy var, Gz, Gz var");
			simulate(out);

			out.close();
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		}
		System.out.println("Done!");
	}

	private void simulate(PrintStream out) {
		energySingle = calcTotalEnergy();
		energyTotal = calcTotalEnergy();
		baseProj = allProjectionsTotal();
		
		while (step < param.nSteps) {
			step += 1;
			
			iterateRandomSingle();
			
			try {
				Thread.sleep(0, 0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (step % param.aggregate == 0) {
				flush(out);
			}
		}
	}

	private void flush(PrintStream out) {
		double energy_Mean = energy_Sum / param.aggregate;
		double energy_Var = (energy_SumSq / param.aggregate - energy_Mean * energy_Mean);

		double[][] baseProj_Mean = new double[4][3]; 
		double[][] baseProj_Var = new double[4][3]; 
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				baseProj_Mean[state][coord] = baseProj_Sum[state][coord] / param.aggregate;
				baseProj_Var[state][coord]  = 
						(baseProj_SumSq[state][coord] / param.aggregate - baseProj_Mean[state][coord] * baseProj_Mean[state][coord]);
			}
		}

		printVals(out, energy_Mean, energy_Var, baseProj_Mean, baseProj_Var);
		
		energy_Sum = 0;
		energy_SumSq = 0;
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				baseProj_Sum[state][coord] = 0;
				baseProj_SumSq[state][coord]  = 0;
			}
		}

//		double fX_Mean = sumFX / (double) nAtoms / (double) param.aggregate;
//		double fY_Mean = sumFY / (double) nAtoms / (double) param.aggregate;
//		double fZ_Mean = sumFZ / (double) nAtoms / (double) param.aggregate;
//
//		double fX_Var = (sumFX_Sq / (double) param.aggregate - fX_Mean * fX_Mean);
//		double fY_Var = (sumFY_Sq / (double) param.aggregate - fY_Mean * fY_Mean);
//		double fZ_Var = (sumFZ_Sq / (double) param.aggregate - fZ_Mean * fZ_Mean);
//
//		double cX_Mean = sumCX / (double) nAtoms / (double) param.aggregate;
//		double cY_Mean = sumCY / (double) nAtoms / (double) param.aggregate;
//		double cZ_Mean = sumCZ / (double) nAtoms / (double) param.aggregate;
//		
//		double cX_Var = (sumCX_Sq - cX_Mean * cX_Mean)/param.aggregate;
//		double cY_Var = (sumCY_Sq - cY_Mean * cY_Mean)/param.aggregate;
//		double cZ_Var = (sumCZ_Sq - cZ_Mean * cZ_Mean)/param.aggregate;
//		
//		double gX_Mean = sumGX / (double) nAtoms / (double) param.aggregate;
//		double gY_Mean = sumGY / (double) nAtoms / (double) param.aggregate;
//		double gZ_Mean = sumGZ / (double) nAtoms / (double) param.aggregate;
//		
//		double gX_Var = (sumGX_Sq - gX_Mean * gX_Mean)/param.aggregate;
//		double gY_Var = (sumGY_Sq - gY_Mean * gY_Mean)/param.aggregate;
//		double gZ_Var = (sumGZ_Sq - gZ_Mean * gZ_Mean)/param.aggregate;
//		
//		double aX_Mean = sumAX / (double) nAtoms / (double) param.aggregate;
//		double aY_Mean = sumAY / (double) nAtoms / (double) param.aggregate;
//		double aZ_Mean = sumAZ / (double) nAtoms / (double) param.aggregate;
//		
//		double aX_Var = (sumAX_Sq - aX_Mean * aX_Mean)/param.aggregate;
//		double aY_Var = (sumAY_Sq - aY_Mean * aY_Mean)/param.aggregate;
//		double aZ_Var = (sumAZ_Sq - aZ_Mean * aZ_Mean)/param.aggregate;
		
//		out.print(String.format(PREC, energy_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, energy_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, fX_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, fX_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, fY_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, fY_Var));
//		out.print(", ");
//
//		out.print(String.format(PREC, fZ_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, fZ_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, cX_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, cX_Var));
//		out.print(", ");
//
//		out.print(String.format(PREC, cY_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, cY_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, cZ_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, cZ_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, gX_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, gX_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, gY_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, gY_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, gZ_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, gZ_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, aX_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, aX_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, aY_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, aY_Var));
//		out.print(", ");
//		
//		out.print(String.format(PREC, aZ_Mean));
//		out.print(", ");
//		out.print(String.format(PREC, aZ_Var));
//		out.print(", ");
//		out.println();
//		
//		sumEnergy = 0;
//		sumEnergySq = 0;
//		sumCX = 0;
//		sumCY = 0;
//		sumCZ = 0;
//		sumFX = 0;
//		sumFY = 0;
//		sumFZ = 0;
//		sumGX = 0;
//		sumGY = 0;
//		sumGZ = 0;
//		sumAX = 0;
//		sumAY = 0;
//		sumAZ = 0;
//		
//		sumCX_Sq = 0;
//		sumCY_Sq = 0;
//		sumCZ_Sq = 0;
//		sumFX_Sq = 0;
//		sumFY_Sq = 0;
//		sumFZ_Sq = 0;
//		sumGX_Sq = 0;
//		sumGY_Sq = 0;
//		sumGZ_Sq = 0;
//		sumAX_Sq = 0;
//		sumAY_Sq = 0;
//		sumAZ_Sq = 0;
	}

	private void printVals(PrintStream out, double energy_Mean, double energy_Var, double[][] baseProjMean, double[][] baseProjVar) {

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
		out.println();
	}

	private void iterateRandomSingle() {
		
		int[] index = chooseRandomAtom(rand);
		MyVector old = getSpinDirection(index).copy();

		// Before change
		double oldEnergy = calcAtomEnergy(index);
		double[][] projOld = allProjectionsSingle(index);
		
		// Create a new sample / make a small change / proposal spin:
		setSpinDirection(index, markovSurface(getSpinDirection(index)));

		// Calculate energy and other values with new vector:
		double newEnergy = calcAtomEnergy(index);
		
		double delE = newEnergy - oldEnergy;
		if (rand.nextDouble() <= Math.min(1, Math.exp(-delE * invBoltz / param.temp))) {
			energySingle = energySingle + delE;
			double[][] projNew = allProjectionsSingle(index);
			updateBaseProj(projOld, projNew);
		} else {
			setSpinDirection(index, old);
			double[][] projNew = allProjectionsSingle(index);
			updateBaseProj(projOld, projNew);
			nRejects  += 1;
		}
	}

	/* Energy calculations: */
	private double calcTotalEnergy() {
		double E = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] localIndex = it.next();
			E += calcFieldEnergy(localIndex);
			E += calcAniEnergy(localIndex);
			E += calcCouplingEnergy(localIndex);
		}
		return E;
	}

	private double calcAtomEnergy(int[] index) {
		double E = 0;
		
		// Field energy:
		E += calcFieldEnergy(index);

		// Single-ion anisotropy:
		E += calcAniEnergy(index);

		// NN couplings
		E += 2*calcCouplingEnergy(index);
		
		return E;
	}

	private double calcCouplingEnergy(int[] indexA) {
		double E = 0;
		MyVector S = getSpinDirection(indexA);
		Element atomA = getAtom(indexA);

		int[][][] allNeighbours = crys.getNNIndices().clone();
		for (int nJ = 0; nJ < allNeighbours.length; nJ++) {
			int[][] nb_with_specific_J = allNeighbours[nJ];
			
			for (int n = 0; n < nb_with_specific_J.length; n++) {
				int[] spec_J_nb = nb_with_specific_J[n];
				int[] indexB = normIndex(addIndex(indexA, spec_J_nb));
				Element atomB = getAtom(indexB);
				E += atomA.getCoupling(atomB, nJ) * S.dot(getSpinDirection(indexB))/2;
			}
		}
		return E;
	}

	private double calcFieldEnergy(int[] index) {
		MyVector S = getSpinDirection(index).mult(getAtom(index).spin); //Spin vector S
		double E = -g * muB * S.dot(param.H);
		return E;
	}
	
	private double calcAniEnergy(int[] index) {
		double E = 0;
		MyVector S = getSpinDirection(index).mult(getAtom(index).spin); //Spin vector S, may require .mult(getAtom(index).spin)
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

	private void updateBaseProj(double[][] projOld, double[][] projNew) {
		energy_Sum += energySingle / nAtoms;
		energy_SumSq += energySingle * energySingle / (nAtoms * nAtoms);

		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				double diff = projNew[state][coord] - projOld[state][coord];
				baseProj[state][coord] = baseProj[state][coord] + diff;
				baseProj_Sum[state][coord] += baseProj[state][coord];
				baseProj_SumSq[state][coord] += baseProj[state][coord] * baseProj[state][coord];
			}
		}
		// double fX = projBasisStateTotal(Crystal.Fx); // Alternative use calcMagnetization(0);
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
				projections[state][coord] = projAtom(index, BasisState.getState(state, coord) );
			}
		}
		return projections;
	}

	private double[][] allProjectionsTotal() {
		double[][] projections = new double[4][3];
		for (int state = 0; state < 4; state++) {
			for (int coord = 0; coord < 3; coord++) {
				projections[state][coord] = projTotal(BasisState.getState(state, coord) );
			}
		}
		return projections;
	}

	/* All things random */
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
	private MyVector markovSurface(MyVector x) {
		MyVector out;
		MyVector eps = randomVector();
		double sigma = x.dot(eps);
		eps = eps.sub(eps.mult(sigma));
		eps = eps.normalize();
		double gamma = (2*rand.nextDouble() - 1) * delta;
		out = MyVector.add(x, eps.mult(gamma)).normalize();
		// out = randomVector(); // Direct sampling
		return out;
	}

	private int[] chooseRandomAtom(Random rand) {
		int X = rand.nextInt(2*param.nX); 
		int Y = rand.nextInt(2*param.nY);
		int W = rand.nextInt(param.nZ);
		int Z;
		if ( (X+Y) % 2 == 0) {
			Z = 2*W;
		} else {
			Z = 2*W+1;
		}
		int[] index = new int[] {X, Y, Z};
		return index;
	}

	/**
	 * Scales the vector by the diagonal matrix diag(a,b,c)
	 */
	public MyVector scaleVec(float a, float b, float c, MyVector vec) {
		return new MyVector(a * vec.x, b * vec.y, c * vec.z);
	}

	/* Indexing */
	public int[] normIndex(int[] ind) {
		return new int[] { 
				(ind[0] + 2*param.nX) % (2*param.nX), 
				(ind[1] + 2*param.nY) % (2*param.nY),
				(ind[2] + 2*param.nZ) % (2*param.nZ) };
	}

	private int[] addIndex(int[] local, int[] global) {
		return new int[] {
				global[0]+local[0],
				global[1]+local[1],
				global[2]+local[2]
		};
	}
	
	/* Iterators */
	public Iterator<int[]> iterateUnitCells() {
		List<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < 2*param.nX; i += 2) {
			for (int j = 0; j < 2*param.nY; j += 2) {
				for (int k = 0; k < 2*param.nZ; k += 2) {
					if (crys.isValid(i, j, k)) {
						list.add(normIndex(new int[] { i, j, k }));
					} else {
						System.out.print("index not valid: "+ i+", "+j+", "+k);
					}
				}
			}
		}
		return list.iterator();
	}
	
	public Iterator<int[]> iterateAtoms() {
		List<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < 2*param.nX; i++) {
			for (int j = 0; j < 2*param.nY; j++) {
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
		return mu/nAtoms;
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
		if(frac > 1.0)
			throw new Exception();
		while ( getFraction(el) < frac) {
			int[] index = chooseRandomAtom(rand);
			setAtom(index, el);
		}
	}
	
	public double getFraction(Element el) {
		int count = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = (int[]) it.next();
			if(getAtom(index) == el) {
				count++;
			}
		}
		return (double) count/(double) nAtoms;
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
			int[] cellIndex = (int[]) it.next(); 
			for (int i = 0; i < crys.getBasisNB().length; i++) {
				int[] localIndex = crys.getBasisNB()[i];
				int[] totalIndex = addIndex(localIndex, cellIndex);
				setSpinDirection(normIndex(totalIndex), list[i]);
			}
		}
	}

	public void loadConfig(File filename) throws IOException {
		Iterator<int[]> it = iterateAtoms();
		Scanner in = new Scanner(filename);
		int i = 0;
		while (in.hasNextLine()) {
			int[] index = it.next();
			String line = in.nextLine();
			if (line.isEmpty()) {
				System.out.println("Warning: Not all atoms were assigned a spin.");
			} else {
				StringTokenizer st = new StringTokenizer(line);
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				double z = Double.parseDouble(st.nextToken());
				MyVector vec = new MyVector(x, y, z);
				setSpinDirection(index, vec);
	
				Element elem = Element.valueOf(st.nextToken());
				setAtom(index, elem);
			}
			i += 1;
		}
		in.close();
		System.out.println("Loaded in " + i + " spin orientations.");
	}

	public void saveConfig(File configFile) {
	
		System.out.println("Prints end configuration to path: " + configFile.getAbsolutePath());
	
		try {
			PrintStream out = new PrintStream(configFile);
			Iterator<int[]> it = iterateAtoms();
			while(it.hasNext()) {
				int[] index = it.next();
				MyVector spin = getSpinDirection(index); 
				out.print(spin.x + " ");
				out.print(spin.y + " ");
				out.print(spin.z + " ");
				out.println(getAtom(index));
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		}
	}

}
