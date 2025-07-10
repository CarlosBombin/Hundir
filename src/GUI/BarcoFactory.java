package GUI;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory que gestiona la información de los diferentes tipos de barcos del juego.
 * Proporciona datos sobre tamaño, color de visualización y símbolo para cada tipo de barco.
 * Utiliza el patrón Factory para centralizar la creación y configuración de barcos.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class BarcoFactory {
    
    /** Mapa que almacena la información de cada tipo de barco */
    private static final Map<String, BarcoInfo> TIPOS_BARCO = new HashMap<>();
    
    static {
        TIPOS_BARCO.put("PORTAVIONES", new BarcoInfo(4, Color.RED, "P"));
        TIPOS_BARCO.put("SUBMARINO", new BarcoInfo(3, Color.BLUE, "S"));
        TIPOS_BARCO.put("DESTRUCTOR", new BarcoInfo(2, Color.GREEN, "D"));
        TIPOS_BARCO.put("FRAGATA", new BarcoInfo(1, Color.ORANGE, "F"));
    }
    
    /**
     * Obtiene la información completa de un tipo de barco específico.
     * Si el tipo no existe, devuelve información por defecto.
     * 
     * @param tipo Nombre del tipo de barco (PORTAVIONES, SUBMARINO, DESTRUCTOR, FRAGATA)
     * @return BarcoInfo con la información del barco o valores por defecto si no existe
     */
    public static BarcoInfo obtenerInfo(String tipo) {
        return TIPOS_BARCO.getOrDefault(tipo, 
            new BarcoInfo(1, Color.GRAY, "?"));
    }
    
    /**
     * Verifica si un tipo de barco es válido en el juego.
     * 
     * @param tipo Nombre del tipo de barco a validar
     * @return true si el tipo existe en el sistema, false en caso contrario
     */
    public static boolean esTipoValido(String tipo) {
        return TIPOS_BARCO.containsKey(tipo);
    }
    
    /**
     * Registra un nuevo tipo de barco en el sistema.
     * Permite extender el juego con nuevos tipos de barcos.
     * 
     * @param tipo Nombre del nuevo tipo de barco
     * @param tamaño Número de casillas que ocupa el barco
     * @param color Color para visualizar el barco en el tablero
     * @param simbolo Símbolo de una letra para mostrar en las casillas
     */
    public static void registrarTipoBarco(String tipo, int tamaño, Color color, String simbolo) {
        TIPOS_BARCO.put(tipo, new BarcoInfo(tamaño, color, simbolo));
    }
    
    /**
     * Clase interna que encapsula la información de un tipo de barco.
     * Contiene el tamaño, color de visualización y símbolo del barco.
     */
    public static class BarcoInfo {
        /** Número de casillas que ocupa el barco */
        private final int tamaño;
        /** Color para mostrar el barco en el tablero */
        private final Color color;
        /** Símbolo de una letra para mostrar en las casillas */
        private final String simbolo;
        
        /**
         * Constructor que crea la información de un barco.
         * 
         * @param tamaño Número de casillas que ocupa
         * @param color Color de visualización
         * @param simbolo Símbolo de una letra
         */
        public BarcoInfo(int tamaño, Color color, String simbolo) {
            this.tamaño = tamaño;
            this.color = color;
            this.simbolo = simbolo;
        }
        
        /**
         * Obtiene el tamaño del barco en casillas.
         * @return Número de casillas que ocupa el barco
         */
        public int getTamaño() { return tamaño; }
        
        /**
         * Obtiene el color de visualización del barco.
         * @return Color para mostrar en el tablero
         */
        public Color getColor() { return color; }
        
        /**
         * Obtiene el símbolo del barco.
         * @return Símbolo de una letra para mostrar en las casillas
         */
        public String getSimbolo() { return simbolo; }
    }
}