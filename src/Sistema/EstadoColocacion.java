package Sistema;

import Estados.EstadoPartida;
import Estados.ColocacionPrincipal;
import Cliente.Usuario;

/**
 * Gestor del estado de colocación de barcos en una partida.
 * Coordina el proceso de colocación entre dos jugadores utilizando
 * el patrón State para gestionar las diferentes fases de colocación.
 * 
 * Mantiene contadores independientes para cada jugador y coordina
 * las transiciones entre estados según el progreso de la colocación.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class EstadoColocacion {
    
    /** Estado actual de la partida en el proceso de colocación */
    private EstadoPartida estadoActual;
    
    /** Contador de barcos del jugador principal */
    private ContadorBarcosJugador contadorPrincipal;
    /** Contador de barcos del jugador rival */
    private ContadorBarcosJugador contadorRival;
    
    /**
     * Constructor que inicializa el estado de colocación.
     * Comienza en el estado ColocacionPrincipal y crea contadores para ambos jugadores.
     */
    public EstadoColocacion() {
        this.estadoActual = ColocacionPrincipal.getInstancia();
        this.contadorPrincipal = new ContadorBarcosJugador();
        this.contadorRival = new ContadorBarcosJugador();
    }
    
    /**
     * Verifica si un usuario puede colocar un barco en el estado actual.
     * 
     * @param usuario Usuario que quiere colocar el barco
     * @param esPrincipal true si es el jugador principal, false si es el rival
     * @return true si puede colocar el barco, false en caso contrario
     */
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return estadoActual.puedeColocarBarco(usuario, esPrincipal);
    }
    
    /**
     * Finaliza la colocación para un jugador y cambia al siguiente estado.
     * 
     * @param esPrincipal true si es el jugador principal, false si es el rival
     */
    public void finalizarColocacion(boolean esPrincipal) {
        this.estadoActual = estadoActual.finalizarColocacion(esPrincipal);
    }
    
    /**
     * Obtiene la descripción del estado actual para un jugador específico.
     * 
     * @param esPrincipal true si es el jugador principal, false si es el rival
     * @return Descripción del estado actual
     */
    public String obtenerDescripcion(boolean esPrincipal) {
        return estadoActual.obtenerDescripcion(esPrincipal);
    }
    
    /**
     * Verifica si la partida está completamente lista para comenzar.
     * La partida está completa cuando ambos jugadores han terminado la colocación.
     * 
     * @return true si la partida está lista, false en caso contrario
     */
    public boolean partidaCompleta() {
        return estadoActual.partidaCompleta();
    }
    
    /**
     * Obtiene el contador de barcos del jugador principal.
     * 
     * @return Contador de barcos del jugador principal
     */
    public ContadorBarcosJugador getContadorPrincipal() {
        return contadorPrincipal;
    }
    
    /**
     * Obtiene el contador de barcos del jugador rival.
     * 
     * @return Contador de barcos del jugador rival
     */
    public ContadorBarcosJugador getContadorRival() {
        return contadorRival;
    }
    
    /**
     * Obtiene el contador de barcos de un jugador específico.
     * 
     * @param esPrincipal true para obtener el contador del principal, false para el rival
     * @return Contador de barcos del jugador especificado
     */
    public ContadorBarcosJugador getContador(boolean esPrincipal) {
        if (esPrincipal) {
            return contadorPrincipal;
        } else {
            return contadorRival;
        }
    }
    
    /**
     * Obtiene el nombre de la clase del estado actual.
     * Útil para debugging y logging del estado de la partida.
     * 
     * @return Nombre simple de la clase del estado actual
     */
    public String getEstadoActual() {
        return estadoActual.getClass().getSimpleName();
    }
}
