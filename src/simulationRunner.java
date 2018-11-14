
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import main.*;
import processing.core.PApplet;
import javax.swing.*;

import constants.Element;

public class simulationRunner {

	private static double tStart = 1;
	private static double hStart = 1;

	private static double dT = 2;
	private static double dH = 2;

	private static double tEnd = 20;
	private static double hEnd = hStart + 1;

	private static int tArrSize = (int) Math.ceil((tEnd - tStart) / dT);// Numbers of temperatures to simulate
	private static int hArrSize = (int) Math.ceil((hEnd - hStart) / dH);// Numbers of fields to simulate

	private static double[][] T_H_Arr;

	private static String location = "C:\\Users\\anders\\Documents\\11_Semester\\Speciale\\Data\\after_git_impl\\";
	private static String name = "Ni_sim_2,2,2";
	private static String configFilename = "config";
	private static boolean saveConfig = true;
	private static boolean loadConfig = true;
	private static int N = tArrSize * hArrSize; // Total numbers of simulations;

	static Visualizer vis;
	static Simulator[] sim;
	static Thread[] threads;
	private static boolean parallel = true;

	public static void main(String[] args) throws InterruptedException, IOException {

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
			String filename = String.format(name + "_T=%1.2f_and_H=%1.2f", T_H_Arr[i][0], T_H_Arr[i][1]);
			paramRange[i] = new Parameters(new int[] { 1000000, 1, 1, 1 },
					new double[] { T_H_Arr[i][0], 0, 0, T_H_Arr[i][1] },
					new boolean[] { true },
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
					String configFile = location + String.format(name +"_config_T=%1.2f_H=%1.2f.txt", T_H_Arr[i][0], T_H_Arr[i][1]);
					sim[i] = new Simulator(paramRange[i], configFile);
				} else {
					sim[i] = new Simulator(paramRange[i]);	
				}
				threads[i] = new Thread(sim[i]);
				threads[i].start();
			}
			// Visualizing:
			int vIndex = 0;
			vis = new Visualizer(sim[vIndex]);
			String[] myArgs = { "test.Visualizer" };
			PApplet.runSketch(myArgs, vis);
			
			// Waiting message:
			waitMessage(startTime, 0);
			for (int i = 0; i < N; i++) {
				threads[i].join();
				if (saveConfig) {
					String configFile = location
							+ String.format(name + "_config_T=%1.2f_H=%1.2f.txt", T_H_Arr[i][0], T_H_Arr[i][1]);
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
				sim[i].saveConfig(configFile);
				if(i+1 != sim.length) {
					sim[i+1].loadConfig(configFile);
				}
			}
		}

		System.out.println("Total time taken: " + (System.currentTimeMillis() - startTime) + " ms");
		System.exit(0);
	}

	private static int getIndex(int nh, int nt) {
		return nh + hArrSize * nt;
	}

	private static void waitMessage(long startTime, int i) throws InterruptedException {
		while (threads[i].isAlive()) {
			double progress = sim[i].progress / (double) sim[i].param.nSteps;
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

}
