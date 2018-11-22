
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import main.*;
import processing.core.PApplet;

import constants.Crystal;
import constants.Element;

public class simulationRunner {
	
	private static String name = "Sim";
	private static String configFilename = "config";
	private static File location = new File("..\\..\\");
	private static final File logFile = new File("C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\logFile.txt");

	private static boolean saveConfig = false;
	private static boolean loadConfig = false;

	private static Visualizer vis;
	private static Simulator[] sim;
	private static Thread[] threads;
	private static boolean parallel = true;
	private static Parameters commonParam = null;

	public static void main(String[] args) throws InterruptedException, IOException {

		Scanner in = new Scanner(System.in);
		Parameters[] paramRange = null;
		System.out.println("Welcome to magnetic monte carlo Markov chain simulator utility");
		
		while (in.hasNext()) {
			String line = in.nextLine();
			args = line.split(" ");
			if (args.length < 1)
				break;
			String command = args[0];
			if (args[0].charAt(0) == '#')
				continue;
			switch (command) {
			case "setCommon":
				commonParam = initCommon(args);
				break;
			case "scanT":
				paramRange = scanT(args);
				break;
			case "scanHx":
				paramRange = scanH(args, 0);
				break;
			case "scanHy":
				paramRange = scanH(args, 1);
				break;
			case "scanHz":
				paramRange = scanH(args, 2);
				break;
			case "scanTHx":
				paramRange = scanTH(args, 0);
				break;
			case "scanTHy":
				paramRange = scanTH(args, 1);
				break;
			case "scanTHz":
				paramRange = scanTH(args, 2);
				break;
			case "printParam":
				printParam(paramRange);
				break;
				
			case "setDir":
				String newDirToSet = args[1];
				setDir(newDirToSet);
				break;
			case "printDir":
				System.out.println(location.getCanonicalPath());
				break;
			case "mkDir":
				String newDirToCreate = args[1];
				mkDir(newDirToCreate);
				break;
			case "runSim":
				System.out.println("Running simulations: ");
				try {
					runSimul(paramRange);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "help":
				writeHelp();
				break;
			case "exit":
			case "":
				System.out.println("Program exits.");
				System.exit(0);
				break;
			default:
				System.out.println("Command not recognized.");
				writeHelp();
				break;
			}
		}
		in.close();
		System.exit(0);
	}

	private static void printParam(Parameters[] paramRange) {
		for (int i = 0; i < paramRange.length; i++) {
			System.out.println(paramRange[i].toString());
		}
	}

	private static void setDir(String newDirName) throws IOException {
		
		File newDir = Paths.get(location.getCanonicalFile().toString(), newDirName).toFile();

		if (newDir.exists() && newDir.isFile()) {
			System.out.println("Warning: The specified location is file. Cannot set the directory");
		} else if (newDir.exists() && newDir.isDirectory()){
			location = newDir;
			System.out.println("Directory set to " + location.getCanonicalPath());
		} else {
			System.out.println("The specified directory does not exists, creates a new one.");
			mkDir(newDirName);
		}
	}

	private static void mkDir(String newDirName) throws IOException {		
		File newDir = Paths.get(location.getCanonicalFile().toString(), newDirName).toFile();
		
		boolean success = newDir.mkdir();
		if (success) {
			System.out.println("Successfully created directory " + location.getCanonicalPath());
		} else {
			System.out.println("Could not create " + location.getCanonicalPath()
					+ " a file or directory with that name already exists.");
			setDir(newDirName);
		}
	}

	private static void runSimul(Parameters[] paramRange) throws InterruptedException, IOException {
		
		if (!location.exists() || !location.isDirectory()) {
			throw new IOException();
		}
		
		long startTime = System.currentTimeMillis();
		if (parallel) {
			runParallel(paramRange, startTime);
		} else {
			runSequential(paramRange, startTime);
		}
		System.out.println("Total time taken: " + formatTime(System.currentTimeMillis() - startTime));
	}

	private static Parameters[] scanTH(String[] args, int coord) throws InterruptedException, IOException {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double tStart = Double.parseDouble(args[1]);
			double dT = Double.parseDouble(args[2]);
			int nT = Integer.parseInt(args[3]);

			double hStart = Double.parseDouble(args[4]);
			double dH = Double.parseDouble(args[5]);
			int nH = Integer.parseInt(args[6]);

			Parameters[] paramRange = new Parameters[nT*nH];
			for (int t = 0; t < nT; t++) {
				for (int h = 0; h < nH; h++) {
					paramRange[t] = commonParam.clone();
					paramRange[t].temp = tStart + t * dT;
					paramRange[t].H.setCoord(hStart + h * dH, coord);
				}
			}
			return paramRange;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Not enough arguments.");
			System.out.println(e.toString());
			writeHelp();
		} catch (NumberFormatException e) {
			System.out.println("Arguments values not of the right format.");
			System.out.println(e.toString());
			writeHelp();
		}
		return null;
	}

