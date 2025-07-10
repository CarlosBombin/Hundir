package Barcos;
import Estados.Tocado;
import Tablero.Casilla;

/**
 * Clase que representa un Destructor en el juego Hundir la Flota.
 * Es un barco pequeño, ocupando 2 casillas.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Destructor extends Barco {
    
    /** Tamaño del destructor en número de casillas */
    private int tamaño = 2;
    
    /**
     * Constructor que crea un destructor ocupando 2 casillas específicas.
     * Establece la relación bidireccional entre el barco y sus casillas.
     * 
     * @param cas1 Primera casilla del destructor
     * @param cas2 Segunda casilla del destructor
     */
    public Destructor(Casilla cas1, Casilla cas2) {
        super();
        this.posiciones.add(cas1);
        cas1.setBarco(this);
        this.posiciones.add(cas2);
        cas2.setBarco(this);
    }
    
    /**
     * Determina si el destructor está completamente hundido.
     * Un destructor está hundido cuando todas sus 2 casillas han sido tocadas.
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
