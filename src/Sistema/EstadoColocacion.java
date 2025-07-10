package Sistema;

import Estados.EstadoPartida;
import Estados.ColocacionPrincipal;
import Cliente.Usuario;

public class EstadoColocacion {
    
    private EstadoPartida estadoActual;
    
    private ContadorBarcosJugador contadorPrincipal;
    private ContadorBarcosJugador contadorRival;
    
    public EstadoColocacion() {
        this.estadoActual = ColocacionPrincipal.getInstancia();
        this.contadorPrincipal = new ContadorBarcosJugador();
        this.contadorRival = new ContadorBarcosJugador();
    }
    
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
    
    public String getEstadoActual() {
        return estadoActual.getClass().getSimpleName();
    }
}
