package Estados;

/**
 * Clase que representa el estado inicial de una casilla que contiene parte de un barco.
 * Indica que la casilla tiene un barco pero no ha sido atacada aún.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class DesconocidoBarco implements Estado {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static Estado instancia;
    
    /**
     * Obtiene la única instancia de la clase DesconocidoBarco.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de DesconocidoBarco
     */
    public static Estado getInstancia() {
        if (instancia == null) {
            instancia = new DesconocidoBarco();
        }
        return instancia;
    }
    
    /**
     * Define la transición de estado cuando una casilla con barco recibe daño.
     * Al atacar una casilla con barco, este resulta tocado.
     * 
     * @return Estado Tocado, indicando que el barco ha sido impactado
     */
    public Estado getDaño() {
        return Tocado.getInstancia();
    }
}
