package Barcos;
import java.util.ArrayList;
import java.util.List;

import Tablero.Casilla;

public abstract class Barco {
	protected List<Casilla> posiciones;
	
	public abstract boolean estaHundido();

	public Barco() {
        this.posiciones = new ArrayList<>();
    }
}
