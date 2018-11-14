import java.io.BufferedReader;
import java.io.Reader;
import java.util.Random;

import peasy.*;
import processing.core.PApplet;
import processing.core.PVector;

public class MonteCarloQuadratic extends PApplet {

//	private static Scanner reader;
//	private static String location = MagnetMonteCarloSimulering.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//	private static JFileChooser fileChooser = new JFileChooser(location);

	private PVector position;
	private Random rand;
	private PeasyCam cam;
	private int counter = 0;
	private float temp = 0.2f;
	private float energy;

	// Other screen
	// private PGraphics graphics;

	public static void main(String[] args) {

//		try {
//			fileChooser.setDialogTitle("Choose configuration file");
//			int returnVal = fileChooser.showOpenDialog(null);
//
//			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				File f = fileChooser.getSelectedFile();
//				System.out.println("You chose to open this file: " + f.getName());
//				reader = new Scanner(f);
//			}
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println("You will now be prompted for variable values.");
//		System.out.println("nX / number of atoms in the x-direction:");
//		input = reader.next();
//		nX = !input.equals("") ? Integer.parseInt(input) : nX;
//		System.out.println(Integer.toString(nX));
//		
//		System.out.print("nY:");
//		input = reader.next();
//		nY = !input.equals("") ? Integer.parseInt(input) : nY;
//		System.out.println(Integer.toString(nY));
//
//		System.out.print("nZ?:");
//		input = reader.next();
//		nZ = !input.equals("") ? Integer.parseInt(input) : nZ;
//		System.out.println(Integer.toString(nZ));
//		
//		System.out.print("Rate of \"Temperature decrease\":");
//		input = reader.next();
//		tempDecre = !input.equals("") ? Float.parseFloat(input) : tempDecre;
//		System.out.println(Double.toString(tempDecre));
//		
//		System.out.print("Anisotropy-x?:");
//		input = reader.next();
//		Dx = !input.equals("") ? Float.parseFloat(input) : Dx;
//		System.out.println(Float.toString(Dx));
//		
//		System.out.print("Anisotropy-y?:");
//		input = reader.next();
//		Dy = !input.equals("") ? Float.parseFloat(input) : Dy;
//		System.out.println(Float.toString(Dy));
//		
//		System.out.print("J-px / nearest neightbour in x-direction?:");
//		input = reader.next();
//		Jpx = !input.equals("") ? Float.parseFloat(input) : Jpx;
//		System.out.println(Float.toString(Jpx));
//		
//		System.out.print("J-py?:");
//		input = reader.next();
//		Jpy = !input.equals("") ? Float.parseFloat(input) : Jpy;
//		System.out.println(Float.toString(Jpy));
//		
//		System.out.print("J-pz?:");
//		input = reader.next();
//		Jpz = !input.equals("") ? Float.parseFloat(input) : Jpz;
//		System.out.println(Float.toString(Jpz));
//		
//		System.out.print("J-dxy? / next-nearest neighbour in the xy plane:");
//		input = reader.next();
//		Jdxy = !input.equals("") ? Float.parseFloat(input) : Jdxy;
//
//		System.out.print("J-dyz?:");
//		input = reader.next();
//		Jdyz = !input.equals("") ? Float.parseFloat(input) : Jdyz;
//		
//		System.out.println("J-dyz?:");
//		input = reader.next();
//		Jdzx = !input.equals("") ? Float.parseFloat(input) : Jdzx;
//
//		System.out.println("J-dz2? / next-nearest neightbour in the z-direction:");
//		input = reader.next();
//		Jdz2 = !input.equals("") ? Float.parseFloat(input) : Jdz2;

		PApplet.main("MonteCarloQuadratic");
	}

	public void settings() {
		size(800, 600);
	}

	public void setup() {
		position = new PVector();

		rand = new Random(0);
		randomSeed(0);
//		cam = new PeasyCam(this, 100);
//		float cameraZ = (float) ((this.height / 2.0f) / tan(PI / 6f));
//		float FoV = PI / 3.0f;
//		float aspectRatio = (float) width / (float) height;
//		perspective(FoV, aspectRatio, 1f, cameraZ * 100.0f);

		// orientations[0] = new PVector(1,0,0);
		// orientations[1] = new PVector(-1,0,0);
		// orientations[2] = new PVector(-1,0,0);
		// orientations[3] = new PVector(1,0,0);
		//
		// orientations[4] = new PVector(-1,0,0);
		// orientations[5] = new PVector(1,0,0);
		// orientations[6] = new PVector(1,0,0);
		// orientations[7] = new PVector(-1,0,0);
		sphereDetail(10);
		// graphics = createGraphics(100, 100, P3D);
//		plotDistribution();
	}

	public void draw() {
		for (int i = 0; i < 100000; i++) {
			iterate();
			System.out.println(energy+" "+position.x+" "+position.y);
			counter += 1;
			if(counter % 1000==0) {
				realDraw();
			}
			if(counter == 200000) {
				this.exit();
			}
		}
	}

