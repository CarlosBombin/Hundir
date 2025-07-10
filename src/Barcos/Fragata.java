package Barcos;

import Estados.Tocado;
import Tablero.Casilla;

public class Fragata extends Barco{
	private int tamaño = 1;
	
	public Fragata (Casilla cas1) {
		super();
		this.posiciones.add(cas1);
		cas1.setBarco(this);
	}
	
	@Override
	public boolean estaHundido() {
		int total = 0;
		for (Casilla cas : posiciones) {
			if (cas.getEstado().equals(Tocado.getInstancia())) {
				total += 1;
			}
		}
		if (total >= tamaño) {
			return true;
		} else {
			return false;
		}
	}
}
