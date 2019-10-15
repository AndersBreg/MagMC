package main;

public abstract class Field<T extends Field> {

	public abstract T add(T B);
	public abstract T sub(T B);
	public abstract T mult(T B);
	public abstract T mult(double f);
	public T div(T B) {
		return this.mult((T) B.inverse());
	}
	public abstract T inverse();
	public abstract T normalize();
	public double norm() {
		return Math.sqrt(this.normSq());
	}
	public abstract double normSq();
	public abstract T copy();
	public abstract T transpose();
	
	// Static methods:
//	public abstract T exp();
//	public abstract T log();
//	public abstract T pow();
//	public abstract T identity();
	
}
