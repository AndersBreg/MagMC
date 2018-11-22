package main;

import java.nio.file.Path;

public final class MyVector {
	public double x;
	public double y;
	public double z;

	// public MyVector() {
	// x = 0;
	// y = 0;
	// z = 0;
	// }

	public MyVector(double a, double b, double c) {
		this.x = a;
		this.y = b;
		this.z = c;
	}

	public double getCoord(int i) {
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	public void setCoord(double val, int i) {
		
	}

	public double dot(MyVector B) {
		return this.x * B.x + this.y * B.y + this.z * B.z;
	}

	public MyVector add(MyVector B) {
		return new MyVector(this.x + B.x, this.y + B.y, this.z + B.z);
	}

	public static MyVector add(MyVector A, MyVector B) {
		return new MyVector(A.x + B.x, A.y + B.y, A.z + B.z);
	}

	public MyVector sub(MyVector B) {
		return new MyVector(this.x - B.x, this.y - B.y, this.z - B.z);
	}

	public MyVector mult(double s) {
		return new MyVector(s * this.x, s * this.y, s * this.z);
	}

	public static MyVector mult(MyVector A, double s) {
		return new MyVector(s * A.x, s * A.y, s * A.z);
	}

	public MyVector normalize() {
		double S = this.x * this.x + this.y * this.y + this.z * this.z;
		S = Math.sqrt(S);
		x = x / S;
		y = y / S;
		z = z / S;
		return this;
	}

	public MyVector copy() {
		return new MyVector(this.x, this.y, this.z);
	}

	public double mag() {
		double S = this.x * this.x + this.y * this.y + this.z * this.z;
		S = Math.sqrt(S);
		return S;
	}

	public String toString() {
		return String.format("(%f, %f, %f)", this.x, this.y, this.z);
	}
	
}
