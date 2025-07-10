package Estados;

import Cliente.Usuario;

/**
 * Estado de partida que indica que es el turno del jugador rival para colocar barcos.
 * El jugador principal debe esperar hasta que el rival termine su colocación.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ColocacionRival implements EstadoPartida {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static EstadoPartida instancia;
    
    /**
     * Obtiene la única instancia de la clase ColocacionRival.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de ColocacionRival
     */
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new ColocacionRival();
        }
        return instancia;
    }
    
    /**
     * Determina si un usuario puede colocar barcos en este estado.
     * Solo el jugador rival puede colocar barcos en este estado.
     * 
     * @param usuario Usuario que intenta colocar un barco (no utilizado en esta implementación)
     * @param esPrincipal true si el usuario es el jugador principal
     * @return true solo si esPrincipal es false (es el rival)
     */
    @Override
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return !esPrincipal;
    }
    
    /**
     * Gestiona la transición cuando el jugador rival finaliza su colocación.
     * Cambia al estado PartidaLista cuando el rival termina de colocar.
     * 
     * @param esPrincipal true si quien finaliza es el jugador principal
     * @return PartidaLista si el rival finaliza, este mismo estado en caso contrario
     */
    @Override
    public EstadoPartida finalizarColocacion(boolean esPrincipal) {
        if (!esPrincipal) {
            return PartidaLista.getInstancia();
        }
        return this;
    }
    
    /**
     * Obtiene la descripción del estado para el jugador especificado.
     * 
     * @param esPrincipal true si se solicita para el jugador principal
     * @return "turno_rival" si es el rival, "esperar" si es el principal
     */
    @Override
    public String obtenerDescripcion(boolean esPrincipal) {
        if (!esPrincipal) {
            return "turno_rival";
        } else {
            return "esperar";
        }
    }
    
    /**
     * Indica si la partida está completa.
     * En este estado, la partida aún no está completa.
     * 
     * @return false, ya que el rival aún está colocando sus barcos
     */
    @Override
    public boolean partidaCompleta() {
        return false;
    }
}
