package Barcos;

import Estados.Tocado;
import Tablero.Casilla;

/**
 * Clase que representa una Fragata en el juego Hundir la Flota.
 * Es el barco más pequeño del juego, ocupando solamente 1 casilla.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Fragata extends Barco {
    
    /** Tamaño de la fragata en número de casillas */
    private int tamaño = 1;
    
    /**
     * Constructor que crea una fragata ocupando 1 casilla específica.
     * Establece la relación bidireccional entre el barco y su casilla.
     * 
     * @param cas1 Única casilla que ocupa la fragata
     */
    public Fragata(Casilla cas1) {
        super();
        this.posiciones.add(cas1);
        cas1.setBarco(this);
    }
    
    /**
     * Determina si la fragata está completamente hundida.
     * Una fragata está hundida cuando su única casilla ha sido tocada.
     * 
     * @return true si la casilla está en estado Tocado, false en caso contrario
     */
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
