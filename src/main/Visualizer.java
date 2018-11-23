package main;

import processing.core.*;

import java.util.Iterator;
import java.util.List;

import main.*;
import peasy.*;

public class Visualizer extends PApplet {

	Simulator master;
	PeasyCam cam;

	public Visualizer(Simulator sim) {
		this.master = sim;
	}

	public void settings() {
		size(800, 600, P3D);
	}

	public void setup() {
		
//		removeExitEvent(getSurface());
		cam = new PeasyCam(this, 100);
		float cameraZ = (float) ((this.height / 2.0f) / tan(PI / 6f));
		float FoV = PI / 3.0f;
		float aspectRatio = (float) width / (float) height;
		perspective(FoV, aspectRatio, 1f, cameraZ * 100.0f);
		sphereDetail(10);
	}

//	static final void removeExitEvent(final PSurface surf) {
//		final java.awt.Window win = ((processing.awt.PSurfaceAWT.SmoothCanvas) surf.getNative()).getFrame();
//
//		for (final java.awt.event.WindowListener evt : win.getWindowListeners())
//			win.removeWindowListener(evt);
//	}

	public void draw() {
		background(255, 255, 255);

		// Draws coordinate system
		drawCoordinateSystem();

		// Draws the spins of atoms
		drawSpins();

		// Draw unit cells
		drawUnitCells();

		drawAtomTypes();

		drawConnections();
		
		// Draws the overlay
		drawHud(Parameters.getNames(), master.param);
		
	}

	private void drawCoordinateSystem() {
		pushMatrix();
		pushStyle();
		translate(20, 0, 0);
	
		stroke(color(255, 0, 0));
		line(0, 0, 0, Simulator.scaling, 0, 0);
	
		stroke(color(0, 255, 0));
		line(0, 0, 0, 0, Simulator.scaling, 0);
	
		stroke(color(0, 0, 255));
		line(0, 0, 0, 0, 0, Simulator.scaling);
	
		// arrow(new PVector(1,0,0));
		// arrow(new PVector(0,1,0));
		// arrow(new PVector(0,0,1));
		popStyle();
		popMatrix();
	}

	private void drawSpins() {
		Iterator<int[]> it = master.iterateAtoms();
		while (it.hasNext()) {
			int[] ls = (int[]) it.next();
			pushStyle();
			pushMatrix();
	
			// Moves to position
			MyVector pos = master.positions[ls[0]][ls[1]][ls[2]];
			translate((float) pos.x, (float) pos.y, (float) pos.z);
	
			MyVector orient = master.spins[ls[0]][ls[1]][ls[2]];
			arrow(orient);
			//
			// int[][] basisNeighBours = master.crys.getBasisNB();
			// for (int basisIndex = 0; basisIndex < master.nBasis; basisIndex++) {
			// pushStyle();
			// pushMatrix();
			//
			// // Moves to the basis position:
			// PVector basisShift = master.basis[basisIndex].copy();
			// basisShift = master.scaleVec(master.a, master.b, master.c, basisShift);
			// basisShift.mult(master.scaling);
			// translate(basisShift.x, basisShift.y, basisShift.z);
			//
			// // Gets spin orientation and draws arrow
			// int[] myBasis = basisNeighBours[basisIndex];
			// PVector orient =
			// master.orientations[ls[0]+myBasis[0]][ls[1]+myBasis[1]][ls[2]+myBasis[2]];
			// arrow(orient);
			//
			// popMatrix();
			// popStyle();
			// }
			popMatrix();
			popStyle();
		}
	}

	private void drawUnitCells() {
		Iterator<int[]> it = master.iterateUnitCells();
		while (it.hasNext()) {
			pushMatrix();
			pushStyle();
	
			int[] is = (int[]) it.next();
			MyVector pos = master.positions[is[0]][is[1]][is[2]];
			translate((float)pos.x, (float)pos.y, (float)pos.z);
	
			// Draws box
			cuboid(master.a * master.scaling, master.b * master.scaling, master.c * master.scaling);
	
			popMatrix();
			popStyle();
		}
	
	}

	private void drawAtomTypes() {
		Iterator<int[]> it = master.iterateAtoms();
		while (it.hasNext()) {
			int[] ls = (int[]) it.next();
			pushStyle();
			pushMatrix();

			// Moves to position
			MyVector pos = master.positions[ls[0]][ls[1]][ls[2]];
			translate((float) pos.x, (float) pos.y, (float) pos.z);
			noStroke();
			switch (master.getAtom(ls)) {
			case Ni:
				fill(color(0xa5, 0x2a, 0x2a));
				break;
			case Co:
				fill(color(0xff, 0x14, 0x93));
				break;
			case Fe:
				fill(color(0xdd, 0x77, 0x00));
				break;
			default:
				fill(color(0, 0, 0));
				break;
			}
			sphere(0.5f);
			popStyle();
			popMatrix();

		}
	}

