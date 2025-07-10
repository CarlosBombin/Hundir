package Barcos;

import Estados.Tocado;
import Tablero.Casilla;

/**
 * Clase que representa un Portaviones en el juego Hundir la Flota.
 * Es el barco más grande del juego, ocupando 4 casillas.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Portaviones extends Barco {
    
    /** Tamaño del portaviones en número de casillas */
    private int tamaño = 4;
    
    /**
     * Constructor que crea un portaviones ocupando 4 casillas específicas.
     * Establece la relación bidireccional entre el barco y sus casillas.
     * 
     * @param cas1 Primera casilla del portaviones
     * @param cas2 Segunda casilla del portaviones
     * @param cas3 Tercera casilla del portaviones
     * @param cas4 Cuarta casilla del portaviones
     */
    public Portaviones(Casilla cas1, Casilla cas2, Casilla cas3, Casilla cas4) {
        super();
        this.posiciones.add(cas1);
        cas1.setBarco(this);
        this.posiciones.add(cas2);
        cas2.setBarco(this);
        this.posiciones.add(cas3);
        cas3.setBarco(this);
        this.posiciones.add(cas4);
        cas4.setBarco(this);
    }
    
    /**
     * Determina si el portaviones está completamente hundido.
     * Un portaviones está hundido cuando todas sus 4 casillas han sido tocadas.
     * 
     * @return true si todas las casillas están en estado Tocado, false en caso contrario
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
