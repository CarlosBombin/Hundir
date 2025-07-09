package Estados;

import Cliente.Usuario;

public class PartidaLista implements EstadoPartida {
    private static EstadoPartida instancia;
    
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new PartidaLista();
        }
        return instancia;
    }
    
    @Override
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return false;
    }
    
    @Override
    public EstadoPartida finalizarColocacion(boolean esPrincipal) {
        return this;
    }
    
    @Override
    public String obtenerDescripcion(boolean esPrincipal) {
        return "completo";
    }
    
    @Override
    public boolean partidaCompleta() {
        return true;
    }
}
