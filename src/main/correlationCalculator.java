package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class correlationCalculator {

	public static void main(String[] args) {
		
	}
	
	public List<Double> calc(ArrayList<Double> L){
		ArrayList<Double> A = (ArrayList<Double>) L.clone();
		
		int length = L.size();
		int N = (int) Math.floor(Math.log(length) / Math.log(2));
		
		for (int i = 0; i < N; i++) {
			ArrayList<Double> B = new ArrayList<Double>(); 
			for (int j = 0; j < A.size(); j++) {
				double d1 = A.get(2*j);
				double d2 = A.get(2*j+1);
				B.add((d1+d2)/2);
				A.addAll(B);
			}
		}
		return A;
	}

}
