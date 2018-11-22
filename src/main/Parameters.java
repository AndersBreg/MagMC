package main;

import java.io.*;
import java.nio.file.Paths;

import constants.*;

public class Parameters {

	/** Number of steps aggregation and cell dimensions */
	public int nSteps;
	public int aggregate;
	public int nX = 1;
	public int nY = 1;
	public int nZ = 1;

	public double temp;

	/** Applied H-field */
	public MyVector H = new MyVector(0, 0, 0);

	public Element baseElem = Element.Ni;
	
	/** Orientation */
	public Config initial;

	/** Standard number of parameters. */
	public static final int nIntParam = 5;
	public static final int nFloatParam = 4;
	public static final int nBoolParam = 0;

//	/** Filename and file extension type. */
//	public String filename;
//	public String extension;
//	public String dir;

	// Boolean options:
//	private boolean perBoundsX = true;
//	private boolean perBoundsY = true;
//	private boolean perBoundsZ = true;

	private static final String[] paramNames = { "#Steps", "Aggregate", "nX", "nY", "nZ", "Temperature", "Hx", "Hy", "Hz" };

	/**
	 * Initialize with parameters: arrI: #steps, nX, nY, nZ arrF: temperature, Hx, Hy, Hz \n
	 */
	public Parameters(final int[] arrI, final double[] arrF) {
		if (arrI.length != nIntParam || arrF.length != nFloatParam) {
			String[] paramNames = getNames();
			String st = new String("");
			for (int i = 0; i < paramNames.length; i++) {
				st = st.concat(paramNames[i] + ", ");
			}
			throw new IndexOutOfBoundsException("Not the right amount of parameters\n" + st);
		}

		nSteps = arrI[0];
		aggregate = arrI[1];
		nX = arrI[2];
		nY = arrI[3];
		nZ = arrI[4];
		
		temp = arrF[0];
		
		H = new MyVector(arrF[1], arrF[2], arrF[3]);
	}
	
	public Parameters() {
		
	}

	public Parameters clone() {
		return new Parameters(new int[] {nSteps, aggregate, nX, nY, nZ}, new double[] {temp, H.x, H.y, H.z});
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
		return new Parameters(intParam, floatParam);
	}
}
