package main;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.logging.*;

import constants.Crystal;
import constants.Element;

public class Simulator implements Runnable {

	/*
	 * Ni lat. const. = [10.02, 5.86, 4.68] Å; From Phys. Rev. 092413 (2009) Ni lat.
	 * const. = [10.02, 5.83, 4.66] Å; From Phys. Rev. 054408 (2011); Phys. Rev.
	 * 064421 (2017) and Phys. Rev. 064421 (2017) and Co lat. const. = [10.20, 5.92,
	 * 4.70] Å; From Phys. Rev. 104420 (2017)
	 */

	public static final float a = 10.02f;
	public static final float b = 5.86f;
	public static final float c = 4.68f;
	public static final float scaling = 1;

	public Crystal crys = Crystal.FCC;
	public MyVector[] basis = crys.get();
	public int nBasis = basis.length; 

	public int nAtoms;
	public Element[][][] atomType;
	public MyVector[][][] spins;
	public MyVector[][][] positions;
	public int progress = 0;

	/**
	 * Parameter values: Temperature, #steps, nX, nY, nZ, Ja, Jb, Jc, Jbc, Jac, Jbc,
	 * Da, Db, Dc, Hx, Hy, Hz
	 */
	public Parameters param;

	// Output values
	public double energySingle;
	private double energyTotal;
	
	private double sumEnergy = 0;
	private double sumEnergySq = 0;
	
	private double sumMuX;
	private double sumMuX_Sq;
	
	private double sumMuY;
	private double sumMuY_Sq;
	
	private double sumMuZ;
	private double sumMuZ_Sq;

	private Random rand;
	/*
	 * Ni lat. const. = [10.02, 5.86, 4.68] Å; From Phys. Rev. 092413 (2009) Ni lat.
	 * const. = [10.02, 5.83, 4.66] Å; From Phys. Rev. 054408 (2011); Phys. Rev.
	 * 064421 (2017) and Phys. Rev. 064421 (2017) and Co lat. const. = [10.20, 5.92,
	 * 4.70] Å; From Phys. Rev. 104420 (2017)
	 */
	private MyVector mid;
	private String filename;
	public File outputFile;
	
	private static final String defaultDir = "C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\";
	
	private double delta = 0.5;
	public int nRejects = 0;
	private static final double invBoltz = 11.6045f; // inverse of the Boltzmann constant in units K / meV
	
	/**
	 * Parameter values: #steps, nX, nY, nZ, Temperature, Ja, Jb, Jc, Jbc, Jac, Jbc,
	 * Da, Db, Dc, Hx, Hy, Hz
	 */
	public Simulator(Parameters newParam) { // TODO Re-write constructor to accept a filename.
		this.param = newParam;
		setup();
		
		if (param.filename.equals("")) {
			filename = String.format("Test_T=%1.2f.txt", param.temp);
			System.out.println("No filename given, output file set to " + filename);
			outputFile = Paths.get(param.dir, filename).toFile();
		} else {
			outputFile = Paths.get(param.dir, param.filename + param.extension).toFile();
			for (int i = 0; i < 10; i++) {
				if (outputFile.exists()) {
					outputFile = Paths.get(param.dir, param.filename + "_" + i + param.extension).toFile();
				}
			}
		}
		System.out.println("No configuration specified, generates new.");
		newConfig();
	}
	
	 // TODO Re-write constructor to accept a filename.
	public Simulator(Parameters newParam, String configFile) throws IOException {
		this.param = newParam;
		setup();
		
		if (param.filename.equals("")) {
			filename = String.format("Test_T=%1.2f.txt", param.temp);
			System.out.println("No filename given, output file set to " + filename);
			outputFile = Paths.get(param.dir, filename).toFile();
		} else {
			outputFile = Paths.get(param.dir, param.filename + param.extension).toFile();
			for (int i = 0; i < 10; i++) {
				if (outputFile.exists()) {
					outputFile = Paths.get(param.dir, param.filename + "_" + i + param.extension).toFile();
				}
			}
		}
		loadConfig(configFile);
	}
	
