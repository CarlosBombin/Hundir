package Estados;

/**
 * Clase que representa el estado "Tocado" de una casilla en el tablero.
 * Indica que la casilla contenía parte de un barco y ha sido atacada exitosamente.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Tocado implements Estado {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static Estado instancia;
    
    /**
     * Obtiene la única instancia de la clase Tocado.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de Tocado
     */
    public static Estado getInstancia() {
        if (instancia == null) {
            instancia = new Tocado();
        }
        return instancia;
    }
    
    /**
     * Define el comportamiento cuando una casilla tocada recibe daño adicional.
     * Una casilla ya tocada permanece en el mismo estado al recibir más ataques.
     * 
     * @return El mismo estado Tocado, ya que no puede cambiar más
     */
    public Estado getDaño() {
        return Tocado.getInstancia();
    }
}
