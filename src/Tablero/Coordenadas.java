package Tablero;

import Sistema.Transformer;

public class Coordenadas {
	private char abcisas;
	private int ordenadas;
	
	public Coordenadas (char x, int y) {
		this.abcisas = x;
		this.ordenadas = y;
	}
	
	public Coordenadas (String x, int y) {
		this.abcisas = x.charAt(0);
		this.ordenadas = y;
	}

	public char getAbcisas() {
		return abcisas;
	}

	public int getAbcisasAsInt() {
		return Transformer.letterToNum(abcisas);
	}

	public int getOrdenadas() {
		return ordenadas;
	}
	
	public String toString() {
		return (String.valueOf(abcisas) + String.valueOf(ordenadas));
	}
}
