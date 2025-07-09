package GUI;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class BarcoFactory {
    private static final Map<String, BarcoInfo> TIPOS_BARCO = new HashMap<>();
    
    static {
        TIPOS_BARCO.put("PORTAVIONES", new BarcoInfo(4, Color.RED, "P"));
        TIPOS_BARCO.put("SUBMARINO", new BarcoInfo(3, Color.BLUE, "S"));
        TIPOS_BARCO.put("DESTRUCTOR", new BarcoInfo(2, Color.GREEN, "D"));
        TIPOS_BARCO.put("FRAGATA", new BarcoInfo(1, Color.ORANGE, "F"));
    }
    
    public static BarcoInfo obtenerInfo(String tipo) {
        return TIPOS_BARCO.getOrDefault(tipo, 
            new BarcoInfo(1, Color.GRAY, "?"));
    }
    
    public static boolean esTipoValido(String tipo) {
        return TIPOS_BARCO.containsKey(tipo);
    }
    
    public static void registrarTipoBarco(String tipo, int tamaño, Color color, String simbolo) {
        TIPOS_BARCO.put(tipo, new BarcoInfo(tamaño, color, simbolo));
    }
    
    public static class BarcoInfo {
        private final int tamaño;
        private final Color color;
        private final String simbolo;
        
        public BarcoInfo(int tamaño, Color color, String simbolo) {
            this.tamaño = tamaño;
            this.color = color;
            this.simbolo = simbolo;
        }
        
        public int getTamaño() { return tamaño; }
        public Color getColor() { return color; }
        public String getSimbolo() { return simbolo; }
    }
}