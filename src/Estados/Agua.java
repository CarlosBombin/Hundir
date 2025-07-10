package Estados;

/**
 * Clase que representa el estado "Agua" de una casilla en el tablero.
 * Indica que la casilla ha sido atacada y no contenía ningún barco.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Agua implements Estado {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static Estado instancia;
    
    /**
     * Obtiene la única instancia de la clase Agua.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de Agua
     */
    public static Estado getInstancia() {
        if (instancia == null) {
            instancia = new Agua();
        }
        return instancia;
    }
    
    /**
     * Define el comportamiento cuando una casilla de agua recibe daño adicional.
     * Una casilla de agua permanece como agua al recibir más ataques.
     * 
     * @return El mismo estado Agua, ya que no puede cambiar
     */
    public Estado getDaño() {
        return Agua.getInstancia();
    }
}
