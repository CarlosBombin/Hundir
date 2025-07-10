package Estados;

/**
 * Clase que representa el estado inicial de una casilla vacía en el tablero.
 * Indica que la casilla no ha sido atacada y no contiene ningún barco.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class DesconocidoAgua implements Estado {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static Estado instancia;
    
    /**
     * Obtiene la única instancia de la clase DesconocidoAgua.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de DesconocidoAgua
     */
    public static Estado getInstancia() {
        if (instancia == null) {
            instancia = new DesconocidoAgua();
        }
        return instancia;
    }
    
    /**
     * Define la transición de estado cuando una casilla vacía recibe daño.
     * Al atacar una casilla vacía, se revela que contiene agua.
     * 
     * @return Estado Agua, indicando que la casilla ha sido atacada y está vacía
     */
    public Estado getDaño() {
        return Agua.getInstancia();
    }
}
