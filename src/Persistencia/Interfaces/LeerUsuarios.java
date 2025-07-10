package Persistencia.Interfaces;

import java.util.List;
import Cliente.Usuario;

/**
 * Interfaz especializada para la lectura de datos de usuarios.
 * Extiende LeerDatos especificando que el tipo de retorno son objetos Usuario.
 * 
 * Las implementaciones de esta interfaz deben ser capaces de leer usuarios
 * desde diferentes fuentes (archivos JSON, bases de datos, etc.) y
 * convertirlos a objetos Usuario del dominio.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface LeerUsuarios extends LeerDatos{
    
    /**
     * Lee una lista de usuarios desde la fuente de datos configurada.
     * Especializa el método base para retornar específicamente objetos Usuario.
     * 
     * @return Lista de usuarios leídos desde la fuente de datos
     */
    public List<Usuario> leer();
}
