package Estados;

/**
 * Interfaz que define el comportamiento de los estados de las casillas del tablero.
 * Implementa el patrón State para gestionar las transiciones de estado cuando
 * una casilla recibe daño en el juego Hundir la Flota.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface Estado {
    
    /**
     * Define la transición de estado cuando la casilla recibe daño.
     * Cada implementación específica determina a qué estado debe cambiar.
     * 
     * @return El nuevo estado después de recibir daño
     */
    public Estado getDaño();
}