	private static Parameters[] scanT(String[] args) throws InterruptedException, IOException {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double tStart = Double.parseDouble(args[1]);
			double dT = Double.parseDouble(args[2]);
			int nT = Integer.parseInt(args[3]);

			Parameters[] paramRange = new Parameters[nT];
			for (int n = 0; n < nT; n++) {
				paramRange[n] = commonParam.clone();
				paramRange[n].temp = tStart + n * dT;
				System.out.println(paramRange[n].toString());
			}
			return paramRange;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Not enough arguments.");
			System.out.println(e.toString());
			writeHelp();
		} catch (NumberFormatException e) {
			System.out.println("Arguments values not of the right format.");
			System.out.println(e.toString());
			writeHelp();
		}
		return null;
	}

	private static Parameters[] scanH(String[] args, int coord) throws InterruptedException {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double hStart = Double.parseDouble(args[1]);
			double dH = Double.parseDouble(args[2]);
			int nH = Integer.parseInt(args[3]);

			Parameters[] paramRange = new Parameters[nH];
			for (int n = 0; n < nH; n++) {
				paramRange[n] = commonParam.clone();
				paramRange[n].H.setCoord(hStart + n * dH, coord);
			}
			return paramRange;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Not enough arguments.");
			System.out.println(e.toString());
			writeHelp();
		} catch (NumberFormatException e) {
			System.out.println("Arguments not of the right type.");
			System.out.println(e.toString());
			writeHelp();
		}
		return null;
	}

	private static Parameters initCommon(String[] args) {
		if (args.length > 7) {
			throw new ArrayIndexOutOfBoundsException("Too many arguments for common parameters.");
		} else if(args.length < 7) {
			throw new NullPointerException("Not enough arguments to initialize common parameters.");
		}
		Parameters commonParam = new Parameters();
		commonParam.nSteps = Integer.parseInt(args[1]);
		commonParam.aggregate = Integer.parseInt(args[2]);
		commonParam.nX = Integer.parseInt(args[3]);
		commonParam.nY = Integer.parseInt(args[4]);
		commonParam.nZ = Integer.parseInt(args[5]);
		commonParam.baseElem = Element.valueOf(args[6]);
//		commonParam.H.x = Double.parseDouble(args[6]);
//		commonParam.H.y = Double.parseDouble(args[7]);
//		commonParam.H.z = Double.parseDouble(args[8]);
		return commonParam;
	}

	private static void writeHelp() {
//		try {
//			Scanner sc = new Scanner(new File("help.txt"));
//			while (sc.hasNextLine()) {
//				String string = (String) sc.nextLine();
//				System.out.println(string);
//			}
//			sc.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		System.out.println("sim.jar.\r\n" + 
				"	list of commands:\r\n" + 
				"	\r\n" + 
				"	\r\n" + 
				"		-help, 			Writes this text.\r\n" + 
				"\r\n" + 
				"		-setCommon 		Sets the common parameters for the simulations\r\n" +
				"			Format:		nSteps nAggre nx ny nz\r\n" + 
				"\r\n" +
				"		-scanT			Makes a scan ramping up or down the temp field.\r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, T_start, dT, nT, Hx, Hy, Hz\r\n" + 
				"\r\n" + 
				"		-scanHx			Makes a scan ramping up or down the Hx field.\r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, Hx_start, dHx, nHx, temp, Hy, Hz\r\n" + 
				"\r\n" + 
				"		-scanHy\r\n" + 
				"			Makes a scan ramping up or down the Hy field.\r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, Hy_start, dHz, nHy, temp, Hx, Hz\r\n" + 
				"		\r\n" + 
				"		-scanH\r\n" + 
				"			Makes a scan ramping up or down the Hz field.\r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, Hz_start, dHz, nHz, temp, Hx, Hy\r\n" + 
				"\r\n" + 
				"		-scanTHx\r\n" + 
				"			Makes a scan ramping up or down the Hz field.\r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, Hx_start, dHx, nHx, T_start, dT, nT, Hy, Hz\r\n" + 
				"\r\n" + 
				"		-scanTHy\r\n" + 
				"			Makes a scan ramping up or down the Hz field. \r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, Hy_start, dH, nHy, T_start, dT, nT, Hx, Hz\r\n" + 
				"					\r\n" + 
				"		-scanTHz\r\n" + 
				"			Makes a scan ramping up or down the Hz field. \r\n" + 
				"			Arguments after the command should be in the format:\r\n" + 
				"				outputLoc, nX, nY, nZ, Hz_start, dHz, nHz, T_start, dT, nT, Hx, Hy\r\n" + 
				"					");
	}

	private static void runSequential(Parameters[] paramRange, long startTime)
			throws InterruptedException, IOException {
		File[] configFiles = new File[paramRange.length];

		for (int i = 0; i < paramRange.length; i++) {
			File file = findFilename(location, name);
			if (loadConfig || saveConfig) {
				String configFile = location + name + String.format("_config_(%d,%d,%d)_T=%1.2f_Hb=%1.2f.txt",
						paramRange[i].nX, paramRange[i].nY, paramRange[i].nZ, paramRange[i].temp, paramRange[i].H.z);
				configFiles[i] = new File(configFile);
			}

			if (loadConfig) {
				sim[i] = new Simulator(paramRange[i], file, configFiles[i]);
			} else {
				sim[i] = new Simulator(paramRange[i], file);
			}
		}

		for (int i = 0; i < paramRange.length; i++) {
			Thread thread = new Thread(sim[i]);

			thread.start();
			waitMessage(startTime, i);
			thread.join();

			writeToLogFile(sim[i], formatTime(System.currentTimeMillis() - startTime));
			sim[i].saveConfig(configFiles[i]);
			if (i + 1 != sim.length) {
				sim[i + 1].loadConfig(configFiles[i]);
			}
		}
	}

	private static void runParallel(Parameters[] paramRange, long startTime) throws InterruptedException, IOException {
		File[] configFiles = new File[paramRange.length];

		sim = new Simulator[paramRange.length];
		threads = new Thread[paramRange.length];

		for (int i = 0; i < paramRange.length; i++) {
			File file = findFilename(location, name);
			if (loadConfig || saveConfig) {
				String configFile = location + name + String.format("_config_(%d,%d,%d)_T=%1.2f_Hb=%1.2f.txt",
						paramRange[i].nX, paramRange[i].nY, paramRange[i].nZ, paramRange[i].temp, paramRange[i].H.z);
				configFiles[i] = new File(configFile);
			}

			if (loadConfig) {
				sim[i] = new Simulator(paramRange[i], file, configFiles[i]);
			} else {
				sim[i] = new Simulator(paramRange[i], file);
			}

			sim[i].setElementFraction(Element.Ni, 1.0);
			sim[i].configFromBasisState(Crystal.Cz); // Forces a basis configuration onto
			// the state
			threads[i] = new Thread(sim[i]);
			threads[i].start();
		}

		// Visualizing:
//		int vIndex = 0;
//		vis = new Visualizer(sim[vIndex]);
//		String[] myArgs = { "test.Visualizer" };
//		PApplet.runSketch(myArgs, vis);
		
		// Waiting message:
		waitMessage(startTime, 0);
		for (int i = 0; i < paramRange.length; i++) {
			threads[i].join();
			writeToLogFile(sim[i], Long.toString(System.currentTimeMillis() - startTime));
			if (saveConfig) {
				sim[i].saveConfig(configFiles[i]);
			}
		}
//		vis.exit();
	}

	private static void waitMessage(long startTime, int i) throws InterruptedException {
		while (threads[i].isAlive()) {
			double progress = sim[i].step / (double) sim[i].param.nSteps;
			System.out.format("Simulation progress: %1.4f", progress);
			long elapsed = System.currentTimeMillis() - startTime;
			long ETA = (long) (elapsed * (1 / progress - 1));

			System.out.println(", estimated finish time: " + formatTime(ETA));
			Thread.sleep(1000);
		}
	}

	private static String formatTime(long ETA) {
		return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(ETA),
				TimeUnit.MILLISECONDS.toSeconds(ETA)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ETA)));
	}

	private static void writeToLogFile(Simulator sim, String timeTaken) {
		System.out.println("Prints additional data to path: " + logFile.getAbsolutePath());
		try {
			FileWriter writer = new FileWriter(logFile, true);
			writer.write("File:" + sim.outputFile.getName() + " has rejection ratio "
					+ ((double) sim.nRejects / (double) sim.param.nSteps) + " and time taken " + timeTaken + "\n");
			writer.close();
		} catch (IOException e) {
			System.err.println("Caught Exception: " + e.getMessage());
		}
	}

	private static File findFilename(File location, String filename) throws IOException {
		File file = null;
		for (int i = 0; i < 100; i++) {
			file = Paths.get(location.getPath(), filename + "_" + i + ".txt").toFile();
			if (!file.exists()) {
				break;
			}
		}
		file.createNewFile();
		return file;
	}

}
