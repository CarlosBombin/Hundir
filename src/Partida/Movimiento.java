package Partida;

import Cliente.Usuario;
import Tablero.Casilla;

public class Movimiento {
    private Usuario usuario;
    private Casilla casilla;

    public Movimiento (Usuario usuario, Casilla casilla) {
        this.usuario = usuario;
        this.casilla = casilla;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public Casilla getCasilla() {
        return this.casilla;
    }
}
