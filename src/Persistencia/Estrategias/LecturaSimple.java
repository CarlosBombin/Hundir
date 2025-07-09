package Persistencia.Estrategias;

import Persistencia.Interfaces.LeerPartidas;
import Persistencia.Interfaces.LeerUsuarios;
import Persistencia.Partidas.LeerPartidasJson;
import Persistencia.Usuarios.LeerUsuariosJson;

public class LecturaSimple implements EstrategiaLeer {
	private static LecturaSimple instancia;
	private LeerUsuarios leerUsuarios;
	private LeerUsuarios leerUsuariosActivos;
	private LeerPartidas leerPartidas;

	public static LecturaSimple getInstancia() {
		if (instancia == null) {
			instancia = new LecturaSimple();
		}
		return instancia;
	}

	public LecturaSimple() {
		this.leerUsuarios = new LeerUsuariosJson("usuarios.json");
		this.leerUsuariosActivos = new LeerUsuariosJson("usuariosActivos.json");
		this.leerPartidas = new LeerPartidasJson();
	}

	public LecturaSimple(LeerUsuarios leerUsuarios, LeerUsuarios leerUsuariosActivos, LeerPartidas leerPartidas) {
		this.leerUsuarios = leerUsuarios;
		this.leerUsuariosActivos = leerUsuariosActivos;
		this.leerPartidas = leerPartidas;
	}

	public LeerUsuarios getUsuarios() {
		return this.leerUsuarios;
	}

	public LeerUsuarios getUsuariosActivos() {
		return this.leerUsuariosActivos;
	}

	public LeerPartidas getPartidas() {
		return this.leerPartidas;
	}
}
