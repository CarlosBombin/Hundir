package Sistema;

import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

public class ValidadorColocacion {
    
    private final Tablero tablero;
    
    public ValidadorColocacion(Tablero tablero) {
        this.tablero = tablero;
    }
    
    public boolean esValidaColocacion(String tipoBarco, int fila, int columna, String orientacion) {
        if (!esValidoTipoBarco(tipoBarco)) {
            return false;
        }
        
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        boolean esHorizontal = esOrientacionHorizontal(orientacion);
        
        return esValidaPosicion(tipo.getTamaño(), fila, columna, esHorizontal) &&
               noHayColisiones(tipo.getTamaño(), fila, columna, esHorizontal);
    }
    
    private boolean esValidoTipoBarco(String tipoBarco) {
        return TipoBarco.fromString(tipoBarco) != null;
    }
    
    private boolean esOrientacionHorizontal(String orientacion) {
        return "HORIZONTAL".equalsIgnoreCase(orientacion);
    }
    
    private boolean esValidaPosicion(int tamaño, int fila, int columna, boolean esHorizontal) {
        if (esHorizontal) {
            return columna + tamaño <= 8;
        } else {
            return fila + tamaño <= 8;
        }
    }
    
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