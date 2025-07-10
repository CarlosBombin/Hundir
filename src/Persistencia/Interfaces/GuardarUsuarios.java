package Persistencia.Interfaces;

/**
 * Interfaz especializada para el guardado de datos de usuarios.
 * Extiende GuardarDatos para operaciones específicas con objetos Usuario.
 * 
 * Las implementaciones de esta interfaz deben ser capaces de persistir usuarios
 * en diferentes destinos (archivos JSON, bases de datos, etc.) manteniendo
 * la integridad de los datos y la estructura requerida.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface GuardarUsuarios extends GuardarDatos{
    
    /**
     * Guarda datos de usuario en el destino configurado.
     * Especializa el método base para manejar específicamente objetos Usuario
     * o colecciones de usuarios.
     * 
     * @param datos Usuario o colección de usuarios a persistir
     */
    public void guardar(Object datos);
}
