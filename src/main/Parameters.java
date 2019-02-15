package main;

import java.io.*;
import java.nio.file.Paths;

import constants.*;

public class Parameters {

	/** Number of steps aggregation and cell dimensions */
	public long nSteps;
	public int nX = 1;
	public int nY = 1;
	public int nZ = 1;

	public Element baseElem = null;
	public BasisState initState = null;
	
	/** Orientation */
	public Config initial;

	/** Standard number of parameters. */
	public static final int nIntParam = 4;
	public static final int nBoolParam = 0;

	public static final String[] paramNames = { "MonteCarloSteps", "nX", "nY", "nZ"};

	/**
	 * Initialize with parameters: arrI: #steps, nX, nY, nZ arrF: temperature, Hx, Hy, Hz \n
	 */
	public Parameters(final long[] ls) {
		if (ls.length != nIntParam) {
			String st = new String("");
			for (int i = 0; i < paramNames.length; i++) {
				st = st.concat(paramNames[i] + ", ");
			}
			throw new IndexOutOfBoundsException("Not the right amount of parameters\n" + st);
		}

		nX = (int) ls[1];
		nY = (int) ls[2];
		nZ = (int) ls[3];
		nSteps = ls[0]*nX*nY*nZ*4;
	}
	
	public Parameters() {
		
	}

	public Parameters clone() {
		Parameters param = new Parameters(new long[] {nSteps, nX, nY, nZ});
		param.baseElem = this.baseElem;
		return param;
	}
	
	public long[] getValues() {
		return new long[] { nSteps/(nX*nY*nZ*4), nX, nY, nZ};
	}

	public String getParam(int i) {
		switch (i) {
		case (0):
			return Long.toString(nSteps/(nX*nY*nZ*4));
		case (1):
			return Integer.toString(nX);
		case (2):
			return Integer.toString(nY);
		case (3):
			return Integer.toString(nZ);
		case (4):
			return baseElem.toString();
		default:
			return "";
		}
	}

	public String toString() {
		String S = new String();
		for (int i = 0; i < paramNames.length; i++) {
			S += paramNames[i] + ", ";
		}
		S += "\r\n";
		for (int i = 0; i < paramNames.length; i++) {
			S += getParam(i) +", ";
		}
		S += "\r\n";
		return S;
	}

	private void setInitConfig(Config newConfig) {
		this.initial = newConfig;
	}
	
	private static Parameters loadParameters(String dir, String filename) throws IOException {
		File file = Paths.get(dir, filename + ".txt").toFile();
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		long[] intParam = new long[Parameters.nIntParam];
		
		for (int i = 0; i < intParam.length; i++) {
			String s = in.readLine();
			intParam[i] = Integer.parseInt(s);
		}
		
		in.close();
		return new Parameters(intParam);
	}
}
