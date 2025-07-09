package Partida;

import java.util.ArrayList;
import java.util.List;
import Cliente.Usuario;
import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

public class Partida {
	private Usuario usuarioPrincipal;
	private Usuario usuarioRival;
	private Tablero tableroPrincipal;
	private Tablero tableroRival;
	private List<Movimiento> movimientos;
	private Usuario ganador;
	private Usuario perdedor;

	public Partida (Usuario usuarioPrincipal, Usuario usuarioRival) {
		this.usuarioPrincipal = usuarioPrincipal;
		this.usuarioRival = usuarioRival;
		this.tableroPrincipal = new Tablero();
		this.tableroPrincipal.RellenaTablero();
		this.tableroRival = new Tablero();
		this.tableroRival.RellenaTablero();
		this.movimientos = new ArrayList<Movimiento>();
	}

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

	public Usuario getUsuarioPrincipal() {
		return this.usuarioPrincipal;
	}

	public Usuario getUsuarioRival() {
		return this.usuarioRival;
	}

	public List<Movimiento> getMovimientos() {
		return this.movimientos;
	}

	@Override
	public String toString() {
		return this.usuarioPrincipal.getName() + "vs" + this.usuarioRival.getName();
	}

	public void setGanadorPrincipal() {
		setGanador(this.usuarioPrincipal);
		setPerdedorRival();
	}

	public void setGanadorRival() {
		setGanador(this.usuarioRival);
		setPerdedorPrincipal();
	}

	public void setGanador(Usuario usuario) {
		this.ganador = usuario;
	}

	public void setPerdedorPrincipal() {
		setPerdedor(this.usuarioPrincipal);
	}

	public void setPerdedorRival() {
		setPerdedor(this.usuarioRival);
	}

	public void setPerdedor(Usuario usuario) {
		this.perdedor = usuario;
	}

	public Usuario getGanador() {
		return this.ganador;
	}

	public Usuario getPerdedor() {
		return this.perdedor;
	}

	public Tablero getTableroPrincipal() {
		return this.tableroPrincipal;
	}

	public Tablero getTableroRival() {
		return this.tableroRival;
	}
}