	private void drawConnections() {
		pushStyle();
//		drawCouplings(new int[] {4,5,5});
		popStyle();
	}

	private void drawCouplings(int[] index) {
		if (master.crys.isValid(index)) {
			int[] indexA = master.normIndex(index);
			MyVector A = master.positions[indexA[0]][indexA[1]][indexA[2]];
			int[][][] NNlist = master.crys.getNNIndices();
			int indexOfNeighboursToShow = 5;
			int[][] neighbours = NNlist[indexOfNeighboursToShow];
			for (int n = 0; n < neighbours.length; n++) {
				int[] nb = neighbours[n];
				int[] indexB = master.normIndex(new int[] {indexA[0]+nb[0], indexA[1]+nb[1],indexA[2]+nb[2]});
				MyVector B = master.positions[indexB[0]][indexB[1]][indexB[2]];
				stroke(colorMapI(n));
				strokeWeight(10);
				line((float) B.x, (float) B.y, (float) B.z, (float) A.x, (float) A.y, (float) A.z);
			}
		}
	}

	private void drawHud(String[] paramNames, Parameters par) {
		cam.beginHUD();
		fill(color(0, 0, 0));
		float cursorPosX = 50;
		float cursorPosY = 50;
		double[] param = par.asList();
		for (int i = 0; i < param.length; i++) {
			text(paramNames[i] + " = " + param[i], cursorPosX, cursorPosY);
			pushStyle();
//			if (5 <= i && i <= 7) {
//				fill(colorMapF((i - 5f) / 9f));
//				ellipse(cursorPosX - 5, cursorPosY - 5, 10, 10);
//			} else if (8 <= i && i <= 10) {
//				fill(colorMapF(map(i, 5f, 10f, 1f / 3f, 2f / 3f)));
//				ellipse(cursorPosX - 5, cursorPosY - 5, 10, 10);
//			}
			popStyle();
			cursorPosY += 20;
		}
		text("Energy = " + master.energySingle/master.nAtoms, 50, cursorPosY);
		cursorPosY += 20;
		text("Current step = " + master.step, 50, cursorPosY);
		cam.endHUD();
	}

	private void cuboid(float dX, float dY, float dZ) {
		pushMatrix();
		pushStyle();
		noFill();
		stroke(color(0, 0, 0));
		translate(dX / 2, dY / 2, dZ / 2);
		box(dX, dY, dZ);
		popStyle();
		popMatrix();
	}

	private int colorMapI(int i) {
		switch (i) {
		case 0:
			return color(255, 0, 0);
		case 1:
			return color(0, 255, 0);
		case 2:
			return color(0, 0, 255);
		case 3:
		case 4:
			return color(0, 255, 255);
		case 5:
		case 6:
			return color(255, 0, 255);
		case 7:
		case 8:
			return color(255, 255, 0);
		default:
			return color(0, 0, 0);
		}
	}

	private int colorMapF(float a) {
		float val = a * 6;
		if (val < 1) {
			return lerpColor(color(0, 0, 0), color(255, 0, 0), val);
		} else if (val < 2) {
			return lerpColor(color(255, 0, 0), color(255, 255, 0), val);
		} else if (val < 3) {
			return lerpColor(color(255, 255, 0), color(0, 255, 0), val);
		} else if (val < 4) {
			return lerpColor(color(0, 255, 0), color(0, 255, 255), val);
		} else if (val < 5) {
			return lerpColor(color(0, 255, 255), color(0, 0, 255), val);
		} else if (val < 6) {
			return lerpColor(color(0, 0, 255), color(255, 0, 255), val);
		} else {
			return lerpColor(color(255, 0, 255), color(255, 255, 255), val);
		}
	}

	private void arrow(MyVector orient) {
		pushMatrix();
		pushStyle();
		int dirColor = color((float) (orient.x + 1f) * 128f, (float) (orient.y + 1f) * 128f, (float) (orient.z + 1f) * 128f);
		stroke(dirColor);
		strokeWeight(5);
		line((float) -orient.x, (float) -orient.y, (float) -orient.z, (float) orient.x, (float) orient.y, (float) orient.z);
		translate((float) orient.x, (float) orient.y, (float) orient.z);

		double theta = Math.asin(orient.y / orient.mag());
		double phi = -Math.atan2(orient.z, orient.x);
		rotateY((float) phi); // Phi
		rotateZ((float) theta); // theta
		fill(dirColor);
		scale(0.5f);
		pyramid(8, 0.3f);
		popMatrix();
		popStyle();
	}

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

//	@Override
//	public void exit() {
//	}
}