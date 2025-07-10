package GUI;

/**
 * Interfaz que define los métodos para manejar diferentes tipos de mensajes
 * recibidos del servidor durante el juego. Implementa el patrón Observer
 * para desacoplar el procesamiento de mensajes de la lógica de comunicación.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface ManejadorMensajes {
    
    /**
     * Maneja el evento cuando se encuentra un rival para la partida.
     * Se ejecuta cuando el servidor notifica que un segundo jugador
     * se ha unido a la partida.
     * 
     * @param rival Nombre del jugador rival que se ha unido
     */
    void rivalEncontrado(String rival);
    
    /**
     * Maneja el evento cuando la partida está lista para comenzar.
     * Se ejecuta cuando ambos jugadores están conectados y pueden
     * empezar la fase de colocación de barcos.
     * 
     * @param info Información adicional sobre el estado de la partida
     */
    void partidaLista(String info);
    
    /**
     * Maneja el evento cuando es el turno del jugador para colocar barcos.
     * Se ejecuta cuando el servidor indica que el jugador puede comenzar
     * o continuar colocando sus barcos en el tablero.
     */
    void turnoColocacion();
    
    /**
     * Maneja el evento cuando el jugador debe esperar durante la colocación.
     * Se ejecuta cuando es el turno del rival para colocar barcos y el
     * jugador actual debe esperar.
     */
    void esperarColocacion();
    
    /**
     * Maneja el evento cuando la partida está completamente preparada.
     * Se ejecuta cuando ambos jugadores han terminado de colocar sus barcos
     * y la fase de combate puede comenzar.
     */
    void partidaReady();
    
    /**
     * Maneja los mensajes de error recibidos del servidor.
     * Se ejecuta cuando ocurre algún problema durante la comunicación
     * o el procesamiento de comandos en el servidor.
     * 
     * @param error Descripción del error ocurrido
     */
    void error(String error);
    
    /**
     * Maneja los ataques recibidos del jugador rival.
     * Se ejecuta cuando el rival ataca una casilla del tablero del jugador
     * y se debe actualizar la visualización correspondiente.
     * 
     * @param mensaje Mensaje con los detalles del ataque (coordenadas y resultado)
     */
    void ataqueRecibido(String mensaje);
    
    /**
     * Maneja mensajes que no son reconocidos por otros métodos específicos.
     * Se ejecuta como fallback para mensajes del servidor que no encajan
     * en las categorías predefinidas.
     * 
     * @param mensaje Mensaje completo recibido del servidor
     */
    void mensajeNoReconocido(String mensaje);
}