	public void setup() {
		nAtoms = param.nX * param.nY * param.nZ * nBasis / 8;

		positions = new MyVector[param.nX][param.nY][param.nZ];
		atomType = new Element[param.nX][param.nY][param.nZ];
		spins = new MyVector[param.nX][param.nY][param.nZ];

		rand = new Random();

		mid = new MyVector(param.nX * 0.25, param.nY * 0.25, param.nZ * 0.25);
		mid = scaleVec(a, b, c, mid);
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = it.next();
			index = normIndex(index);
			MyVector position = new MyVector(index[0], index[1], index[2]);
			position = scaleVec(a/2, b/2, c/2, position);
			positions[index[0]][index[1]][index[2]] = position.sub(mid).mult(scaling);
		}
	}

	@Override
	public void run() {
//		outputFile = new File(param.dir + filename);
		Path path = outputFile.toPath();

		outputFile = path.toAbsolutePath().toFile();
		System.out.println("Prints data to path: " + outputFile.getAbsolutePath());

		try {
			PrintStream out = new PrintStream(outputFile);
			String[] names = Parameters.getNames();
			for (int i = 0; i < names.length; i++) {
				out.print(names[i] + ", ");
			}
			out.println();
			double[] parameters = param.asList();
			for (int i = 0; i < parameters.length; i++) {
				out.print(parameters[i] + ", ");
			}
			out.println();
			out.println("Output: ");
			out.println("energy, en. var, muX, muX var, muY, muY var, muZ, muZ var");
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
		
		while (progress < param.nSteps) {
			progress += 1;
			
			double dE = iterateRandomSingle(param.temp);
			
			try {
				Thread.sleep(0, 0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			double energyPrAtom = energySingle/nAtoms; 
			double muX = calcMagnetization(0);
			double muY = calcMagnetization(1);
			double muZ = calcMagnetization(2);
			sumEnergy += energyPrAtom;
			sumEnergySq += energyPrAtom * energyPrAtom;
			sumMuX += muX;
			sumMuY += muY;
			sumMuZ += muZ;
			sumMuX_Sq += muX*muX;
			sumMuY_Sq += muY*muY;
			sumMuZ_Sq += muZ*muZ;
			
			if (param.printFullArray && progress % param.aggregate == 0) {
				double energy_Mean = sumEnergy/param.aggregate;
				double energy_Var = (sumEnergySq - energy_Mean * energy_Mean)/param.aggregate;
				
				double muX_Mean = sumMuX/param.aggregate;
				double muX_Var = (sumMuX_Sq - muX_Mean * muX_Mean)/param.aggregate;
				
				double muY_Mean = sumMuY/param.aggregate;
				double muY_Var = (sumMuY_Sq - muY_Mean * muY_Mean)/param.aggregate;
				
				double muZ_Mean = sumMuZ/param.aggregate;
				double muZ_Var = (sumMuZ_Sq - muZ_Mean * muZ_Mean)/param.aggregate;
				
				out.print(String.format("%1.8e", energy_Mean));
				out.print(", ");
				out.print(String.format("%1.8e", energy_Var));
				out.print(", ");
				
				out.print(String.format("%1.8e", muX_Mean));
				out.print(", ");
				out.print(String.format("%1.8e", muX_Var));
				out.print(", ");
				
				out.print(String.format("%1.8e", muY_Mean));
				out.print(", ");
				out.print(String.format("%1.8e", muY_Var));
				out.print(", ");
				
				out.print(String.format("%1.8e", muZ_Mean));
				out.print(", ");
				out.print(String.format("%1.8e", muZ_Var));
//				out.print(", ");
				
				out.println();
				
				sumEnergy = 0;
				sumEnergySq = 0;
			}
		}
		if (!param.printFullArray) {
			double meanEnergy = sumEnergy / (double) param.nSteps / nAtoms;
			double varEnergy = (sumEnergySq - sumEnergy * sumEnergy / (double) param.nSteps) / (double) param.nSteps
					/ nAtoms;
			double meanX = sumMuX / (double) param.nSteps / nAtoms;
			double meanY = sumMuY / (double) param.nSteps / nAtoms;
			double meanZ = sumMuZ / (double) param.nSteps / nAtoms;
			out.print(meanEnergy + ", ");
			out.print(varEnergy + ", ");
			out.print(meanX + ", ");
			out.print(meanY + ", ");
			out.println(meanZ);
		}
	}

	private double iterateRandomSingle(double temp) {
		int[] index = chooseRandomAtom(rand);
		MyVector old = getSpin(index).copy();

		// Before change
		double oldEnergy = calcAtomEnergy(index);
		
		// Create a new sample / make a small change:
		setSpin(index, markovSurface(getSpin(index)));

		// Calculate energy with new vector:
		double newEnergy = calcAtomEnergy(index);
		double delE = newEnergy - oldEnergy;
		if (rand.nextDouble() <= Math.min(1, Math.exp(-delE * invBoltz / temp))) {
			energySingle = energySingle + delE;
			return delE;
		} else {
			setSpin(index, old);
			nRejects  += 1;
			return 0;
		}
	}

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
		MyVector S = getSpin(indexA);
		Element atomA = getAtom(indexA);
		
		int[][][] allNeighbours = crys.getNNIndices();
		for (int nJ = 0; nJ < allNeighbours.length; nJ++) {
			int[][] nb_with_specific_J = allNeighbours[nJ];
			
			for (int n = 0; n < nb_with_specific_J.length; n++) {
				int[] spec_J_nb = nb_with_specific_J[n];
				int[] indexB = normIndex(addIndex(indexA, spec_J_nb));
				Element atomB = getAtom(indexB);
				E += atomA.getCoupling(atomB, nJ) * S.dot(getSpin(indexB))/2;
			}
		}
		return E;
	}

	private double calcFieldEnergy(int[] index) {
		double E = 0;
		MyVector S = getSpin(index);
		E = -S.dot(param.H) * getAtom(index).spin;
		return E;
	}
	
	private double calcAniEnergy(int[] index) {
		double E = 0;
		MyVector S = getSpin(index);
		E += getAtom(index).Dx * S.x * S.x;
		E += getAtom(index).Dy * S.y * S.y;
		E += getAtom(index).Dz * S.z * S.z;
		return E;
	}
	
	private double calcMagnetization(int coord) {
		double mu = 0;
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = (int[]) it.next();
			mu += getSpin(index).getCoord(coord);
		}
		return mu/nAtoms;
	}

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

	public int[][][] getNeighbours() {
		return this.crys.getNNIndices().clone();
	}

	/**
	 * Scales the vector by the diagonal matrix diag(a,b,c)
	 */
	public MyVector scaleVec(float a, float b, float c, MyVector vec) {
		return new MyVector(a * vec.x, b * vec.y, c * vec.z);
	}

	private void setAtom(int[] index, Element elem) {
		atomType[index[0]][index[1]][index[2]] = elem;	
	}

	public Element getAtom(int[] index) {
		return atomType[index[0]][index[1]][index[2]];
	}

	private int[] chooseRandomAtom(Random rand) {
		int X = rand.nextInt(param.nX); 
		int Y = rand.nextInt(param.nY);
		int W = rand.nextInt(param.nZ/2);
		int Z;
		if ( (X+Y) % 2 == 0) {
			Z = 2*W;
		} else {
			Z = 2*W+1;
		}
		int[] index = new int[] {X, Y, Z};
		return index;
	}

	public int[] normIndex(int[] ind) {
		return new int[] { 
				(ind[0] + param.nX) % param.nX, 
				(ind[1] + param.nY) % param.nY,
				(ind[2] + param.nZ) % param.nZ };
	}

	public Iterator<int[]> iterateUnitCells() {
		List<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < param.nX; i += 2) {
			for (int j = 0; j < param.nY; j += 2) {
				for (int k = 0; k < param.nZ; k += 2) {
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
		for (int i = 0; i < param.nX; i++) {
			for (int j = 0; j < param.nY; j++) {
				for (int k = 0; k < param.nZ / 2; k++) {
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

	public void setInitial(MyVector[] list) {
		Iterator<int[]> it = iterateUnitCells();
		while (it.hasNext()) {
			int[] cellIndex = (int[]) it.next(); 
			for (int i = 0; i < crys.getBasisNB().length; i++) {
				int[] localIndex = crys.getBasisNB()[i];
				int[] totalIndex = addIndex(localIndex, cellIndex);
				setSpin(normIndex(totalIndex), list[i]);
			}
		}
	}

	public MyVector getSpin(int[] index) {
		return spins[index[0]][index[1]][index[2]];
	}
	
	private void setSpin(int[] index, MyVector newSpin) {
		spins[index[0]][index[1]][index[2]] = newSpin;
	}
	
	private int[] addIndex(int[] local, int[] global) {
		return new int[] {
				global[0]+local[0],
				global[1]+local[1],
				global[2]+local[2]
		};
	}

	public double productWithBasisState(MyVector[] basisState) {
		double proj = 0;
		Iterator<int[]> it = iterateUnitCells();
		while (it.hasNext()) {
			int[] unitCellIndex = (int[]) it.next();
			int[][] basis = crys.getBasisNB();
			for (int i = 0; i < basis.length; i++) {
				int[] index = addIndex(unitCellIndex, basis[i]);
				proj += getSpin(index).dot(basisState[i]);
			}
		}
		return proj;
	}	
	
	
	/** Generates and sets spin-configuration */
	private void newConfig() {
		Iterator<int[]> it = iterateAtoms();
		while (it.hasNext()) {
			int[] index = it.next();
			setSpin(index, randomVector());
			setAtom(index, Element.Ni);	
		}
	}


	public void configFromBasisState(MyVector[] basisState) {
		Iterator<int[]> it = iterateUnitCells();
		while (it.hasNext()) {
			int[] unitCellIndex = (int[]) it.next();
			int[][] basis = crys.getBasisNB();
			for (int i = 0; i < basis.length; i++) {
				int[] index = addIndex(unitCellIndex, basis[i]);
				setSpin(index, basisState[i]);
			}
		}
	}	
	
	public void loadConfig(String filename) throws IOException {
		Iterator<int[]> it = iterateAtoms();
		BufferedReader in = new BufferedReader(new FileReader(filename));
		int i = 0;
		while (it.hasNext()) {
			int[] index = it.next();
			String line = in.readLine();
			if (line == null) {
				break;
			} else {
				StringTokenizer st = new StringTokenizer(line);
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				double z = Double.parseDouble(st.nextToken());
				MyVector vec = new MyVector(x, y, z);
				setSpin(index, vec);
	
				Element elem = Element.valueOf(st.nextToken());
				setAtom(index, elem);
			}
			i += 1;
		}
		in.close();
		System.out.println("Loaded in " + i + " spin orientations.");
	}

	public void saveConfig(String filename) {
		File file = new File(filename);
		Path path = file.toPath();
	
		file = path.toAbsolutePath().toFile();
		System.out.println("Prints end configuration to path: " + file.getAbsolutePath());
	
		try {
			PrintStream out = new PrintStream(file);
			Iterator<int[]> it = iterateAtoms();
			while(it.hasNext()) {
				int[] index = it.next();
				MyVector spin = getSpin(index); 
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
