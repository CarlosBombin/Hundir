package Cliente;

/**
 * Clase que representa un usuario del juego Hundir la Flota.
 * Almacena las credenciales y estado del usuario para autenticación
 * y gestión de sesiones.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Usuario {
    
    /** Nombre de usuario */
    private String name;
    /** Contraseña del usuario */
    private String password;
    /** Indica si es un usuario nuevo (registro) o existente (login) */
    private boolean esNuevo;
    
    /**
     * Constructor que crea un usuario con nombre y contraseña.
     * Por defecto, el usuario se considera existente (no nuevo).
     * 
     * @param name Nombre del usuario
     * @param password Contraseña del usuario
     */
    public Usuario(String name, String password) {
        this.name = name;
        this.password = password;
        this.esNuevo = false;
    }
    
    /**
     * Obtiene el nombre del usuario.
     * 
     * @return Nombre del usuario
     */
    public String getName() {
        return name;
    }
    
    /**
     * Establece el nombre del usuario.
     * 
     * @param name Nuevo nombre del usuario
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Obtiene la contraseña del usuario.
     * 
     * @return Contraseña del usuario
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Establece la contraseña del usuario.
     * 
     * @param password Nueva contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Verifica si el usuario es nuevo (para registro).
     * 
     * @return true si es un usuario nuevo, false si es existente
     */
    public boolean esNuevo() {
        return esNuevo;
    }
    
    /**
     * Establece si el usuario es nuevo o existente.
     * 
     * @param esNuevo true para usuario nuevo (registro), false para existente (login)
     */
    public void setNuevo(boolean esNuevo) {
        this.esNuevo = esNuevo;
    }
    
    /**
     * Representación textual del usuario.
     * 
     * @return Nombre del usuario
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Compara dos usuarios por igualdad.
     * Dos usuarios son iguales si tienen el mismo nombre.
     * 
     * @param obj Objeto a comparar
     * @return true si los usuarios son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Usuario usuario = (Usuario) obj;
        return name != null ? name.equals(usuario.name) : usuario.name == null;
    }
    
    /**
     * Calcula el código hash del usuario.
     * Se basa en el nombre del usuario para mantener consistencia con equals().
     * 
     * @return Código hash del usuario
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
