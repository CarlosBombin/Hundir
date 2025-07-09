package Estados;

import Cliente.Usuario;

public class EsperandoRival implements EstadoPartida {
    private static EstadoPartida instancia;
    
    public static EstadoPartida getInstancia() {
        if (instancia == null) {
            instancia = new EsperandoRival();
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
        return "esperar_rival";
    }
    
    @Override
    public boolean partidaCompleta() {
        return false;
    }
}
