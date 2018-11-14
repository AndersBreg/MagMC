import java.io.BufferedReader;
import java.io.Reader;
import java.util.Random;

import peasy.*;
import processing.core.PApplet;
import processing.core.PVector;

public class MonteCarloQuadratic extends PApplet {

	private PVector position;
	private Random rand;
	private int counter = 0;
	private float temp = 0.2f;
	private float energy;

	public static void main(String[] args) {
		PApplet.main("MonteCarloQuadratic");
	}

	public void settings() {
		size(800, 600);
	}

	public void setup() {
		position = new PVector();

		rand = new Random(0);
		randomSeed(0);
		sphereDetail(10);
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
		translate(width/2, height/2);
		noFill();
		ellipse(-10,-10, 20, 20);
		
		translate(10*position.x, 10*position.y);
		fill(color(0,0,0));
		rect(-5, -5, 10, 10);
	}

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
