package Estados;

import Cliente.Usuario;

/**
 * Estado de partida que indica que se está esperando a que se una un rival.
 * En este estado, ningún jugador puede colocar barcos hasta que la partida esté completa.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class EsperandoRival implements EstadoPartida {
    
    /** Única instancia de la clase (patrón Singleton) */
    private static EstadoPartida instancia;
    
    /**
     * Obtiene la única instancia de la clase EsperandoRival.
     * Implementa el patrón Singleton de forma lazy.
     * 
     * @return La instancia única de EsperandoRival
     */
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new EsperandoRival();
        }
        return instancia;
    }
    
    /**
     * Determina si un usuario puede colocar barcos en este estado.
     * Mientras se espera rival, ningún jugador puede colocar barcos.
     * 
     * @param usuario Usuario que intenta colocar un barco (no utilizado)
     * @param esPrincipal true si el usuario es el jugador principal (no utilizado)
     * @return false, ya que no se puede colocar barcos sin rival
     */
    @Override
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return false;
    }
    
    /**
     * Gestiona la transición cuando se intenta finalizar colocación.
     * En este estado no hay transición posible hasta que se una un rival.
     * 
     * @param esPrincipal true si quien finaliza es el jugador principal (no utilizado)
     * @return El mismo estado, ya que no puede cambiar sin rival
     */
    @Override
    public EstadoPartida finalizarColocacion(boolean esPrincipal) {
        return this;
    }
    
    /**
     * Obtiene la descripción del estado actual.
     * 
     * @param esPrincipal true si se solicita para el jugador principal (no utilizado)
     * @return "esperar_rival" indicando que se está esperando un oponente
     */
    @Override
    public String obtenerDescripcion(boolean esPrincipal) {
        return "esperar_rival";
    }
    
    /**
     * Indica si la partida está completa.
     * Mientras se espera rival, la partida no está completa.
     * 
     * @return false, ya que falta el rival para completar la partida
     */
    @Override
    public boolean partidaCompleta() {
        return false;
    }
}
