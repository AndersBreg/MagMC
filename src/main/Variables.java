package main;

public class Variables {

	/** Applied H-field */
	public MyVector B = new MyVector(0, 0, 0);
	public double temp = 0;

	public Variables() {

	}

	public Variables(Variables V) {
		this.temp = V.temp;
		this.B = V.B.copy();
	}
	public Variables(double temp, double Bx, double By, double Bz) {
		this.temp = temp;

		B = new MyVector(Bx, By, Bz);
	}

	public String toString() {
		return "Temp: " + temp + ", B = (" + B.x + ", " + B.y + ", " + B.z + ")";
	}

}
