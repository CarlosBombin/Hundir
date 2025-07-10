package Tablero;

import Barcos.Barco;
import Estados.Estado;

public class Tablero implements Cuadricula{
	public Casilla[][] cas;
	private EstrategiaTablero estrategia;
	
	public void RellenaTablero() {
		this.cas = estrategia.crearTablero();
	}
	
	public Tablero () {
		this.estrategia = EstrategiaTableroSimple.getInstancia();
	}
	
	public Tablero (EstrategiaTablero Strategy) {
		this.estrategia = Strategy;
	}

	public void setBarco(Coordenadas id, Barco barco) {
	    getCasilla(id).setBarco(barco);
	}

	public Estado getEstado(Coordenadas id) {
		return getCasilla(id).getEstado();
	}
	
	public void getDaño(Coordenadas id) {
		getCasilla(id).getDaño();
	}
		
	public Casilla getCasilla(Coordenadas coordenadas) {
	    if (cas == null) {
	        return null;
	    }
	    for (int i = 0; i < cas.length; i++) {
	        for (int j = 0; j < cas[i].length; j++) {
	            Casilla casilla = cas[i][j];
	            int filaCasilla = casilla.getFila();
	            int colCasilla = casilla.getColumna();
	            int filaCoord = coordenadas.getAbcisasAsInt();
	            int colCoord = coordenadas.getOrdenadas();
	            if (filaCasilla == filaCoord && colCasilla == colCoord) {
	                return casilla;
	            }
	        }
	    }
	    return null;
	}
	
	public boolean todosBarcosHundidos() {
	    if (cas == null) return false;
	    for (int i = 0; i < cas.length; i++) {
	        for (int j = 0; j < cas[i].length; j++) {
	            Casilla casilla = cas[i][j];
	            if (casilla.tieneBarco() && !casilla.getBarco().estaHundido()) {
	                return false;
	            }
	        }
	    }
	    return true;
	}
}
