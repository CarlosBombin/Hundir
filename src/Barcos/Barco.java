package Barcos;
import java.util.List;

import Tablero.Casilla;

public abstract class Barco {
	protected List<Casilla> posiciones;
	
	public abstract boolean estaHundido();
}
