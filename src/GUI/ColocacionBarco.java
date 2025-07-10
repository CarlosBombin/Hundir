package GUI;

/**
 * Clase inmutable que representa los datos necesarios para colocar un barco en el tablero.
 * Encapsula el tipo de barco, posición y orientación seleccionados por el usuario.
 * Se utiliza para transferir información entre la interfaz gráfica y la lógica del juego.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ColocacionBarco {
    /** Tipo de barco a colocar (PORTAVIONES, SUBMARINO, DESTRUCTOR, FRAGATA) */
    private final String tipoBarco;
    /** Fila donde inicia el barco (0-7) */
    private final int fila;
    /** Columna donde inicia el barco (0-7) */
    private final int columna;
    /** Orientación del barco (HORIZONTAL o VERTICAL) */
    private final String orientacion;
    
    /**
     * Constructor que crea una colocación de barco con todos los parámetros necesarios.
     * 
     * @param tipoBarco Tipo del barco (PORTAVIONES, SUBMARINO, DESTRUCTOR, FRAGATA)
     * @param fila Fila inicial del barco (0-7)
     * @param columna Columna inicial del barco (0-7)
     * @param orientacion Orientación del barco (HORIZONTAL o VERTICAL)
     */
    public ColocacionBarco(String tipoBarco, int fila, int columna, String orientacion) {
        this.tipoBarco = tipoBarco;
        this.fila = fila;
        this.columna = columna;
        this.orientacion = orientacion;
    }
    
    /**
     * Obtiene el tipo de barco a colocar.
     * @return Tipo del barco como String
     */
    public String getTipoBarco() { return tipoBarco; }
    
    /**
     * Obtiene la fila donde inicia el barco.
     * @return Fila inicial (0-7)
     */
    public int getFila() { return fila; }
    
    /**
     * Obtiene la columna donde inicia el barco.
     * @return Columna inicial (0-7)
     */
    public int getColumna() { return columna; }
    
    /**
     * Obtiene la orientación del barco.
     * @return Orientación como String (HORIZONTAL o VERTICAL)
     */
    public String getOrientacion() { return orientacion; }
    
    /**
     * Representación textual de la colocación del barco.
     * Útil para logs y debugging.
     * 
     * @return String con información completa de la colocación
     */
    @Override
    public String toString() {
        return String.format("%s en (%d,%d) %s", tipoBarco, fila, columna, orientacion);
    }
}