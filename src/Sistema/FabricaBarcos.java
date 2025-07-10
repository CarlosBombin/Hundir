package Sistema;

import Barcos.*;
import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

public class FabricaBarcos {
    
    private final Tablero tablero;
    
    public FabricaBarcos(Tablero tablero) {
        this.tablero = tablero;
    }
    
    public Barco crearBarco(String tipoBarco, int fila, int columna, String orientacion) {
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            return null;
        }
        
        boolean esHorizontal = esOrientacionHorizontal(orientacion);
        
        switch (tipo) {
            case PORTAVIONES:
                return crearPortaviones(fila, columna, esHorizontal);
            case SUBMARINO:
                return crearSubmarino(fila, columna, esHorizontal);
            case DESTRUCTOR:
                return crearDestructor(fila, columna, esHorizontal);
            case FRAGATA:
                return crearFragata(fila, columna);
            default:
                return null;
        }
    }
    
    private boolean esOrientacionHorizontal(String orientacion) {
        return "HORIZONTAL".equalsIgnoreCase(orientacion);
    }
    
    private Portaviones crearPortaviones(int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = obtenerCasillas(4, fila, columna, esHorizontal);
        if (casillas == null) {
            return null;
        }
        
        Portaviones portaviones = new Portaviones(casillas[0], casillas[1], casillas[2], casillas[3]);
        asignarBarcoACasillas(portaviones, casillas);
        return portaviones;
    }
    
    private Submarino crearSubmarino(int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = obtenerCasillas(3, fila, columna, esHorizontal);
        if (casillas == null) {
            return null;
        }
        
        Submarino submarino = new Submarino(casillas[0], casillas[1], casillas[2]);
        asignarBarcoACasillas(submarino, casillas);
        return submarino;
    }
    
    private Destructor crearDestructor(int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = obtenerCasillas(2, fila, columna, esHorizontal);
        if (casillas == null) {
            return null;
        }
        
        Destructor destructor = new Destructor(casillas[0], casillas[1]);
        asignarBarcoACasillas(destructor, casillas);
        return destructor;
    }
    
    private Fragata crearFragata(int fila, int columna) {
        Casilla[] casillas = obtenerCasillas(1, fila, columna, true);
        if (casillas == null) {
            return null;
        }
        
        Fragata fragata = new Fragata(casillas[0]);
        asignarBarcoACasillas(fragata, casillas);
        return fragata;
    }
    
    private Casilla[] obtenerCasillas(int cantidad, int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = new Casilla[cantidad];
        
        for (int i = 0; i < cantidad; i++) {
            int targetFila = esHorizontal ? fila : fila + i;
            int targetColumna = esHorizontal ? columna + i : columna;
            Coordenadas coord = new Coordenadas((char)('A' + targetColumna), targetFila);
            casillas[i] = tablero.getCasilla(coord);

            if (casillas[i] == null) {
                return null;
            }
        }
        
        return casillas;
    }
    
    private void asignarBarcoACasillas(Barco barco, Casilla[] casillas) {
        for (Casilla casilla : casillas) {
            casilla.setBarco(barco);
        }
    }
}
