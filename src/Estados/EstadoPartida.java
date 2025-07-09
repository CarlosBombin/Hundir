package Estados;

import Cliente.Usuario;

public interface EstadoPartida {
    boolean puedeColocarBarco(Usuario usuario, boolean esPrincipal);
    EstadoPartida finalizarColocacion(boolean esPrincipal);
    String obtenerDescripcion(boolean esPrincipal);
    boolean partidaCompleta();
}