	private void realDraw() {
		background(255, 255, 255);
		// translate(width / 2, height / 2, 400);
		translate(width/2, height/2);
		noFill();
		ellipse(-10,-10, 20, 20);
		
		translate(10*position.x, 10*position.y);
		fill(color(0,0,0));
		rect(-5, -5, 10, 10);
		
	}
	
//	private void drawSpins() {
//		for (int i = 0; i < nX; i++) {
//			for (int j = 0; j < nY; j++) {
//				for (int k = 0; k < nZ; k++) {
//					pushStyle();
//					pushMatrix();
//					// Moves to position
//					PVector position = positions[getIndex(i, j, k)];
//					translate(position.x, position.y, position.z);
//	
//					// Draws box
//					noFill();
//					box(spacing);
//	
//					// Gets spin orientation and draws arrow
//					PVector orient = orientations[getIndex(i, j, k)];
//					arrow(orient);
//	
//					popMatrix();
//					popStyle();
//				}
//			}
//		}
//	}

//	private void drawHud() {
//		cam.beginHUD();
//		fill(color(0, 0, 0));
//		text("J-xy-plane = " + Jpx, 50, 50);
//		text("J-NN = " + Jpz, 50, 70);
//		text("J-anisotropy = " + Dx, 50, 90);
//		text("Temp = " + temp, 50, 110);
//		text("Energy = " + energy, 50, 130);
//		cam.endHUD();
//	}

//	private void drawCoordinateSystem() {
//		pushMatrix();
//		pushStyle();
//		translate(20, 0, 0);
//	
//		stroke(color(255, 0, 0));
//		line(0, 0, 0, spacing, 0, 0);
//	
//		stroke(color(0, 255, 0));
//		line(0, 0, 0, 0, spacing, 0);
//	
//		stroke(color(0, 0, 255));
//		line(0, 0, 0, 0, 0, spacing);		
//		
//		// arrow(new PVector(1,0,0));
//		// arrow(new PVector(0,1,0));
//		// arrow(new PVector(0,0,1));
//		popStyle();
//		popMatrix();
//	}

	private void arrow(PVector orient) {
		int dirColor = color((orient.x + 1f) * 128f, (orient.y + 1f) * 128f, (orient.z + 1f) * 128f);
		stroke(dirColor);
		strokeWeight(5);
		line(-orient.x, -orient.y, -orient.z, orient.x, orient.y, orient.z);
		translate(orient.x, orient.y, orient.z);

		float theta = asin(orient.y / orient.mag());
		float phi = -atan2(orient.z, orient.x);
		rotateY(phi); // Phi
		rotateZ(theta); // theta
		fill(dirColor);
		scale(0.5f);
		pyramid(8, 0.3f);
	}

	private void iterate() {
//		int index = getIndex(rand.nextInt(nX), rand.nextInt(nY), rand.nextInt(nZ));
//		PVector old = orientations[index].copy();
//		orientations[index] = PVector.random3D(this);
		
		// Create a new sample :
		PVector proposal = sample();
		// Calculate it's energy:
		float newEnergy = calculateEnergy(proposal);
		
		float delE = newEnergy - energy;
		if(rand.nextFloat() <= min(1, exp(-delE / temp)) ) {
			position = proposal.copy();
			energy = newEnergy;
		}
	}

	private float calculateEnergy(PVector system) {
		float S = 0;
		S += system.x*system.x + system.y*system.y;
		return S;
	}

	private PVector sample() {
		PVector addition = randomVector().mult(0.3f);
		PVector proposal = PVector.add(position.copy(), addition);
		return proposal;
	}	

	private PVector randomVector() {
		float X = (float) rand.nextGaussian();
		float Y = (float) rand.nextGaussian();
		return new PVector(X, Y);
	}
	
	// Utilities for drawing
	private void pyramid(int n, float r) {
		beginShape(TRIANGLE_FAN);
		vertex(1, 0, 0);
		for (int i = 0; i <= n; i++) {
			vertex(0, r * cos(i * TWO_PI / n), r * sin(i * TWO_PI / n));
		}
		endShape(CLOSE);
		beginShape();
		for (int i = 0; i < n; i++) {
			vertex(0, r * cos(i * TWO_PI / n), r * sin(i * TWO_PI / n));
		}
		endShape();
	}

	private void plotDistribution() {
		// PGraphics out = createGraphics(100, 100, P3D);
		float[] arr = new float[10000];
		float max = 0;
		for (int i = 0; i < arr.length; i++) {
			arr[i] = -log(rand.nextFloat());
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		int nBins = 100;
		int[] hist = new int[nBins];
		for (int i = 0; i < arr.length; i++) {
			hist[(int) floor(100 * arr[i] / (max + 0.001f))] += 1;
		}
//		int histMax = max(hist);
		// graphics.fill(1f);
		// graphics.strokeWeight(10);
//		for (int i = 0; i < hist.length; i++) {
//			System.out.println("For bin " + i + " there are " + hist[i]);
			// graphics.rect(i, hist[i]*100/histMax, 1, 1);
			// out.point(i, hist[i]*100/histMax);
//		}
		// return out;
	}

}
