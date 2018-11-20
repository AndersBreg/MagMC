
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import main.*;
import processing.core.PApplet;

import constants.Crystal;
import constants.Element;

public class simulationRunner {
	
	private static double xStart = 2;
	private static double yStart = 0;

	private static double dX = 49;
	private static double dY = 1;

	private static double xEnd = 3; // In kelvin
	private static double yEnd = 100; // In tesla

	private static int xArrSize = (int) Math.ceil((xEnd - xStart) / dX);// Numbers of temperatures to simulate
	private static int yArrSize = (int) Math.ceil((yEnd - yStart) / dY);// Numbers of fields to simulate

	private static double[][] X_Y_Arr;

	private static String location = "C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\Ni_c-axis_mag_2\\";
	private static String name = "Sim";
	private static String configFilename = "config";
	private static File logFile = new File("C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\logFile.txt");
	
	private static boolean saveConfig = false;
	private static boolean loadConfig = false;
	private static int N = xArrSize * yArrSize; // Total numbers of simulations;

	private static Visualizer vis;
	private static Simulator[] sim;
	private static Thread[] threads;
	private static boolean parallel = true;

	public static void main(String[] args) throws InterruptedException, IOException {
//		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println(N);
		X_Y_Arr = new double[N][2];
		for (int nt = 0; nt < xArrSize; nt++) {
			for (int nh = 0; nh < yArrSize; nh++) {
				X_Y_Arr[getIndex(nh, nt)][0] = xStart + dX * nt;
				X_Y_Arr[getIndex(nh, nt)][1] = yStart + dY * nh;
			}
		}

		Parameters[] paramRange = new Parameters[N]; // Array of all parameters to use

		for (int i = 0; i < N; i++) {
			String filename = name + String.format("_T=%1.2f_Hb=%1.2f", X_Y_Arr[i][0], X_Y_Arr[i][1]); 
					/*String.format("_(%d,%d,%d)_T=%1.2f_H=%1.2f.txt", 
					paramRange[i].nX/2, 
					paramRange[i].nY/2, 
					paramRange[i].nZ/2, 
					paramRange[i].temp,
					paramRange[i].H.z);*/ // TODO required re-writing the simulator constructor.
			paramRange[i] = new Parameters(
					new int[] { 1 << 18, 1 << 8, 4, 4, 4 },
					new double[] { X_Y_Arr[i][0], 0, 0, X_Y_Arr[i][1]},
					new String[] {
						location, 
						filename,
						".txt"
						}
					);
		}

		sim = new Simulator[N];
		threads = new Thread[N];
		long startTime = System.currentTimeMillis();
		if (parallel) {
			for (int i = 0; i < N; i++) {
				if(loadConfig) {
					String configFile = location + name + 
							String.format("_config_(%d,%d,%d)_T=%1.2f_Hb=%1.2f.txt", 
									paramRange[i].nX/2, 
									paramRange[i].nY/2, 
									paramRange[i].nZ/2, 
									paramRange[i].temp,
									paramRange[i].H.z);
					sim[i] = new Simulator(paramRange[i], configFile);
				} else {
					sim[i] = new Simulator(paramRange[i]);
				}
				sim[i].setElementFraction(Element.Ni, 1.0);
//				sim[i].configFromBasisState(Crystal.Cz); // Forces a basis configuration onto the state
				threads[i] = new Thread(sim[i]);
				threads[i].start();
			}
			// Visualizing:
			int vIndex = 2;
			vis = new Visualizer(sim[vIndex]);
			String[] myArgs = { "test.Visualizer" };
			PApplet.runSketch(myArgs, vis);
			
			// Waiting message:
			waitMessage(startTime, 0);
			for (int i = 0; i < N; i++) {
				threads[i].join();
				writeToLogFile(sim[i], Long.toString(System.currentTimeMillis() - startTime));
				if (saveConfig) {
					String configFile = location + name
							+ String.format("_config_(%d,%d,%d)_T=%1.2f_Hb=%1.2f.txt", 
									paramRange[i].nX/2, 
									paramRange[i].nY/2, 
									paramRange[i].nZ/2, 
									paramRange[i].temp,
									paramRange[i].H.z);
					sim[i].saveConfig(configFile);
				}
			}
		} else {
			String configFile = location + "configFile.txt";
			for (int i = 0; i < N; i++) {
				threads[i] = new Thread(sim[i]);
				threads[i].start();
				waitMessage(startTime, i);
				threads[i].join();
				writeToLogFile(sim[i], formatTime(System.currentTimeMillis() - startTime));
				sim[i].saveConfig(configFile);
				if(i+1 != sim.length) {
					sim[i+1].loadConfig(configFile);
				}
			}
		}

		System.out.println("Total time taken: " + formatTime(System.currentTimeMillis() - startTime));
		System.exit(0);
	}

	private static int getIndex(int nh, int nt) {
		return nh + yArrSize * nt;
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
					+ ((double) sim.nRejects / (double) sim.param.nSteps) + " and time taken " + timeTaken +"\n");
			writer.close();
		} catch (IOException e) {
			System.err.println("Caught Exception: " + e.getMessage());
		}
	}

}
