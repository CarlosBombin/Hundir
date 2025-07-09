package GUI;

public class MensajeServidor {
    
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
            
        } else if (mensaje.startsWith("instrucciones:")) {
            
        } else if (mensaje.startsWith("partida_ready:")) {
            manejador.partidaReady();
            
        } else if (mensaje.startsWith("error")) {
            manejador.error(mensaje.substring(6));
            
        } else {
            manejador.mensajeNoReconocido(mensaje);
        }
    }
}