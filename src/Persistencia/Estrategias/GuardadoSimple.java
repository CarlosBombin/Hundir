package Persistencia.Estrategias;

import Persistencia.Interfaces.GuardarPartidas;
import Persistencia.Interfaces.GuardarUsuarios;
import Persistencia.Partidas.GuardarPartidasJson;
import Persistencia.Usuarios.GuardarUsuariosJson;

public class GuardadoSimple implements EstrategiaGuardar {
	private static GuardadoSimple instancia;
	private GuardarUsuarios guardarUsuarios;
	private GuardarUsuarios guardarUsuariosActivos;
	private GuardarPartidas guardarPartidas;

	public static GuardadoSimple getInstancia() {
		if (instancia == null) {
			instancia = new GuardadoSimple();
		}
		return instancia;
	}

	public GuardadoSimple() {
		this.guardarUsuarios = new GuardarUsuariosJson("usuarios.json");
		this.guardarUsuariosActivos = new GuardarUsuariosJson("usuariosActivos.json");
		this.guardarPartidas = new GuardarPartidasJson();
	}

	public GuardadoSimple(GuardarUsuarios guardarUsuarios, GuardarUsuarios guardarUsuariosActivos, GuardarPartidas guardarPartidas) {
		this.guardarUsuarios = guardarUsuarios;
		this.guardarUsuariosActivos = guardarUsuariosActivos;
		this.guardarPartidas = guardarPartidas;
	}

	public GuardarUsuarios setUsuarios() {
		return this.guardarUsuarios;
	}

	public GuardarUsuarios setUsuariosActivos() {
		return this.guardarUsuariosActivos;
	}

	public GuardarPartidas setPartidas() {
		return this.guardarPartidas;
	}
}
