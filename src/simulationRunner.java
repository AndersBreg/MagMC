
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import constants.BasisState;
import constants.Element;
import main.Parameters;
import main.Simulator;
import main.Visualizer;
import processing.core.PApplet;

public class simulationRunner {
	
	private static String name = "Sim";
	private static char sep = File.separatorChar;
	private static File parentDir = new File(".."+sep);
	private static File configFile;
	private static File defaultDataDir = new File(parentDir, "."+sep+"Data"+sep);
	private static File curDir = new File(defaultDataDir.getAbsolutePath());
	private static final File logFile = new File("."+sep+"logFile.txt");

	private static boolean useConfig = false;
	private static boolean saveConfig = false;

	private static boolean visualizing = false;
	private static Visualizer vis;
	private static Simulator[] sim;
	private static Thread[] threads;
	private static boolean parallel = false;
	private static Parameters commonParam = null;

	public static void main(String[] args) throws InterruptedException, IOException {

		parentDir = new File(args[0]);
		defaultDataDir = new File(parentDir, "Data");
		curDir = new File(defaultDataDir.getAbsolutePath());
		
		Scanner in = new Scanner(System.in);
		Parameters[] paramRange = null;
		System.out.println("Welcome to magnetic monte carlo Markov chain simulator utility");
		
		while (in.hasNext()) {
			String line = in.nextLine();
			System.out.println(line);
			String[] lineArgs = line.split(" ");
			if (lineArgs.length < 1)
				break;
			String command = lineArgs[0];
			if (lineArgs[0].charAt(0) == '#')
				continue;
			
			switch (command) {
			case "setCommon":
				commonParam = initCommon(lineArgs);
				break;
			case "setVars":
			case "setCommonVars":
				setCommonVars(commonParam, lineArgs);
				break;
			case "scanT":
				paramRange = scanT(lineArgs);
				break;
			case "scanBx":
				paramRange = scanB(lineArgs, 0);
				break;
			case "scanBy":
				paramRange = scanB(lineArgs, 1);
				break;
			case "scanBz":
				paramRange = scanB(lineArgs, 2);
				break;
				
			case "scanAngleBYZ":
			case "scanAngleYZ":
				paramRange = scanBangle(lineArgs, 0);
				break;
			case "scanAngleBZX":
			case "scanAngleZX":
			case "scanAngleXZ":
				paramRange = scanBangle(lineArgs, 1);
				break;
			case "scanAngleBXY":
			case "scanAngleXY":
				paramRange = scanBangle(lineArgs, 2);
				break;
				
			case "scanTBx":
				paramRange = scanTB(lineArgs, 0);
				break;
			case "scanTBy":
				paramRange = scanTB(lineArgs, 1);
				break;
			case "scanTBz":
				paramRange = scanTB(lineArgs, 2);
				break;
			case "printParam":
				printParam(paramRange);
				break;
				
			case "setDir":
				String newDirToSet = lineArgs[1];
				setDir(newDirToSet);
				break;
			case "printDir":
				System.out.println(curDir.getCanonicalPath());
				break;
			case "resetDir":
				curDir = defaultDataDir;
				System.out.println("Path reset to " + curDir.getCanonicalPath());
				break;
			case "mkDir":
				String newDirToCreate = lineArgs[1];
				mkDir(newDirToCreate);
				break;
				
			case "loadConfigFile":
			case "loadConfFile":
				if (!lineArgs[1].endsWith(".txt")) {
					lineArgs[1] = lineArgs[1]+".txt";
				}
				File file = Paths.get(curDir.getPath(), lineArgs[1]).toFile();
				System.out.println("Looking for file: " + file.getCanonicalPath());
				if (!file.exists()) {
					System.out.println("The specified configuration file does not exist.");
				}
				configFile = file;
				useConfig = true;
				System.out.println("Loaded in config file: " + configFile.getName());
//				Scanner confFilePrinter = new Scanner(configFile);
//				while (confFilePrinter.hasNext()) {
//					String string = (String) confFilePrinter.next();
//					System.out.println(string);
//				}
				break;
			case "saveConfig":
			case "saveConfigFile":
			case "saveConfigCopy":
				if (!lineArgs[1].endsWith(".txt")) {
					lineArgs[1] = lineArgs[1]+".txt";
				}
				Path p = Paths.get(curDir.toString(), lineArgs[1]);
				Files.copy(configFile.toPath(), p, StandardCopyOption.REPLACE_EXISTING );
				break;
			case "enableConfFile":
			case "enableConfigFile":
				useConfig = true;
				break;
			case "disableConfFile":
			case "disableConfigFile":
				useConfig = false;
				break;
			case "toggleVis":
			case "toggleVisualizer":
			case "toggleVisualiser":
				visualizing = !visualizing;
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
			case "h":
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
		
		File newDir = Paths.get(defaultDataDir.getCanonicalFile().toString(), newDirName).toFile();

		if (newDir.exists() && newDir.isFile()) {
			System.out.println("Warning: The specified location is a file. Cannot set the directory");
			throw new IOException();
		} else if (newDir.exists() && newDir.isDirectory()){
			curDir = newDir;
			System.out.println("Directory set to " + curDir.getCanonicalPath());
		} else {
			System.out.println("The specified directory does not exists, creates a new one.");
			try {
				mkDir(newDirName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void mkDir(String newDirName) throws IOException {		
		File newDir = Paths.get(defaultDataDir.getCanonicalFile().toString(), newDirName).toFile();
		System.out.println("Trying to create directory: " + newDir.toString());
		
		boolean success = newDir.mkdirs();
		if (success) {
			System.out.println("Successfully created directory " + newDir.getCanonicalPath());
			curDir = newDir;
		} else {
			System.out.println("Could not create " + newDir.getCanonicalPath()
					+ " a file or directory with that name already exists.");
			try {
				setDir(newDirName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void runSimul(Parameters[] paramRange) throws InterruptedException, IOException {
		
		if (!curDir.exists() || !curDir.isDirectory()) {
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

	private static Parameters[] scanT(String[] args) throws InterruptedException, IOException {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double tStart = Double.parseDouble(args[1]);
			double dT = Double.parseDouble(args[2]);
			double tEnd = Integer.parseInt(args[3]);
			int nT = (int) ((tEnd - tStart)/dT); 

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

	private static Parameters[] scanB(String[] args, int coord) throws InterruptedException {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double hStart = Double.parseDouble(args[1]);
			double dH = Double.parseDouble(args[2]);
			double hEnd = Integer.parseInt(args[3]);
			int nH = (int) ((hEnd - hStart)/dH);
			
			Parameters[] paramRange = new Parameters[nH];
			for (int n = 0; n < nH; n++) {
				paramRange[n] = commonParam.clone();
				paramRange[n].B.setCoord(hStart + n * dH, coord);
				System.out.println(paramRange[n].toString());
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

	private static Parameters[] scanBangle(String[] args, int i) {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			
			double angStart = Double.parseDouble(args[1]);
			double dAng = Double.parseDouble(args[2]);
			double angEnd = Integer.parseInt(args[3]);
			double field = Double.parseDouble(args[4]);
			int nAng = (int) ((angEnd - angStart)/dAng);
			
			Parameters[] paramRange = new Parameters[nAng];
			for (int n = 0; n < nAng; n++) {
				paramRange[n] = commonParam.clone();
				double ang = Math.toRadians(angStart + n * dAng);
				paramRange[n].B.setCoord(field * Math.cos(ang), (i+1) % 3);
				paramRange[n].B.setCoord(field * Math.sin(ang), (i+2) % 3);
				System.out.println(paramRange[n].toString());
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

	private static Parameters[] scanTB(String[] args, int coord) throws InterruptedException, IOException {
		try {
			if (commonParam == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double tStart = Double.parseDouble(args[1]);
			double dT = Double.parseDouble(args[2]);
			double tEnd = Double.parseDouble(args[3]);
			int nT = (int) ((tEnd - tStart)/dT);
	
			double hStart = Double.parseDouble(args[4]);
			double dH = Double.parseDouble(args[5]);
			double hEnd = Double.parseDouble(args[6]);
			int nH = (int) ((hEnd - hStart)/dH);
			
			Parameters[] paramRange = new Parameters[nT*nH];
			for (int t = 0; t < nT; t++) {
				for (int h = 0; h < nH; h++) {
					paramRange[h+t*nH] = commonParam.clone();
					paramRange[h+t*nH].temp = tStart + t * dT;
					paramRange[h+t*nH].B.setCoord(hStart + h * dH, coord);
					System.out.println(paramRange[h+t*nH].toString());
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

	private static Parameters initCommon(String[] args) {
		if (args.length > 8) {
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
		if (args.length == 8) {
			commonParam.initState = BasisState.valueOf(args[7]);
		}
		return commonParam;
	}
	
	private static void setCommonVars(Parameters param, String[] args) {
		if (args.length > 5) {
			throw new ArrayIndexOutOfBoundsException("Too many arguments for common parameters.");
		} else if(args.length < 5) {
			throw new NullPointerException("Not enough arguments to initialize common parameters.");
		}
		param.temp = Double.parseDouble(args[1]);
		param.B.x = Double.parseDouble(args[2]);
		param.B.y= Double.parseDouble(args[3]);
		param.B.z = Double.parseDouble(args[4]);
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
				"			Format:		nSteps nAggre nx ny nz Elem [ Base_State ]\r\n" + 
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

		sim = new Simulator[paramRange.length];
		threads = new Thread[paramRange.length];
		
		for (int i = 0; i < paramRange.length; i++) {
			File file = findFilename(curDir, name);

			if (useConfig && configFile != null && configFile.exists()) {
				sim[i] = new Simulator(paramRange[i], file, configFile);
			} else {
				sim[i] = new Simulator(paramRange[i], file);
				configFile = curDir.toPath().resolve(name+"_end_config.txt").toFile(); 
			}
			if (paramRange[i].initState != null) {
				sim[i].configFromBasisState(paramRange[i].initState); // Forces a basis configuration onto
				// the state
			}
		}

		for (int i = 0; i < paramRange.length; i++) {
			threads[i] = new Thread(sim[i]);

			threads[i].start();
			if (visualizing) {
				int vIndex = i;
				vis = new Visualizer(sim[vIndex]);
				if (i == 0) {
					String[] myArgs = { "test.Visualizer" };
					PApplet.runSketch(myArgs, vis);	
				}
			}

			waitMessageSequential(startTime, i, paramRange.length);
			threads[i].join();

			writeToLogFile(sim[i], formatTime(System.currentTimeMillis() - startTime));
			sim[i].saveConfig(configFile);
			if (i + 1 != sim.length) {
				sim[i + 1].loadConfig(configFile);
			}
		}
	}

	private static void runParallel(Parameters[] paramRange, long startTime) throws InterruptedException, IOException {

		sim = new Simulator[paramRange.length];
		threads = new Thread[paramRange.length];

		for (int i = 0; i < paramRange.length; i++) {
			File file = findFilename(curDir, name);
			if (configFile.exists()) {
				sim[i] = new Simulator(paramRange[i], file, configFile);
			} else {
				sim[i] = new Simulator(paramRange[i], file);
			}
			if (paramRange[i].initState != null) {
				sim[i].configFromBasisState(paramRange[i].initState); // Forces a basis configuration onto
				// the state	
			}
			threads[i] = new Thread(sim[i]);
			threads[i].start();
		}

		// Visualizing:
		if (visualizing) {
			int vIndex = 0;
			vis = new Visualizer(sim[vIndex]);
			String[] myArgs = { "test.Visualizer" };
			PApplet.runSketch(myArgs, vis);
		}
		
		// Waiting message:
		waitMessageParallel(startTime, 0);
		for (int i = 0; i < paramRange.length; i++) {
			threads[i].join();
			writeToLogFile(sim[i], Long.toString(System.currentTimeMillis() - startTime));
			if (saveConfig) {
				sim[i].saveConfig(configFile);
			}
		}
//		vis.exit();
	}

	private static void waitMessageParallel(long startTime, int i) throws InterruptedException {
		while (threads[i].isAlive()) {
			double progress = sim[i].step / (double) sim[i].param.nSteps;
			System.out.format("Simulation progress: %1.4f", progress);
			long elapsed = System.currentTimeMillis() - startTime;
			long ETA = (long) (elapsed * (1 / progress - 1));

			System.out.println(", estimated finish time: " + formatTime(ETA));
			Thread.sleep(2000);
		}
	}

	private static void waitMessageSequential(long startTime, int i, int paramRangeLength) throws InterruptedException {
		while (threads[i].isAlive()) {
			double progress = (sim[i].step / (double) sim[i].param.nSteps + i) / paramRangeLength;
			System.out.format("Simulation progress: %1.4f", progress);
			long elapsed = System.currentTimeMillis() - startTime;
			long ETA = (long) (elapsed * (1 / progress - 1));

			System.out.println(", estimated finish time: " + formatTime(ETA));
			Thread.sleep(2000);
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
		for (int i = 0; i < 10000; i++) {
			file = location.toPath().resolve(filename + "_" + i + ".txt").toFile();
			if (!file.exists()) {
				break;
			}
		}
		file.createNewFile();
		return file;
	}
}
