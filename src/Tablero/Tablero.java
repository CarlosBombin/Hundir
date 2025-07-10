package Tablero;

import Barcos.Barco;
import Estados.Estado;

/**
 * Representa el tablero de juego completo utilizando el patrón Strategy.
 * Gestiona una matriz de casillas y delega la creación del tablero
 * a una estrategia específica, permitiendo flexibilidad en la implementación.
 * 
 * Implementa la interfaz Cuadricula y utiliza el patrón Strategy
 * para la creación y gestión del tablero de casillas.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Tablero implements Cuadricula {
    /** Matriz de casillas que componen el tablero */
    public Casilla[][] cas;
    /** Estrategia utilizada para crear y gestionar el tablero */
    private EstrategiaTablero estrategia;
    
    /**
     * Inicializa el tablero utilizando la estrategia configurada.
     * Delega la creación de la matriz de casillas a la estrategia actual.
     */
    public void RellenaTablero() {
        this.cas = estrategia.crearTablero();
    }
    
    /**
     * Constructor por defecto que utiliza la estrategia simple.
     * Inicializa el tablero con EstrategiaTableroSimple.
     */
    public Tablero() {
        this.estrategia = EstrategiaTableroSimple.getInstancia();
    }
    
    /**
     * Constructor que permite inyectar una estrategia específica.
     * Útil para testing y configuraciones personalizadas.
     * 
     * @param Strategy Estrategia de tablero a utilizar
     */
    public Tablero(EstrategiaTablero Strategy) {
        this.estrategia = Strategy;
    }

    /**
     * Asigna un barco a una casilla específica del tablero.
     * 
     * @param id Coordenadas donde colocar el barco
     * @param barco Barco a colocar en la casilla
     */
    public void setBarco(Coordenadas id, Barco barco) {
        getCasilla(id).setBarco(barco);
    }

    /**
     * Obtiene el estado de una casilla específica.
     * 
     * @param id Coordenadas de la casilla
     * @return Estado actual de la casilla
     */
    public Estado getEstado(Coordenadas id) {
        return getCasilla(id).getEstado();
    }
    
    /**
     * Aplica daño a una casilla específica del tablero.
     * 
     * @param id Coordenadas de la casilla a atacar
     */
    public void getDaño(Coordenadas id) {
        getCasilla(id).getDaño();
    }
    
    /**
     * Busca y retorna la casilla que corresponde a las coordenadas especificadas.
     * Realiza una búsqueda lineal en la matriz de casillas comparando coordenadas.
     * 
     * @param coordenadas Coordenadas de la casilla buscada
     * @return Casilla que coincide con las coordenadas o null si no se encuentra
     */
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
    
    /**
     * Verifica si todos los barcos en el tablero han sido hundidos.
     * Recorre todas las casillas buscando barcos que aún no estén hundidos.
     * 
     * @return true si todos los barcos están hundidos, false si queda alguno flotando
     */
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
