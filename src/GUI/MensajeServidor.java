package GUI;

/**
 * Procesador de mensajes del servidor que implementa el patrón Strategy.
 * Analiza los mensajes recibidos del servidor y los distribuye al manejador
 * correspondiente según su tipo y contenido.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class MensajeServidor {
    
    /**
     * Procesa un mensaje del servidor y lo distribuye al manejador apropiado.
     * Analiza el prefijo del mensaje para determinar el tipo y ejecuta
     * el método correspondiente del manejador.
     * 
     * @param mensaje Mensaje completo recibido del servidor
     * @param manejador Manejador que procesará el mensaje específico
     */
    public static void procesar(String mensaje, ManejadorMensajes manejador) {
        if (mensaje.startsWith("rival_encontrado:")) {
            manejador.rivalEncontrado(mensaje.substring(17));
            
        } else if (mensaje.startsWith("partida_lista:")) {
            manejador.partidaLista(mensaje.substring(15));
            
        } else if (mensaje.startsWith("turno_colocacion:")) {
            manejador.turnoColocacion();
            
        } else if (mensaje.startsWith("esperar_colocacion:")) {
            manejador.esperarColocacion();
            
        } else if (mensaje.startsWith("iniciar_colocacion:")) {
            // Mensaje para iniciar colocación - procesado internamente
            
        } else if (mensaje.startsWith("instrucciones:")) {
            // Mensaje con instrucciones - procesado internamente
            
        } else if (mensaje.startsWith("partida_ready:")) {
            manejador.partidaReady();
            
        } else if (mensaje.startsWith("error")) {
            manejador.error(mensaje.substring(6));
            
        } else if (mensaje.startsWith("ataque_recibido:")) {
            manejador.ataqueRecibido(mensaje);
            
        } else {
            manejador.mensajeNoReconocido(mensaje);
        }
    }
}