package Persistencia.Estrategias;

import Persistencia.Interfaces.GuardarPartidas;
import Persistencia.Interfaces.GuardarUsuarios;
import Persistencia.Partidas.GuardarPartidasJson;
import Persistencia.Usuarios.GuardarUsuariosJson;

/**
 * Implementación concreta de la estrategia de guardado simple.
 * Utiliza archivos JSON para persistir usuarios, usuarios activos y partidas.
 * Implementa el patrón Singleton para garantizar una única instancia del sistema
 * de guardado y el patrón Strategy para permitir intercambio de estrategias.
 * 
 * Esta estrategia utiliza:
 * - Archivos JSON para usuarios registrados
 * - Archivos JSON para usuarios actualmente conectados
 * - Persistencia JSON para el historial de partidas
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class GuardadoSimple implements EstrategiaGuardar {
    /** Instancia única del singleton */
    private static GuardadoSimple instancia;
    /** Guardador para usuarios registrados en el sistema */
    private GuardarUsuarios guardarUsuarios;
    /** Guardador para usuarios actualmente conectados */
    private GuardarUsuarios guardarUsuariosActivos;
    /** Guardador para partidas completadas y en curso */
    private GuardarPartidas guardarPartidas;

    /**
     * Obtiene la instancia única del guardado simple.
     * Implementa el patrón Singleton de forma lazy (creación bajo demanda).
     * 
     * @return Instancia única de GuardadoSimple
     */
    public static GuardadoSimple getInstancia() {
        if (instancia == null) {
            instancia = new GuardadoSimple();
        }
        return instancia;
    }

    /**
     * Constructor por defecto que inicializa los guardadores con configuración estándar.
     * Configura guardado en archivos JSON con nombres predeterminados:
     * - usuarios.json para usuarios registrados
     * - usuariosActivos.json para usuarios conectados
     * - Configuración por defecto para partidas
     */
    public GuardadoSimple() {
        this.guardarUsuarios = new GuardarUsuariosJson("usuarios.json");
        this.guardarUsuariosActivos = new GuardarUsuariosJson("usuariosActivos.json");
        this.guardarPartidas = new GuardarPartidasJson();
    }

    /**
     * Constructor con inyección de dependencias para testing y configuración personalizada.
     * Permite inyectar implementaciones específicas de los guardadores.
     * 
     * @param guardarUsuarios Implementación para guardar usuarios registrados
     * @param guardarUsuariosActivos Implementación para guardar usuarios activos
     * @param guardarPartidas Implementación para guardar partidas
     */
    public GuardadoSimple(GuardarUsuarios guardarUsuarios, GuardarUsuarios guardarUsuariosActivos, GuardarPartidas guardarPartidas) {
        this.guardarUsuarios = guardarUsuarios;
        this.guardarUsuariosActivos = guardarUsuariosActivos;
        this.guardarPartidas = guardarPartidas;
    }

    /**
     * Obtiene el guardador de usuarios registrados del sistema.
     * 
     * @return Implementación de GuardarUsuarios para usuarios registrados
     */
    public GuardarUsuarios setUsuarios() {
        return this.guardarUsuarios;
    }

    /**
     * Obtiene el guardador de usuarios actualmente conectados.
     * 
     * @return Implementación de GuardarUsuarios para usuarios activos
     */
    public GuardarUsuarios setUsuariosActivos() {
        return this.guardarUsuariosActivos;
    }

    /**
     * Obtiene el guardador de partidas del sistema.
     * 
     * @return Implementación de GuardarPartidas para persistir partidas
     */
    public GuardarPartidas setPartidas() {
        return this.guardarPartidas;
    }
}
