
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import main.*;
import processing.core.PApplet;
import javax.swing.*;

import constants.Element;

public class simulationRunner {

	private static double tStart = 1f;
	private static double hStart = 1f;

	private static double dT = 4f;
	private static double dH = 2f;

	private static double tEnd = tStart + 20f;
	private static double hEnd = hStart + 1f;

	private static int tArrSize = (int) Math.ceil((tEnd - tStart) / dT);// Numbers of temperatures to simulate
	private static int hArrSize = (int) Math.ceil((hEnd - hStart) / dH);// Numbers of fields to simulate

	private static double[][] T_H_Arr;

	private static String location = "C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\working\\";
	private static String filename;
	private static String fileFormat = ".txt";
	private static String configFilename = "config";
	private static boolean saveConfig = false;
	private static boolean loadConfig = true;
	private static int N = tArrSize * hArrSize; // Total numbers of simulations;

	static Visualizer vis;
	static Simulator[] sim;
	static Thread[] threads;

	public static void main(String[] args) throws InterruptedException {

		System.out.println(N);
		T_H_Arr = new double[N][2];
		for (int nt = 0; nt < tArrSize; nt++) {
			for (int nh = 0; nh < hArrSize; nh++) {
				T_H_Arr[getIndex(nh, nt)][0] = tStart + dT * nt;
				T_H_Arr[getIndex(nh, nt)][1] = hStart + dH * nh;
			}
		}

		Parameters[] paramRange = new Parameters[N]; // Array of all parameters to use

		// Coupling values: Anisotropy:
		// [Ja, Jb, Jc, Jab, Jac, Jbc] [ Da, Db, Dc ]
		// [ 0, 0.67, -.05, 0.3, -.11, 1.04] Pure Ni [ 0.339, 1.82, 0 ]
		// [ -, -, -, -, -, -] Pure Co []

		for (int i = 0; i < N; i++) {
			paramRange[i] = new Parameters(new int[] { 2 << 17, 1, 1, 1 },
					new double[] { T_H_Arr[i][0], 0, 0, T_H_Arr[i][1] }, 
					new boolean[] { true },
					new String[] {
						location, 
						String.format("Ni_sim_new_1_T=%1.2f_and_H=%1.2f", T_H_Arr[i][0], T_H_Arr[i][1]),
						".txt"
						}
					);
			if (loadConfig) {
				try {
					Config list = loadConfig(
							location + String.format("Sim_Ni_Config_T=%1.2f_H=%1.2f.txt", T_H_Arr[i][0], T_H_Arr[i][1]));
					paramRange[i].setInitConfig(list);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		sim = new Simulator[N];
		threads = new Thread[N];

		for (int i = 0; i < N; i++) {
			sim[i] = new Simulator(paramRange[i]);
		}

		// Visualizing:
		// int vIndex = (new Random(1)).nextInt(N); // Choose a random simulation to
		// follow
		int vIndex = 0;
		vis = new Visualizer(sim[vIndex]);
		String[] myArgs = { "test.Visualizer" };
		PApplet.runSketch(myArgs, vis);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < N; i++) {
			threads[i] = new Thread(sim[i]);
			threads[i].start();
		}
		waitMessage(startTime);
		for (int i = 0; i < N; i++) {
			threads[i].join();
			if (saveConfig) {
				saveConfig(sim[i],
						location + String.format("Sim_Ni_Config_T=%1.2f_H=%1.2f.txt", T_H_Arr[i][0], T_H_Arr[i][1]));
			}
		}

		System.out.println("Total time taken: " + (System.currentTimeMillis() - startTime) + " ms");
		System.exit(0);
	}

	private static int getIndex(int nh, int nt) {
		return nh + hArrSize * nt;
	}

	private static void waitMessage(long startTime) throws InterruptedException {
		while (threads[0].isAlive()) {
			double progress = sim[0].progress / (double) sim[0].param.nSteps;
			System.out.print("Simulation progress: " + progress);
			long elapsed = System.currentTimeMillis() - startTime;
			long ETA = (long) (elapsed * (1 / progress - 1));

			String ETAString = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(ETA),
					TimeUnit.MILLISECONDS.toSeconds(ETA)
							- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ETA)));
			System.out.println(", estimated finish time: " + ETAString);
			Thread.sleep(1000);
		}
	}

	public static Config loadConfig(String filename) throws IOException {
		List<MyVector> newSpins = new ArrayList<MyVector>();
		List<Element> newElems = new ArrayList<Element>();
		BufferedReader in = new BufferedReader(new FileReader(filename));
		int i = 0;
		while (true) {
			String line = in.readLine();
			if (line == null) {
				break;
			} else {
				StringTokenizer st = new StringTokenizer(line);
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				double z = Double.parseDouble(st.nextToken());
				MyVector vec = new MyVector(x, y, z);
				newSpins.add(vec);

				Element elem = Element.valueOf(st.nextToken());
				newElems.add(elem);
			}
			i += 1;
		}
		in.close();
		System.out.println("Loaded in " + i + " spin orientations.");
		Config newConfig = new Config(newSpins, newElems);
		return newConfig;
	}

	private static void saveConfig(Simulator sim, String name) {
		File file = new File(name);
		Path path = file.toPath();

		file = path.toAbsolutePath().toFile();
		System.out.println("Prints end configuration to path: " + file.getAbsolutePath());

		try {
			PrintStream out = new PrintStream(file);
			Iterator<int[]> it = sim.iterateAtoms();
			while(it.hasNext()) {
				int[] index = it.next();
				MyVector spin = sim.getSpinDir(index); 
				out.print(spin.x + " ");
				out.print(spin.y + " ");
				out.print(spin.z + " ");
				out.println(sim.getAtom(index));
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
		}
	}
}
