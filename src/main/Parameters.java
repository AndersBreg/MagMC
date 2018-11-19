package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import constants.Crystal;

public class Parameters {

	/** Number of steps aggregation and cell dimensions */
	
	public int nSteps;
	public int aggregate;
	public int nX = 1; // TODO Shoud be changed to be half as large and all references to be 2*nX
	public int nY = 1;
	public int nZ = 1;

	public double temp = 1;

	/** Applied H-field */
	public MyVector H = new MyVector(0, 0, 0);

	/** Filename and file extension type. */
	public String filename;
	public String extension;
	public String dir;

	/** Orientation */
	public Config initial;

	// Boolean options:
	private boolean perBoundsX = true;
	private boolean perBoundsY = true;
	private boolean perBoundsZ = true;

	public static final int nIntParam = 5;
	public static final int nFloatParam = 4;
	public static final int nBoolParam = 0;

	public Crystal basis;

	private static final String[] paramNames = { "#Steps", "Aggregate", "nX", "nY", "nZ", "Temperature", "Hx", "Hy", "Hz" };
//	public final double[] Jlist;

	/**
	 * Initialize with parameters: arrI: #steps, nX, nY, nZ arrF: temperature, Hx, Hy, Hz \n
	 */
	public Parameters(final int[] arrI, final double[] arrF, final String[] strings) {
		if (arrI.length != nIntParam || arrF.length != nFloatParam) {
			String[] paramNames = getNames();
			String st = new String("");
			for (int i = 0; i < paramNames.length; i++) {
				st = st.concat(paramNames[i] + ", ");
			}
			throw new IndexOutOfBoundsException("Not the right amount of parameters\n" + st);
		}

		temp = arrF[0];
		
		H = new MyVector(arrF[1], arrF[2], arrF[3]);

		nSteps = arrI[0];
		aggregate = arrI[1];
		nX = 2*arrI[2];
		nY = 2*arrI[3];
		nZ = 2*arrI[4];
		
		dir = strings[0];
		filename = strings[1]; // TODO Re write such that outputfile is directly given to simmulator.
		extension = strings[2];
	}

	public static String[] getNames() {
		return paramNames;
	}

	public double[] asList() {
		return new double[] { nSteps, aggregate, nX, nY, nZ, temp, H.x, H.y, H.z };
	}

	private String getParam(int i) {
		switch (i) {
		case (0):
			return Integer.toString(nSteps);
		case (1):
			return Integer.toString(aggregate);
		case (2):
			return Integer.toString(nX);
		case (3):
			return Integer.toString(nY);
		case (4):
			return Integer.toString(nZ);
		case (5):
			return Double.toString(temp);
		case (6):
			return Double.toString(H.x);
		case (7):
			return Double.toString(H.y);
		case (8):
			return Double.toString(H.z);
		default:
			return "";
		}
	}

	public String toString() {
		String S = new String();
		for (int i = 0; i < paramNames.length; i++) {
			S += paramNames[i] + ": " + getParam(i) + ", ";
		}
		return S;
	}

	public void setInitConfig(Config newConfig) {
		this.initial = newConfig;
	}
	

	public static Parameters loadParameters(String dir, String filename) throws IOException {
		File file = Paths.get(dir, filename + ".txt").toFile();
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		int[] intParam = new int[Parameters.nIntParam];
		double[] floatParam = new double[Parameters.nFloatParam];
		for (int i = 0; i < intParam.length; i++) {
			String s = in.readLine();
			intParam[i] = Integer.parseInt(s);
		}
		for (int i = 0; i < intParam.length; i++) {
			String s = in.readLine();
			floatParam[i] = Double.parseDouble(s);			
		}
		in.close();
		return new Parameters(intParam, floatParam, null);
	}
}
