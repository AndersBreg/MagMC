package main;

public class Complex extends Field<Complex> {

	public final double re, im;

	public Complex() {
		re = 0;
		im = 0;
	}
	public Complex(double a) {
		re = a;
		im = 0;
	}

	public Complex(double a, double b) {
		re = a;
		im = b;
	}

	@Override
	public Complex add(Complex B) {
		return new Complex(this.re + B.re, this.im + B.im);
	}

	@Override
	public Complex sub(Complex B) {
		return new Complex(this.re - B.re, this.im - B.im);
	}

	@Override
	public Complex mult(Complex B) {
		return new Complex(this.re * B.re - this.im * B.im, this.re * B.im + this.im * B.re);
	}

	@Override
	public Complex mult(double f) {
		return new Complex(f * this.re, f * this.im);
	}

	@Override
	public Complex div(Complex B) {
		return this.mult(B.inverse());
	}

	@Override
	public double normSq() {
		return this.re * this.re + this.im * this.im;
	}

	@Override
	public double norm() {
		return Math.sqrt(this.normSq());
	}

	public double abs(Complex B) {
		return B.norm();
	}

	@Override
	public Complex transpose() {
		return new Complex(this.re, -this.im);
	}

	// (a+b*i)*(c+d*i) = 1 => c+d*i=1/(a+b*i)=(a-b*i)/(a+b*i)*(a-b*i) =
	// (a-b*i)/(a^2+b^2)
	@Override
	public Complex inverse() {
		return this.transpose().mult(1 / this.normSq());
	}

	@Override
	public Complex normalize() {
		return this.mult(1 / this.norm());
	}

	@Override
	public Complex copy() {
		return new Complex(this.re, this.im);
	}

	// exp(a+i*b) = exp(a)*(cos(b)+i*sin(b))
	public static Complex exp(Complex B) {
		return new Complex(Math.cos(B.im), Math.sin(B.im)).mult(Math.exp(B.re));
	}

	// log(a+i*b) = log(r*exp(I*phi)) = log(r) + I*phi
	// z = a+i*b => z*z' = |z|^2 = a^2 + b^2 = r^2 => r = sqrt(a^2+b^2)
	// (z - z')/(z + z') = (2 i b) /(2 a) = i b/(a) = i r*sin(phi) / r*cos(phi) = i
	// tan(phi) =>
	// phi = arctan(b/a)
	public static Complex log(Complex B) {
		return new Complex(Math.log(B.norm()), Math.atan2(B.im, B.re));
	}

	public Complex pow(double f) {
		return Complex.exp(Complex.log(this).mult(f));
	}

	@Override
	public String toString() {
		return this.re + " + I*" + this.im;
	}

	public static final Complex identity() {
		return new Complex(1, 0);
	}
}
