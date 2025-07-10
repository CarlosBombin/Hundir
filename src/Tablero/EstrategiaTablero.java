package Tablero;

/**
 * Interfaz que define el contrato para las estrategias de creación de tableros.
 * Implementa el patrón Strategy para permitir diferentes formas de crear
 * y configurar tableros de juego sin acoplar el código a una implementación específica.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface EstrategiaTablero {
    
    /**
     * Crea y retorna una matriz de casillas que representa el tablero.
     * Cada implementación define cómo se inicializan las casillas y
     * sus coordenadas correspondientes.
     * 
     * @return Matriz bidimensional de casillas que forma el tablero
     */
    public Casilla[][] crearTablero();
}
