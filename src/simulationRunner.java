
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import constants.BasisState;
import constants.Element;
import main.Parameters;
import main.Simulator;
import main.Variables;
import main.Visualizer;
import processing.core.PApplet;

public class simulationRunner {
	
	private static String name = "Sim";
	private static char sep = File.separatorChar;
	private static File parentDir;
	private static File configFile;
	private static File defaultDataDir;
	private static File curDir;
	private static final File logFile = new File("."+sep+"logFile.txt");

	private static boolean useConfig = false;
	private static boolean saveConfig = false;

	private static boolean visualizing = false;
	private static boolean[] simToVisualize;
	private static Visualizer vis;
	private static boolean parallel = false;
	private static Parameters common = null;
	private static Variables commonVars = null;
	private static boolean saveAllConfig = false;
	private static boolean periodicBoundaries = true;
	
	private static ArrayList<Variables> varRange = new ArrayList<Variables>();
	private static Simulator sim;
	private static File outputFile;
	private static int repeat = 1;
	private static boolean visExists = false;

	public static void main(String[] args) throws InterruptedException, IOException {

		parentDir = new File("." + sep);
		defaultDataDir = new File(parentDir, ".");
		curDir = new File(defaultDataDir.getAbsolutePath());
		
		Scanner in = new Scanner(System.in);
		Variables[] newVarRange = null;
		
		System.out.println("Welcome to magnetic monte carlo Markov chain simulator utility");
		while (in.hasNext()) {
			String line = in.nextLine();
			System.out.println("Executing command: " + line);
			String[] lineArgs = line.split(" ");
			if (lineArgs.length < 1)
				break;
			String command = lineArgs[0];
			if (lineArgs[0].charAt(0) == '#')
				continue;
			
			switch (command.toLowerCase()) {
			case "setparam":
			case "setparameters":
			case "setcommonparam":
			case "setcommonparameters":
				common = setCommonParam(lineArgs);
				break;
			case "setvars":
			case "setvariables":
			case "setcommonvars":
			case "setcommonvariables":
				commonVars = setCommonVars(lineArgs);
				break;
			case "scant":
				newVarRange = scanT(lineArgs);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scanpoint":
				newVarRange = scanPoint(lineArgs);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scanbx":
			case "scanba":
				newVarRange = scanB(lineArgs, 0);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scanby":
			case "scanbb":
				newVarRange = scanB(lineArgs, 1);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scanbz":
			case "scanbc":
				newVarRange = scanB(lineArgs, 2);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
				
			case "scananglebyz":
			case "scanangleyz":
				newVarRange = scanBangle(lineArgs, 0);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scananglebzx":
			case "scananglebxz":
			case "scananglezx":
			case "scananglexz":
				newVarRange = scanBangle(lineArgs, 1);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scananglebxy":
			case "scananglexy":
				newVarRange = scanBangle(lineArgs, 2);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
				
			case "scantbx":
				newVarRange = scanTB(lineArgs, 0);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scantby":
				newVarRange = scanTB(lineArgs, 1);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "scantbz":
				newVarRange = scanTB(lineArgs, 2);
				varRange.addAll(Arrays.asList(newVarRange));
				break;
			case "repeat":
			case "setrepeat":
			case "enablerepeat":
				repeat  = Integer.parseInt(lineArgs[1]);
				break;
			case "printparam":
			case "printcommon":
			case "printcommonparam":
			case "printcommonparameters":
				printParam(common);
				break;
			case "printvar":
			case "printvars":
				printVars(varRange);
				break;	
			case "setdir":
				String newDirToSet = lineArgs[1];
				setDir(newDirToSet);
				break;
			case "printdir":
//				System.out.println(curDir.getCanonicalPath());
				break;
			case "resetdir":
				curDir = defaultDataDir;
//				System.out.println("Path reset to " + curDir.getCanonicalPath());
				break;
			case "mkdir":
				String newDirToCreate = lineArgs[1];
				mkDir(newDirToCreate);
				break;
				
			case "loadconfigfile":
			case "loadconffile":
			case "loadconfig":
			case "loadconf":
				if (!lineArgs[1].endsWith(".conf")) {
					lineArgs[1] = lineArgs[1]+".conf";
				}
				File file = Paths.get(curDir.getPath(), lineArgs[1]).toFile();
				System.out.println("Looking for file: " + file.getCanonicalPath());
				if (!file.exists()) {
					System.out.println("The specified configuration file does not exist.");
				}
				configFile = file;
				useConfig = true;
				System.out.println("Loaded in config file: " + configFile.getName());
				break;
			case "saveconf":
			case "saveconfig":
			case "saveconfigfile":
			case "saveconfigcopy":
				if (!lineArgs[1].endsWith(".txt")) {
					lineArgs[1] = lineArgs[1]+".txt";
				}
				Path p = Paths.get(curDir.toString(), lineArgs[1]);
				System.out.println("Saves current configuration to:" + p.toString());
				Files.copy(configFile.toPath(), p, StandardCopyOption.REPLACE_EXISTING );
				configFile = p.toFile();
				break;
			case "savecur":
			case "savecurconf":
			case "savecurconfig":
			case "savecurrentconf":
			case "savecurrentconfig":
				System.out.println("Saving current configuration.");
				if (sim != null) {
					if (!lineArgs[1].endsWith(".conf")) {
						lineArgs[1] = lineArgs[1]+".conf";
					}
					File newConfigFile = Paths.get(curDir.toString(), lineArgs[1]).toFile();
					PrintStream out = new PrintStream(newConfigFile);
					sim.saveConfig(out);
				}
				break;
			case "enableconf":
			case "enableconfig":
			case "enableconffile":
			case "enableconfigfile":
				useConfig = true;
				configFile = Paths.get(curDir.toString(), lineArgs[1]).toFile();
				break;
			case "disableconf":
			case "disableconfig":
			case "disableconffile":
			case "disableconfigfile":
				useConfig = false;
				break;
			case "enablesaveallconfigs":
			case "enablesaveallconfig":
			case "enablesaveallconf":
			case "enablesaveall":
			case "saveallconfig":
				saveAllConfig = true;
				break;
			case "disablesaveallconfigs":
			case "disablesaveallconfig":
			case "disablesaveallconf":
			case "disablesaveall":
				saveAllConfig = false;
				break;
				
			case "disablevis":
			case "disablevisualizer":
			case "disablevisualiser":
			case "disablevisualizing":
			case "disablevisualising":
				visualizing = false;
				break;
			case "enablevis":
			case "enablevisualizer":
			case "enablevisualiser":
			case "enablevisualizing":
			case "enablevisualising":
				visualizing = true;
				break;
			case "disableperiodic":
				periodicBoundaries = false;
				break;
			case "enableperiodic":
				periodicBoundaries = true;
				break;
			
			case "run":
			case "runsim":
//				System.out.println("Running simulations: ");
				try {
					if (lineArgs.length > 1) {
						outputFile = new File(lineArgs[1]);
						if(outputFile.exists()) {
							throw new IOException("Outputfile already exists.");
						}
					}
					runSimul(common, varRange);
					varRange.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "modifyelement":
			case "modifyelem":
			case "modify":
				modifyElement(lineArgs);
				break;
			case "help":
			case "h":
//				writeHelp();
				break;
			case "wait":
				Thread.sleep((long) 10000);
			case "exit":
			case "":
//				System.out.println("Program exits.");
				System.exit(0);
				break;
			default:
				System.out.println("Command not recognized.");
//				writeHelp();
				break;
			}
		}
		in.close();
		System.exit(0);
	}
	

	private static void printVars(List<Variables> vars) {
		for (Iterator<Variables> iterator = vars.iterator(); iterator.hasNext();) {
			System.out.println(iterator.next());
		}
	}

	private static void modifyElement(String[] args) {
		String elemString = args[1];
		Element elem = Element.valueOf(elemString);
		int parameter = Integer.parseInt(args[2]);
		double value = Double.parseDouble(args[3]);
//		System.out.println("Changed element: " + elem);
//		System.out.println("Old parameters: " + elem.paramString());
		elem.modify(parameter, value);
//		System.out.println("New parameters: " + elem.paramString());
	}

	private static void printParam(Parameters param) {
		System.out.println(param.toString());
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

	private static void runSimul(Parameters param, List<Variables> varRange) throws InterruptedException, IOException {
		
//		if (!curDir.exists() || !curDir.isDirectory()) {
//			throw new IOException();
//		}
		
		long startTime = System.currentTimeMillis();
		runSequential(param, varRange, startTime);
//		System.out.println("Total time taken: " + formatTime(System.currentTimeMillis() - startTime));
	}

	private static Variables[] scanT(String[] args) throws InterruptedException, IOException {
		try {
			if (commonVars == null) {
				throw new NullPointerException("Common variables is not yet set.");
			}
			double tStart = Double.parseDouble(args[1]);
			double dT = Double.parseDouble(args[2]);
			double tEnd = Double.parseDouble(args[3]);
			int nT = 1 + (int) ((tEnd - tStart)/dT); 

			Variables[] varRange = new Variables[nT*repeat];
			for (int n = 0; n < nT; n++) {
				for (int rep = 0; rep < repeat; rep++) {
					varRange[rep + n*repeat] = new Variables(commonVars);
					varRange[rep + n*repeat].temp = tStart + n * dT;
//				System.out.println(varRange[n].toString());
				}
			}
			return varRange;
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

	private static Variables[] scanPoint(String[] args) {
		try {
			if (commonVars == null) {
				throw new NullPointerException("Common variables is not yet set.");
			}
			int repeats = Integer.parseInt(args[1]);
			Variables[] varRange = new Variables[repeats];
			for (int n = 0; n < repeats; n++) {
				varRange[n] = new Variables(commonVars);
			}
			return varRange;
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
	
	private static Variables[] scanB(String[] args, int coord) throws InterruptedException {
		try {
			if (common == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double hStart = Double.parseDouble(args[1]);
			double dH = Double.parseDouble(args[2]);
			double hEnd = Double.parseDouble(args[3]);
			int nH = 1+(int) ((hEnd - hStart)/dH);
			
			Variables[] varRange = new Variables[nH];
			for (int n = 0; n < nH; n++) {
				varRange[n] = new Variables(commonVars);
				varRange[n].B.setCoord(hStart + n * dH, coord);
//				System.out.println(varRange[n].toString());
			}
			return varRange;
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

	private static Variables[] scanBangle(String[] args, int i) {
		try {
			if (common == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			
			double angStart = Double.parseDouble(args[1]);
			double dAng = Double.parseDouble(args[2]);
			double angEnd = Double.parseDouble(args[3]);
			double field = Double.parseDouble(args[4]);
			int nAng = 1 + (int) ((angEnd - angStart)/dAng);
			
			Variables[] varRange = new Variables[nAng];
			for (int n = 0; n < nAng; n++) {
				varRange[n] = new Variables(commonVars);
				double ang = Math.toRadians(angStart + n * dAng);
				varRange[n].B.setCoord(field * Math.cos(ang), (i+1) % 3);
				varRange[n].B.setCoord(field * Math.sin(ang), (i+2) % 3);
//				System.out.println(varRange[n].toString());
			}
			return varRange;
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

	private static Variables[] scanTB(String[] args, int coord) throws InterruptedException, IOException {
		try {
			if (common == null) {
				throw new NullPointerException("Common Parameters is not set.");
			}
			double tStart = Double.parseDouble(args[1]);
			double dT = Double.parseDouble(args[2]);
			double tEnd = Double.parseDouble(args[3]);
			int nT = 1 + (int) ((tEnd - tStart)/dT);
	
			double hStart = Double.parseDouble(args[4]);
			double dH = Double.parseDouble(args[5]);
			double hEnd = Double.parseDouble(args[6]);
			int nH = 1 + (int) ((hEnd - hStart)/dH);
			
			Variables[] varRange = new Variables[nT*nH];
			for (int t = 0; t < nT; t++) {
				for (int h = 0; h < nH; h++) {
					varRange[h+t*nH] = new Variables();
					varRange[h+t*nH].temp = tStart + t * dT;
					varRange[h+t*nH].B.setCoord(hStart + h * dH, coord);
//					System.out.println(varRange[h+t*nH].toString());
				}
			}
			return varRange;
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

	private static Parameters setCommonParam(String[] args) {
		if (args.length > 7) {
			throw new ArrayIndexOutOfBoundsException("Too many arguments for common parameters.");
		} else if(args.length < 6) {
			throw new NullPointerException("Not enough arguments to initialize common parameters.");
		}
		Parameters commonParam = new Parameters();
		int MonteCarloSteps = Integer.parseInt(args[1]);
		int nX = Integer.parseInt(args[2]);
		int nY = Integer.parseInt(args[3]);
		int nZ = Integer.parseInt(args[4]);
		commonParam = new Parameters(new long[] {MonteCarloSteps, nX, nY, nZ});
		commonParam.baseElem = Element.valueOf(args[5]);
		if (args.length == 8) {
			commonParam.initState = BasisState.valueOf(args[6]);
		}
		return commonParam;
	}
	
	private static Variables setCommonVars(String[] args) {
		if (args.length > 5) {
			throw new ArrayIndexOutOfBoundsException("Too many arguments for common parameters.");
		} else if(args.length < 5) {
			throw new NullPointerException("Not enough arguments to initialize common parameters.");
		}
		Variables vars = new Variables();
		vars.temp = Double.parseDouble(args[1]);
		vars.B.x = Double.parseDouble(args[2]);
		vars.B.y = Double.parseDouble(args[3]);
		vars.B.z = Double.parseDouble(args[4]);
		return vars;
	}
	
	private static void writeHelp() {
		try {
			Path location = Paths.get(parentDir.toString(), "MCMC_Code_project", "help.txt");
			Scanner sc = new Scanner( location.toFile() );
			while (sc.hasNextLine()) {
				String string = (String) sc.nextLine();
				System.out.println(string);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void runSequential(Parameters param, List<Variables> varList, long startTime)
			throws InterruptedException, IOException {
		PrintStream out;
		if (outputFile != null) {
			out = new PrintStream(outputFile);
		} else {
			out = System.out;
		}
		if(sim == null) {
			System.out.println("Creates new simulator.");
			if (useConfig && configFile != null && configFile.exists()) {
				sim = new Simulator(param, varList, out, configFile, periodicBoundaries);
			} else {
				sim = new Simulator(param, varList, out, periodicBoundaries);
				configFile = curDir.toPath().resolve(name + ".conf").toFile();
			}
		} else {
			System.out.println("Uses existing simulator.");
			sim.varList = varList;
			sim.output = out;
		}
		Thread thread = new Thread(sim);

		thread.start();
		if (visualizing && visExists == false) {
			vis = new Visualizer(sim);
			String[] myArgs = { "main.Visualizer" };
			PApplet.runSketch(myArgs, vis);
			visExists  = true;
		}
		if (!out.equals(System.out)) {
			waitMessageSequential(startTime, thread, sim);
		}
		thread.join();
		writeToLogFile(sim, formatTime(System.currentTimeMillis() - startTime));
		if (outputFile != null) {
			out.close();
		}
	}

//	private static void runParallel(Parameters param, Variables[] varRange, long startTime) throws InterruptedException, IOException {
//
//		sim = new Simulator[paramRange.length];
//		threads = new Thread[paramRange.length];
//
//		for (int i = 0; i < paramRange.length; i++) {
//			File file = findFilename(curDir, name);
//			if (useConfig && configFile != null && configFile.exists()) {
//				sim[i] = new Simulator(paramRange[i], file, configFile);
//			} else {
//				sim[i] = new Simulator(paramRange[i], file);
//			}
//			if (paramRange[i].initState != null) {
//				sim[i].configFromBasisState(paramRange[i].initState); // Forces a basis configuration onto
//				// the state
//			}
//			threads[i] = new Thread(sim[i]);
//			threads[i].start();
//		}
//
//		// Visualizing:
//		if (visualizing) {
//			int vIndex = 0;
//			vis = new Visualizer(sim[vIndex]);
//			String[] myArgs = { "test.Visualizer" };
//			PApplet.runSketch(myArgs, vis);
//		}
//		
//		// Waiting message:
//		waitMessageParallel(startTime, 0);
//		for (int i = 0; i < paramRange.length; i++) {
//			threads[i].join();
//			writeToLogFile(sim[i], Long.toString(System.currentTimeMillis() - startTime));
//			if (saveConfig) {
//				sim[i].saveConfig(configFile);
//			}
//		}
////		vis.exit();
//	}

//	private static void waitMessageParallel(long startTime, int i) throws InterruptedException {
//		while (threads[i].isAlive()) {
//			double progress = sim[i].step / (double) sim[i].param.nSteps;
//			System.out.format("Simulation progress: %1.4f", progress);
//			long elapsed = System.currentTimeMillis() - startTime;
//			long ETA = (long) (elapsed * (1 / progress - 1));
//
//			System.out.println(", estimated finish time: " + formatTime(ETA));
//			Thread.sleep(2000);
//		}
//	}

	private static void waitMessageSequential(long startTime, Thread thread, Simulator sim) throws InterruptedException {
		while (thread.isAlive()) {
			double progress = (sim.step / (double) sim.param.nSteps + sim.progress) / sim.varList.size();
			System.out.format("Simulation progress: %1.4f", progress);
			long elapsed = System.currentTimeMillis() - startTime;
			long ETA = (long) (elapsed * (1 / progress - 1));

			System.out.println(", estimated finish time: " + formatTime(ETA));
			Thread.sleep(10*1000); //Shows a message every minutes
		}
	}

	private static String formatTime(long ETA) {
		return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(ETA),
				TimeUnit.MILLISECONDS.toSeconds(ETA)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ETA)));
	}

	private static void writeToLogFile(Simulator sim, String timeTaken) {
		try {
			FileWriter writer = new FileWriter(logFile, true);
			String date = (new SimpleDateFormat("MM/dd HH:mm:ss")).format(new Date());
			writer.write("Time and date: " + date);
			if (outputFile != null) {
				writer.write(" File:" + outputFile.getName());	
			}
//			writer.write(" Reject ratio (reject/steps) " + ((double) sim.nReject / (sim.varList.size()*sim.param.nSteps)));
//			writer.write(" Accept ratio (accept/steps) " + ((double) sim.nAccept / (sim.varList.size()*sim.param.nSteps))  );
			writer.write(" Time taken: " + timeTaken);
			writer.write("\n");
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
