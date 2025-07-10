package Barcos;
import java.util.ArrayList;
import java.util.List;

import Tablero.Casilla;

/**
 * Clase abstracta que representa un barco genérico en el juego Hundir la Flota.
 * Todos los tipos de barcos específicos deben heredar de esta clase.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public abstract class Barco {
    
    /**
     * Lista de casillas que ocupa el barco en el tablero.
     * Cada casilla representa una posición del barco.
     */
    protected List<Casilla> posiciones;
    
    /**
     * Método abstracto que determina si el barco está completamente hundido.
     * Debe ser implementado por cada tipo específico de barco.
     * 
     * @return true si el barco está hundido, false en caso contrario
     */
    public abstract boolean estaHundido();

    /**
     * Constructor por defecto que inicializa la lista de posiciones.
     * Crea una lista vacía de casillas que será llenada por las subclases.
     */
    public Barco() {
        this.posiciones = new ArrayList<>();
    }
}
