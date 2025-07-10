package Estados;

import Cliente.Usuario;

/**
 * Estado de partida que indica que es el turno del jugador principal para colocar barcos.
 * El jugador rival debe esperar hasta que el principal termine su colocación.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ColocacionPrincipal implements EstadoPartida {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static EstadoPartida instancia;
    
    /**
     * Obtiene la única instancia de la clase ColocacionPrincipal.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de ColocacionPrincipal
     */
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new ColocacionPrincipal();
        }
        return instancia;
    }
    
    /**
     * Determina si un usuario puede colocar barcos en este estado.
     * Solo el jugador principal puede colocar barcos en este estado.
     * 
     * @param usuario Usuario que intenta colocar un barco (no utilizado en esta implementación)
     * @param esPrincipal true si el usuario es el jugador principal
     * @return true solo si esPrincipal es true
     */
    @Override
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return esPrincipal;
    }
    
    /**
     * Gestiona la transición cuando el jugador principal finaliza su colocación.
     * Cambia al estado ColocacionRival para que el rival pueda colocar sus barcos.
     * 
     * @param esPrincipal true si quien finaliza es el jugador principal
     * @return ColocacionRival si el principal finaliza, este mismo estado en caso contrario
     */
    @Override
    public EstadoPartida finalizarColocacion(boolean esPrincipal) {
        if (esPrincipal) {
            return ColocacionRival.getInstancia();
        }
        return this;
    }
    
    /**
     * Obtiene la descripción del estado para el jugador especificado.
     * 
     * @param esPrincipal true si se solicita para el jugador principal
     * @return "turno_principal" si es el principal, "esperar" si es el rival
     */
    @Override
    public String obtenerDescripcion(boolean esPrincipal) {
        if (esPrincipal) {
            return "turno_principal";
        } else {
            return "esperar";
        }
    }
    
    /**
     * Indica si la partida está completa.
     * En este estado, la partida aún no está completa.
     * 
     * @return false, ya que falta que el rival coloque sus barcos
     */
    @Override
    public boolean partidaCompleta() {
        return false;
    }
}
