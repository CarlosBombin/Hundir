package Barcos;

import Estados.Tocado;
import Tablero.Casilla;

/**
 * Clase que representa un Submarino en el juego Hundir la Flota.
 * Es un barco de tamaño medio, ocupando 3 casillas.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Submarino extends Barco {
    
    /** Tamaño del submarino en número de casillas */
    private int tamaño = 3;
    
    /**
     * Constructor que crea un submarino ocupando 3 casillas específicas.
     * Establece la relación bidireccional entre el barco y sus casillas.
     * 
     * @param cas1 Primera casilla del submarino
     * @param cas2 Segunda casilla del submarino
     * @param cas3 Tercera casilla del submarino
     */
    public Submarino(Casilla cas1, Casilla cas2, Casilla cas3) {
        super();
        this.posiciones.add(cas1);
        cas1.setBarco(this);
        this.posiciones.add(cas2);
        cas2.setBarco(this);
        this.posiciones.add(cas3);
        cas3.setBarco(this);
    }
    
    /**
     * Determina si el submarino está completamente hundido.
     * Un submarino está hundido cuando todas sus 3 casillas han sido tocadas.
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
