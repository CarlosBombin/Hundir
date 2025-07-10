package Sistema;

/**
 * Enumeración que define los tipos de barcos disponibles en el juego.
 * Cada tipo tiene un nombre, tamaño específico y cantidad máxima permitida
 * por jugador según las reglas del juego Hundir la Flota.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public enum TipoBarco {
    /** Portaviones: el barco más grande (4 casillas, máximo 1 por jugador) */
    PORTAVIONES("PORTAVIONES", 4, 1),
    /** Submarino: barco mediano-grande (3 casillas, máximo 2 por jugador) */
    SUBMARINO("SUBMARINO", 3, 2),
    /** Destructor: barco mediano (2 casillas, máximo 3 por jugador) */
    DESTRUCTOR("DESTRUCTOR", 2, 3),
    /** Fragata: el barco más pequeño (1 casilla, máximo 4 por jugador) */
    FRAGATA("FRAGATA", 1, 4);
    
    /** Nombre del tipo de barco */
    private final String nombre;
    /** Número de casillas que ocupa el barco */
    private final int tamaño;
    /** Cantidad máxima permitida por jugador */
    private final int cantidadMaxima;
    
    /**
     * Constructor privado para inicializar cada tipo de barco.
     * 
     * @param nombre Nombre del tipo de barco
     * @param tamaño Número de casillas que ocupa
     * @param cantidadMaxima Cantidad máxima permitida por jugador
     */
    TipoBarco(String nombre, int tamaño, int cantidadMaxima) {
        this.nombre = nombre;
        this.tamaño = tamaño;
        this.cantidadMaxima = cantidadMaxima;
    }
    
    /**
     * Obtiene el nombre del tipo de barco.
     * 
     * @return Nombre del tipo de barco
     */
    public String getNombre() { 
        return nombre; 
    }
    
    /**
     * Obtiene el tamaño del barco en número de casillas.
     * 
     * @return Número de casillas que ocupa el barco
     */
    public int getTamaño() { 
        return tamaño; 
    }
    
    /**
     * Obtiene la cantidad máxima permitida de este tipo de barco por jugador.
     * 
     * @return Cantidad máxima permitida por jugador
     */
    public int getCantidadMaxima() { 
        return cantidadMaxima; 
    }
    
    /**
     * Convierte una cadena de texto en un tipo de barco.
     * Búsqueda case-insensitive por nombre del barco.
     * 
     * @param nombre Nombre del tipo de barco a buscar
     * @return TipoBarco correspondiente al nombre o null si no se encuentra
     */
    public static TipoBarco fromString(String nombre) {
        if (nombre == null) {
            return null;
        }
        
        for (TipoBarco tipo : TipoBarco.values()) {
            if (tipo.nombre.equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        return null;
    }
}
