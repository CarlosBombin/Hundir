package Partida;

import Cliente.Usuario;
import Tablero.Casilla;

/**
 * Clase que representa un movimiento individual realizado por un usuario en la partida.
 * Encapsula la información de quién realizó el ataque y qué casilla fue atacada,
 * permitiendo mantener un historial completo de todos los movimientos del juego.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Movimiento {
    
    /** Usuario que realizó este movimiento */
    private Usuario usuario;
    /** Casilla que fue atacada en este movimiento */
    private Casilla casilla;

    /**
     * Constructor que crea un nuevo movimiento con el usuario y casilla especificados.
     * 
     * @param usuario Usuario que realiza el ataque
     * @param casilla Casilla del tablero que es atacada
     */
    public Movimiento(Usuario usuario, Casilla casilla) {
        this.usuario = usuario;
        this.casilla = casilla;
    }

    /**
     * Obtiene el usuario que realizó este movimiento.
     * 
     * @return Usuario que ejecutó el ataque
     */
    public Usuario getUsuario() {
        return this.usuario;
    }

    /**
     * Obtiene la casilla que fue atacada en este movimiento.
     * 
     * @return Casilla objetivo del ataque
     */
    public Casilla getCasilla() {
        return this.casilla;
    }
}
