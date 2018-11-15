package main;

import java.util.List;

import constants.Crystal;
import processing.core.PVector;

public class Parameters {

	/** Number of steps aggregation and cell dimensions */
	
	public int nSteps;
	public int aggregate = 2 << 10;
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
	public boolean printFullArray;
	private boolean perBoundsX = true;
	private boolean perBoundsY = true;
	private boolean perBoundsZ = true;

	public static final int nIntParam = 4;
	public static final int nFloatParam = 4;
	public static final int nBoolParam = 1;

	public Crystal basis;

	private static final String[] paramNames = { "#Steps", "nX", "nY", "nZ", "Temperature", "Hx", "Hy", "Hz", "Save config" };
//	public final double[] Jlist;

	/**
	 * Initialize with parameters: arrI: #steps, nX, nY, nZ arrF: temperature, Hx, Hy, Hz \n
	 */
	public Parameters(final int[] arrI, final double[] arrF, final boolean[] options, final String[] strings) {
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
		nX = 2*arrI[1];
		nY = 2*arrI[2];
		nZ = 2*arrI[3];

		printFullArray = options[0];
		
		dir = strings[0];
		filename = strings[1]; // TODO Re write such that outputfile is directly given to simmulator.
		extension = strings[2];
	}

	public static String[] getNames() {
		return paramNames;
	}

	public double[] asList() {
		return new double[] { nSteps, nX, nY, nZ, temp, H.x, H.y, H.z };
	}

	private String getParam(int i) {
		switch (i) {
		case (0):
			return Integer.toString(nSteps);
		case (1):
			return Integer.toString(nX);
		case (2):
			return Integer.toString(nY);
		case (3):
			return Integer.toString(nZ);
		case (4):
			return Double.toString(temp);
		case (5):
			return Double.toString(H.x);
		case (6):
			return Double.toString(H.y);
		case (7):
			return Double.toString(H.z);
		case (8):
			return Boolean.toString(printFullArray);
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
}
