package Estados;

import Cliente.Usuario;

/**
 * Interfaz que define el comportamiento de los estados de una partida.
 * Implementa el patrón State para gestionar las diferentes fases del juego:
 * colocación de barcos, espera de rival y partida lista.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface EstadoPartida {
    
    /**
     * Determina si un usuario puede colocar barcos en el estado actual.
     * 
     * @param usuario Usuario que intenta colocar un barco
     * @param esPrincipal true si el usuario es el jugador principal, false si es el rival
     * @return true si puede colocar barcos, false en caso contrario
     */
    boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal);
    
    /**
     * Gestiona la transición de estado cuando un jugador finaliza la colocación.
     * 
     * @param esPrincipal true si quien finaliza es el jugador principal, false si es el rival
     * @return El nuevo estado de la partida después de la transición
     */
    EstadoPartida finalizarColocacion(boolean esPrincipal);
    
    /**
     * Obtiene una descripción textual del estado actual para el jugador especificado.
     * 
     * @param esPrincipal true si se solicita la descripción para el jugador principal
     * @return Descripción del estado actual
     */
    String obtenerDescripcion(boolean esPrincipal);
    
    /**
     * Indica si la partida está completa y lista para comenzar la fase de combate.
     * 
     * @return true si ambos jugadores han terminado la colocación, false en caso contrario
     */
    boolean partidaCompleta();
}
