package Sistema;

import Estados.EstadoPartida;
import Estados.ColocacionPrincipal;
import Cliente.Usuario;

public class EstadoColocacion {
    
    // Estado actual usando el patrón State
    private EstadoPartida estadoActual;
    
    // Contadores por jugador
    private ContadorBarcosJugador contadorPrincipal;
    private ContadorBarcosJugador contadorRival;
    
    public EstadoColocacion() {
        this.estadoActual = ColocacionPrincipal.getInstancia(); // Siempre empieza el principal
        this.contadorPrincipal = new ContadorBarcosJugador();
        this.contadorRival = new ContadorBarcosJugador();
    }
    
    // Métodos usando el patrón State
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return estadoActual.puedeColocarBarco(usuario, esPrincipal);
    }
    
    public void finalizarColocacion(boolean esPrincipal) {
        this.estadoActual = estadoActual.finalizarColocacion(esPrincipal);
    }
    
    public String obtenerDescripcion(boolean esPrincipal) {
        return estadoActual.obtenerDescripcion(esPrincipal);
    }
    
    public boolean partidaCompleta() {
        return estadoActual.partidaCompleta();
    }
    
    // Getters para contadores
    public ContadorBarcosJugador getContadorPrincipal() {
        return contadorPrincipal;
    }
    
    public ContadorBarcosJugador getContadorRival() {
        return contadorRival;
    }
    
    public ContadorBarcosJugador getContador(boolean esPrincipal) {
        if (esPrincipal) {
            return contadorPrincipal;
        } else {
            return contadorRival;
        }
    }
    
    // Método para debug
    public String getEstadoActual() {
        return estadoActual.getClass().getSimpleName();
    }
}
