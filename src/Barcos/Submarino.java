package Barcos;

import Estados.Tocado;
import Tablero.Casilla;

public class Submarino extends Barco{
	private int tamaño = 3;
	
	public Submarino(Casilla cas1, Casilla cas2, Casilla cas3) {
		this.posiciones.add(cas1);
		cas1.setBarco(this);
		this.posiciones.add(cas2);
		cas2.setBarco(this);
		this.posiciones.add(cas3);
		cas3.setBarco(this);
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
