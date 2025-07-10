package Tablero;
import Estados.DesconocidoAgua;
import Estados.DesconocidoBarco;
import Barcos.Barco;
import Estados.Estado;

/**
 * Representa una casilla individual del tablero de juego.
 * Cada casilla tiene coordenadas específicas, puede contener un barco
 * y mantiene un estado que refleja si ha sido atacada y el resultado.
 * 
 * Utiliza el patrón State para gestionar los diferentes estados
 * de la casilla durante el transcurso del juego.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Casilla {
    /** Coordenadas únicas que identifican la posición de la casilla */
    private final Coordenadas id;
    /** Barco que ocupa esta casilla (null si no hay barco) */
    private Barco barco;
    /** Estado actual de la casilla (agua, barco, tocado, hundido, etc.) */
    private Estado estado;
    
    /**
     * Constructor que crea una nueva casilla en las coordenadas especificadas.
     * Inicializa la casilla como agua desconocida sin barco.
     * 
     * @param id Coordenadas de la casilla en el tablero
     */
    public Casilla(Coordenadas id) {
        this.id = id;
        this.estado = DesconocidoAgua.getInstancia();
        this.barco = null;
    }
    
    /**
     * Obtiene el estado actual de la casilla.
     * El estado determina cómo se muestra la casilla y cómo responde a ataques.
     * 
     * @return Estado actual de la casilla
     */
    public Estado getEstado() {
        return this.estado;
    }
    
    /**
     * Representación textual de la casilla basada en sus coordenadas.
     * 
     * @return String con las coordenadas de la casilla
     */
    public String toString() {
        return this.id.toString();
    }
    
    /**
     * Aplica daño a la casilla, cambiando su estado según las reglas del juego.
     * Delega el cambio de estado al patrón State implementado en la clase Estado.
     */
    public void getDaño() {
        this.estado = this.estado.getDaño();
    }

    /**
     * Obtiene la fila de la casilla como entero.
     * 
     * @return Número de fila de la casilla
     */
    public int getFila() {
        return this.id.getAbcisasAsInt();
    }

    /**
     * Obtiene la columna de la casilla como entero.
     * 
     * @return Número de columna de la casilla
     */
    public int getColumna() {
        return this.id.getOrdenadas();
    }
    
    /**
     * Asigna un barco a esta casilla y actualiza su estado.
     * Cambia automáticamente el estado a DesconocidoBarco para indicar
     * que la casilla contiene un barco.
     * 
     * @param barco Barco a colocar en esta casilla
     */
    public void setBarco(Barco barco) {
        this.barco = barco;
        this.estado = DesconocidoBarco.getInstancia();
    }
    
    /**
     * Obtiene el barco que ocupa esta casilla.
     * 
     * @return Barco en la casilla o null si no hay ninguno
     */
    public Barco getBarco() {
        return this.barco;
    }
    
    /**
     * Verifica si la casilla contiene un barco.
     * 
     * @return true si hay un barco en la casilla, false en caso contrario
     */
    public boolean tieneBarco() {
        return this.barco != null;
    }
}
