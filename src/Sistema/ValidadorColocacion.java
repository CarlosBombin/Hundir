package Sistema;

import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

/**
 * Validador para verificar si la colocación de barcos es válida según las reglas del juego.
 * Realiza verificaciones de tipo de barco, posición en el tablero y colisiones
 * con otros barcos ya colocados.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ValidadorColocacion {
    
    /** Tablero donde se realizan las validaciones */
    private final Tablero tablero;
    
    /**
     * Constructor que inicializa el validador con un tablero específico.
     * 
     * @param tablero Tablero donde se validarán las colocaciones
     */
    public ValidadorColocacion(Tablero tablero) {
        this.tablero = tablero;
    }
    
    /**
     * Valida si una colocación de barco es válida según todas las reglas.
     * Verifica tipo de barco, posición dentro del tablero y ausencia de colisiones.
     * 
     * @param tipoBarco Tipo de barco a colocar
     * @param fila Fila donde colocar el barco
     * @param columna Columna donde colocar el barco
     * @param orientacion Orientación del barco ("HORIZONTAL" o "VERTICAL")
     * @return true si la colocación es válida, false en caso contrario
     */
    public boolean esValidaColocacion(String tipoBarco, int fila, int columna, String orientacion) {
        if (!esValidoTipoBarco(tipoBarco)) {
            return false;
        }
        
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        boolean esHorizontal = esOrientacionHorizontal(orientacion);
        
        return esValidaPosicion(tipo.getTamaño(), fila, columna, esHorizontal) &&
               noHayColisiones(tipo.getTamaño(), fila, columna, esHorizontal);
    }
    
    /**
     * Verifica si el tipo de barco especificado es válido.
     * 
     * @param tipoBarco Tipo de barco a verificar
     * @return true si el tipo es válido, false en caso contrario
     */
    private boolean esValidoTipoBarco(String tipoBarco) {
        return TipoBarco.fromString(tipoBarco) != null;
    }
    
    /**
     * Determina si la orientación especificada es horizontal.
     * 
     * @param orientacion Orientación a verificar
     * @return true si la orientación es horizontal, false si es vertical
     */
    private boolean esOrientacionHorizontal(String orientacion) {
        return "HORIZONTAL".equalsIgnoreCase(orientacion);
    }
    
    /**
     * Verifica si el barco cabe en la posición especificada dentro del tablero.
     * 
     * @param tamaño Tamaño del barco en casillas
     * @param fila Fila de inicio
     * @param columna Columna de inicio
     * @param esHorizontal true si el barco es horizontal, false si es vertical
     * @return true si el barco cabe en el tablero, false en caso contrario
     */
    private boolean esValidaPosicion(int tamaño, int fila, int columna, boolean esHorizontal) {
        if (esHorizontal) {
            return columna + tamaño <= 8;
        } else {
            return fila + tamaño <= 8;
        }
    }
    
    /**
     * Verifica que no haya colisiones con otros barcos en las posiciones objetivo.
     * 
     * @param tamaño Tamaño del barco en casillas
     * @param fila Fila de inicio
     * @param columna Columna de inicio
     * @param esHorizontal true si el barco es horizontal, false si es vertical
     * @return true si no hay colisiones, false si hay conflictos
     */
    private boolean noHayColisiones(int tamaño, int fila, int columna, boolean esHorizontal) {
        for (int i = 0; i < tamaño; i++) {
            int checkFila = esHorizontal ? fila : fila + i;
            int checkColumna = esHorizontal ? columna + i : columna;
            boolean colision = existeBarcoEnPosicion(checkFila, checkColumna);
            if (colision) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica si existe un barco en la posición especificada.
     * 
     * @param fila Fila a verificar
     * @param columna Columna a verificar
     * @return true si existe un barco en la posición, false en caso contrario
     */
    private boolean existeBarcoEnPosicion(int fila, int columna) {
        try {
            Coordenadas coordenadas = new Coordenadas((char)('A' + columna), fila);
            Casilla casilla = tablero.getCasilla(coordenadas);
            if (casilla == null) {
                return true;
            }
            return casilla.getEstado() instanceof Estados.DesconocidoBarco;
        } catch (Exception e) {
            return true;
        }
    }
}