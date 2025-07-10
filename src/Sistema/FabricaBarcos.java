package Sistema;

import Barcos.*;
import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

/**
 * Fábrica para crear barcos en el tablero de juego.
 * Implementa el patrón Factory para crear instancias de barcos específicos
 * según el tipo solicitado, gestionando las casillas del tablero y
 * la asignación de barcos a las mismas.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class FabricaBarcos {
    
    /** Tablero donde se crearán los barcos */
    private final Tablero tablero;
    
    /**
     * Constructor que inicializa la fábrica con un tablero específico.
     * 
     * @param tablero Tablero donde se crearán los barcos
     */
    public FabricaBarcos(Tablero tablero) {
        this.tablero = tablero;
    }
    
    /**
     * Crea un barco del tipo especificado en la posición y orientación dadas.
     * 
     * @param tipoBarco Tipo de barco a crear
     * @param fila Fila donde colocar el barco
     * @param columna Columna donde colocar el barco
     * @param orientacion Orientación del barco ("HORIZONTAL" o "VERTICAL")
     * @return Instancia del barco creado o null si no se puede crear
     */
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
     * Crea un portaviones en la posición y orientación especificadas.
     * 
     * @param fila Fila donde colocar el portaviones
     * @param columna Columna donde colocar el portaviones
     * @param esHorizontal true si es horizontal, false si es vertical
     * @return Instancia de Portaviones creada o null si no se puede crear
     */
    private Portaviones crearPortaviones(int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = obtenerCasillas(4, fila, columna, esHorizontal);
        if (casillas == null) {
            return null;
        }
        
        Portaviones portaviones = new Portaviones(casillas[0], casillas[1], casillas[2], casillas[3]);
        asignarBarcoACasillas(portaviones, casillas);
        return portaviones;
    }
    
    /**
     * Crea un submarino en la posición y orientación especificadas.
     * 
     * @param fila Fila donde colocar el submarino
     * @param columna Columna donde colocar el submarino
     * @param esHorizontal true si es horizontal, false si es vertical
     * @return Instancia de Submarino creada o null si no se puede crear
     */
    private Submarino crearSubmarino(int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = obtenerCasillas(3, fila, columna, esHorizontal);
        if (casillas == null) {
            return null;
        }
        
        Submarino submarino = new Submarino(casillas[0], casillas[1], casillas[2]);
        asignarBarcoACasillas(submarino, casillas);
        return submarino;
    }
    
    /**
     * Crea un destructor en la posición y orientación especificadas.
     * 
     * @param fila Fila donde colocar el destructor
     * @param columna Columna donde colocar el destructor
     * @param esHorizontal true si es horizontal, false si es vertical
     * @return Instancia de Destructor creada o null si no se puede crear
     */
    private Destructor crearDestructor(int fila, int columna, boolean esHorizontal) {
        Casilla[] casillas = obtenerCasillas(2, fila, columna, esHorizontal);
        if (casillas == null) {
            return null;
        }
        
        Destructor destructor = new Destructor(casillas[0], casillas[1]);
        asignarBarcoACasillas(destructor, casillas);
        return destructor;
    }
    
    /**
     * Crea una fragata en la posición especificada.
     * Las fragatas solo ocupan una casilla, por lo que no tienen orientación.
     * 
     * @param fila Fila donde colocar la fragata
     * @param columna Columna donde colocar la fragata
     * @return Instancia de Fragata creada o null si no se puede crear
     */
    private Fragata crearFragata(int fila, int columna) {
        Casilla[] casillas = obtenerCasillas(1, fila, columna, true);
        if (casillas == null) {
            return null;
        }
        
        Fragata fragata = new Fragata(casillas[0]);
        asignarBarcoACasillas(fragata, casillas);
        return fragata;
    }
    
    /**
     * Obtiene las casillas necesarias para colocar un barco del tamaño especificado.
     * 
     * @param cantidad Número de casillas necesarias
     * @param fila Fila de inicio
     * @param columna Columna de inicio
     * @param esHorizontal true si el barco es horizontal, false si es vertical
     * @return Array de casillas o null si no se pueden obtener
     */
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
    
    /**
     * Asigna un barco a todas las casillas que ocupa.
     * Establece la referencia del barco en cada casilla correspondiente.
     * 
     * @param barco Barco a asignar
     * @param casillas Array de casillas que ocupará el barco
     */
    private void asignarBarcoACasillas(Barco barco, Casilla[] casillas) {
        for (Casilla casilla : casillas) {
            casilla.setBarco(barco);
        }
    }
}
