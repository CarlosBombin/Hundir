package GUI;

public interface ManejadorMensajes {
    void rivalEncontrado(String rival);
    void partidaLista(String info);
    void turnoColocacion();
    void esperarColocacion();
    void partidaReady();
    void error(String error);
    void mensajeNoReconocido(String mensaje);
}