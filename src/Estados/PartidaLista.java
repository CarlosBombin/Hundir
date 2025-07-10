package Estados;

import Cliente.Usuario;

/**
 * Estado de partida que indica que ambos jugadores han terminado de colocar sus barcos.
 * La partida está lista para comenzar la fase de combate.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class PartidaLista implements EstadoPartida {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static EstadoPartida instancia;
    
    /**
     * Obtiene la única instancia de la clase PartidaLista.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de PartidaLista
     */
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new PartidaLista();
        }
        return instancia;
    }
    
    /**
     * Determina si un usuario puede colocar barcos en este estado.
     * Cuando la partida está lista, ya no se pueden colocar más barcos.
     * 
     * @param usuario Usuario que intenta colocar un barco (no utilizado)
     * @param esPrincipal true si el usuario es el jugador principal (no utilizado)
     * @return false, ya que la fase de colocación ha terminado
     */
    @Override
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return false;
    }
    
    /**
     * Gestiona la transición cuando se intenta finalizar colocación.
     * Como la partida ya está lista, no hay transición posible.
     * 
     * @param esPrincipal true si quien finaliza es el jugador principal (no utilizado)
     * @return El mismo estado, ya que no puede cambiar más
     */
    @Override
    public EstadoPartida finalizarColocacion(boolean esPrincipal) {
        return this;
    }
    
    /**
     * Obtiene la descripción del estado actual.
     * 
     * @param esPrincipal true si se solicita para el jugador principal (no utilizado)
     * @return "completo" indicando que la partida está lista para jugar
     */
    @Override
    public String obtenerDescripcion(boolean esPrincipal) {
        return "completo";
    }
    
    /**
     * Indica si la partida está completa y lista para el combate.
     * 
     * @return true, ya que ambos jugadores han terminado la colocación
     */
    @Override
    public boolean partidaCompleta() {
        return true;
    }
}
