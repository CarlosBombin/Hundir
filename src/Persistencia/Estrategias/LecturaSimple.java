package Persistencia.Estrategias;

import Persistencia.Interfaces.LeerPartidas;
import Persistencia.Interfaces.LeerUsuarios;
import Persistencia.Partidas.LeerPartidasJson;
import Persistencia.Usuarios.LeerUsuariosJson;

/**
 * Implementación concreta de la estrategia de lectura simple.
 * Utiliza archivos JSON para leer usuarios, usuarios activos y partidas.
 * Implementa el patrón Singleton para garantizar una única instancia del sistema
 * de lectura y el patrón Strategy para permitir intercambio de estrategias.
 * 
 * Esta estrategia utiliza:
 * - Archivos JSON para cargar usuarios registrados
 * - Archivos JSON para cargar usuarios actualmente conectados
 * - Lectura JSON para recuperar historial de partidas
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class LecturaSimple implements EstrategiaLeer {
    /** Instancia única del singleton */
    private static LecturaSimple instancia;
    /** Lector para usuarios registrados en el sistema */
    private LeerUsuarios leerUsuarios;
    /** Lector para usuarios actualmente conectados */
    private LeerUsuarios leerUsuariosActivos;
    /** Lector para partidas almacenadas */
    private LeerPartidas leerPartidas;

    /**
     * Obtiene la instancia única de la lectura simple.
     * Implementa el patrón Singleton de forma lazy (creación bajo demanda).
     * 
     * @return Instancia única de LecturaSimple
     */
    public static LecturaSimple getInstancia() {
        if (instancia == null) {
            instancia = new LecturaSimple();
        }
        return instancia;
    }

    /**
     * Constructor por defecto que inicializa los lectores con configuración estándar.
     * Configura lectura desde archivos JSON con nombres predeterminados:
     * - usuarios.json para usuarios registrados
     * - usuariosActivos.json para usuarios conectados
     * - Configuración por defecto para partidas
     */
    public LecturaSimple() {
        this.leerUsuarios = new LeerUsuariosJson("usuarios.json");
        this.leerUsuariosActivos = new LeerUsuariosJson("usuariosActivos.json");
        this.leerPartidas = new LeerPartidasJson();
    }

    /**
     * Constructor con inyección de dependencias para testing y configuración personalizada.
     * Permite inyectar implementaciones específicas de los lectores.
     * 
     * @param leerUsuarios Implementación para leer usuarios registrados
     * @param leerUsuariosActivos Implementación para leer usuarios activos
     * @param leerPartidas Implementación para leer partidas
     */
    public LecturaSimple(LeerUsuarios leerUsuarios, LeerUsuarios leerUsuariosActivos, LeerPartidas leerPartidas) {
        this.leerUsuarios = leerUsuarios;
        this.leerUsuariosActivos = leerUsuariosActivos;
        this.leerPartidas = leerPartidas;
    }

    /**
     * Obtiene el lector de usuarios registrados del sistema.
     * 
     * @return Implementación de LeerUsuarios para usuarios registrados
     */
    public LeerUsuarios getUsuarios() {
        return this.leerUsuarios;
    }

    /**
     * Obtiene el lector de usuarios actualmente conectados.
     * 
     * @return Implementación de LeerUsuarios para usuarios activos
     */
    public LeerUsuarios getUsuariosActivos() {
        return this.leerUsuariosActivos;
    }

    /**
     * Obtiene el lector de partidas del sistema.
     * 
     * @return Implementación de LeerPartidas para cargar partidas
     */
    public LeerPartidas getPartidas() {
        return this.leerPartidas;
    }
}
