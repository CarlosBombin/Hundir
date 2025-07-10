package Partida;

import java.util.ArrayList;
import java.util.List;
import Cliente.Usuario;
import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

/**
 * Clase que representa una partida completa del juego Hundir la Flota.
 * Gestiona el estado de la partida entre dos usuarios, incluyendo sus tableros,
 * movimientos realizados, turno actual y resultado final.
 * Coordina toda la lógica de juego entre los dos jugadores participantes.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Partida {
    
    /** Usuario principal de la partida (jugador que creó la partida) */
    private Usuario usuarioPrincipal;
    /** Usuario rival (jugador que se unió a la partida) */
    private Usuario usuarioRival;
    /** Tablero del usuario principal con sus barcos */
    private Tablero tableroPrincipal;
    /** Tablero del usuario rival con sus barcos */
    private Tablero tableroRival;
    /** Lista de todos los movimientos realizados en la partida */
    private List<Movimiento> movimientos;
    /** Usuario ganador de la partida (null si no ha terminado) */
    private Usuario ganador;
    /** Usuario perdedor de la partida (null si no ha terminado) */
    private Usuario perdedor;
    /** Usuario que tiene el turno actual para atacar */
    private Usuario turnoActual;

    /**
     * Constructor que crea una nueva partida entre dos usuarios.
     * Inicializa los tableros vacíos para ambos jugadores y la lista de movimientos.
     * 
     * @param usuarioPrincipal Usuario que creó la partida
     * @param usuarioRival Usuario que se unió a la partida
     */
    public Partida(Usuario usuarioPrincipal, Usuario usuarioRival) {
        this.usuarioPrincipal = usuarioPrincipal;
        this.usuarioRival = usuarioRival;
        this.tableroPrincipal = new Tablero();
        this.tableroPrincipal.RellenaTablero();
        this.tableroRival = new Tablero();
        this.tableroRival.RellenaTablero();
        this.movimientos = new ArrayList<Movimiento>();
    }

    /**
     * Registra un movimiento de ataque de un usuario en las coordenadas especificadas.
     * Aplica el daño al tablero correspondiente y almacena el movimiento en el historial.
     * 
     * @param usuario Usuario que realiza el ataque
     * @param id Coordenadas del ataque
     * @throws IllegalArgumentException Si el usuario no participa en esta partida
     */
    public void addMovimiento(Usuario usuario, Coordenadas id) {
        if (usuario.equals(this.usuarioPrincipal)) {
            Casilla casilla = this.tableroPrincipal.getCasilla(id);
            this.tableroPrincipal.getDaño(id);
            this.movimientos.add(new Movimiento(usuario, casilla));
        } else if (usuario.equals(this.usuarioRival)) {
            Casilla casilla = this.tableroRival.getCasilla(id);
            this.tableroRival.getDaño(id);
            this.movimientos.add(new Movimiento(usuario, casilla));
        } else {
            throw new IllegalArgumentException("Ese usuario no se encuentra en la partida.");
        }
    }

    /**
     * Obtiene el usuario principal de la partida.
     * 
     * @return Usuario principal (creador de la partida)
     */
    public Usuario getUsuarioPrincipal() {
        return this.usuarioPrincipal;
    }

    /**
     * Obtiene el usuario rival de la partida.
     * 
     * @return Usuario rival (se unió a la partida)
     */
    public Usuario getUsuarioRival() {
        return this.usuarioRival;
    }

    /**
     * Obtiene la lista completa de movimientos realizados en la partida.
     * Útil para mostrar el historial de ataques o analizar la partida.
     * 
     * @return Lista inmutable de movimientos ordenados cronológicamente
     */
    public List<Movimiento> getMovimientos() {
        return this.movimientos;
    }

    /**
     * Representación textual de la partida mostrando los nombres de los jugadores.
     * Formato: "NombreUsuarioPrincipal vs NombreUsuarioRival"
     * 
     * @return String descriptivo de la partida
     */
    @Override
    public String toString() {
        return this.usuarioPrincipal.getName() + "vs" + this.usuarioRival.getName();
    }

    /**
     * Establece al usuario principal como ganador de la partida.
     * Automáticamente marca al usuario rival como perdedor.
     */
    public void setGanadorPrincipal() {
        setGanador(this.usuarioPrincipal);
        setPerdedorRival();
    }

    /**
     * Establece al usuario rival como ganador de la partida.
     * Automáticamente marca al usuario principal como perdedor.
     */
    public void setGanadorRival() {
        setGanador(this.usuarioRival);
        setPerdedorPrincipal();
    }

    /**
     * Establece un usuario específico como ganador de la partida.
     * 
     * @param usuario Usuario que ganó la partida
     */
    public void setGanador(Usuario usuario) {
        this.ganador = usuario;
    }

    /**
     * Establece al usuario principal como perdedor de la partida.
     */
    public void setPerdedorPrincipal() {
        setPerdedor(this.usuarioPrincipal);
    }

    /**
     * Establece al usuario rival como perdedor de la partida.
     */
    public void setPerdedorRival() {
        setPerdedor(this.usuarioRival);
    }

    /**
     * Establece un usuario específico como perdedor de la partida.
     * 
     * @param usuario Usuario que perdió la partida
     */
    public void setPerdedor(Usuario usuario) {
        this.perdedor = usuario;
    }

    /**
     * Obtiene el usuario ganador de la partida.
     * 
     * @return Usuario ganador o null si la partida no ha terminado
     */
    public Usuario getGanador() {
        return this.ganador;
    }

    /**
     * Obtiene el usuario perdedor de la partida.
     * 
     * @return Usuario perdedor o null si la partida no ha terminado
     */
    public Usuario getPerdedor() {
        return this.perdedor;
    }

    /**
     * Obtiene el tablero del usuario principal.
     * 
     * @return Tablero con los barcos del usuario principal
     */
    public Tablero getTableroPrincipal() {
        return this.tableroPrincipal;
    }

    /**
     * Obtiene el tablero del usuario rival.
     * 
     * @return Tablero con los barcos del usuario rival
     */
    public Tablero getTableroRival() {
        return this.tableroRival;
    }

    /**
     * Obtiene el tablero correspondiente a un usuario específico.
     * Útil para operaciones genéricas sin importar cuál es el jugador.
     * 
     * @param usuario Usuario del cual se quiere obtener el tablero
     * @return Tablero del usuario especificado o null si no participa en la partida
     */
    public Tablero getTableroJugador(Usuario usuario) {
        if (usuario.equals(this.usuarioPrincipal)) {
            return this.tableroPrincipal;
        } else if (usuario.equals(this.usuarioRival)) {
            return this.tableroRival;
        } else {
            return null;
        }
    }

    /**
     * Inicializa el sistema de turnos estableciendo al usuario principal como primero.
     * Debe llamarse al comenzar la fase de combate de la partida.
     */
    public void inicializarTurno() {
        this.turnoActual = usuarioPrincipal;
    }

    /**
     * Obtiene el usuario que tiene el turno actual para atacar.
     * 
     * @return Usuario con el turno actual o null si no se han inicializado los turnos
     */
    public Usuario getTurnoActual() {
        return turnoActual;
    }

    /**
     * Cambia el turno al otro jugador.
     * Alterna entre el usuario principal y el rival automáticamente.
     * Si es turno del principal, cambia al rival y viceversa.
     */
    public void cambiarTurno() {
        if (turnoActual.equals(usuarioPrincipal)) {
            turnoActual = usuarioRival;
        } else {
            turnoActual = usuarioPrincipal;
        }
    }

    /**
     * Verifica si la partida ha terminado.
     * Una partida termina cuando uno de los jugadores ha perdido todos sus barcos.
     * 
     * @return true si la partida ha terminado, false en caso contrario
     */
    public boolean haTerminado() {
        return ganador != null && perdedor != null;
    }

    /**
     * Obtiene el rival de un usuario específico en esta partida.
     * 
     * @param usuario Usuario del cual se quiere obtener el rival
     * @return Usuario rival o null si el usuario no participa en la partida
     */
    public Usuario getRival(Usuario usuario) {
        if (usuario.equals(this.usuarioPrincipal)) {
            return this.usuarioRival;
        } else if (usuario.equals(this.usuarioRival)) {
            return this.usuarioPrincipal;
        } else {
            return null;
        }
    }

    /**
     * Verifica si un usuario específico participa en esta partida.
     * 
     * @param usuario Usuario a verificar
     * @return true si el usuario participa en la partida, false en caso contrario
     */
    public boolean participaUsuario(Usuario usuario) {
        return usuario.equals(this.usuarioPrincipal) || usuario.equals(this.usuarioRival);
    }

    /**
     * Obtiene el número total de movimientos realizados en la partida.
     * 
     * @return Cantidad de ataques realizados por ambos jugadores
     */
    public int getNumeroMovimientos() {
        return this.movimientos.size();
    }

    /**
     * Obtiene los movimientos realizados por un usuario específico.
     * 
     * @param usuario Usuario del cual se quieren obtener los movimientos
     * @return Lista de movimientos del usuario especificado
     */
    public List<Movimiento> getMovimientosUsuario(Usuario usuario) {
        List<Movimiento> movimientosUsuario = new ArrayList<>();
        for (Movimiento movimiento : this.movimientos) {
            if (movimiento.getUsuario().equals(usuario)) {
                movimientosUsuario.add(movimiento);
            }
        }
        return movimientosUsuario;
    }

    /**
     * Obtiene estadísticas básicas de la partida.
     * 
     * @return String con información resumida de la partida
     */
    public String getEstadisticas() {
        StringBuilder stats = new StringBuilder();
        stats.append("Partida: ").append(toString()).append("\n");
        stats.append("Movimientos totales: ").append(getNumeroMovimientos()).append("\n");
        stats.append("Estado: ");
        
        if (haTerminado()) {
            stats.append("Terminada - Ganador: ").append(ganador.getName());
        } else {
            stats.append("En curso - Turno: ").append(
                turnoActual != null ? turnoActual.getName() : "No iniciado"
            );
        }
        
        return stats.toString();
    }
}
