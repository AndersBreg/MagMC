package main;

import java.util.List;

public class Config {

	List<MyVector> spins;
	List<Element> elements;
	
	public Config(List<MyVector> newSpins, List<Element> newElems) {
		this.spins = newSpins;
		this.elements = newElems;
	}
	public Config() {
		genNewConfig();
	}
	private void genNewConfig() {
		// TODO Auto-generated method stub
		
	}
}
