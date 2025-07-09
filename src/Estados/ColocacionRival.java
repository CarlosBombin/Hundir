package Estados;

import Cliente.Usuario;

public class ColocacionRival implements EstadoPartida {
    private static EstadoPartida instancia;
    
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new ColocacionRival();
        }
        return instancia;
    }
    
    @Override
    public boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal) {
        return !esPrincipal;
    }
    
    @Override
    public EstadoPartida finalizarColocacion(boolean esPrincipal) {
        if (!esPrincipal) {
            return PartidaLista.getInstancia();
        }
        return this;
    }
    
    @Override
    public String obtenerDescripcion(boolean esPrincipal) {
        if (!esPrincipal) {
            return "turno_rival";
        } else {
            return "esperar";
        }
    }
    
    @Override
    public boolean partidaCompleta() {
        return false;
    }
}
