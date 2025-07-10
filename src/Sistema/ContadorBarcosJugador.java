package Sistema;

/**
 * Contador que gestiona la cantidad de barcos colocados por un jugador.
 * Controla que cada jugador coloque exactamente la cantidad permitida
 * de cada tipo de barco según las reglas del juego Hundir la Flota.
 * 
 * Cantidades permitidas por tipo:
 * - Portaviones: 1
 * - Submarinos: 2
 * - Destructores: 3
 * - Fragatas: 4
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ContadorBarcosJugador {
    
    /** Cantidad de portaviones colocados (máximo 1) */
    private int portaviones = 0;
    /** Cantidad de submarinos colocados (máximo 2) */
    private int submarinos = 0;
    /** Cantidad de destructores colocados (máximo 3) */
    private int destructores = 0;
    /** Cantidad de fragatas colocadas (máximo 4) */
    private int fragatas = 0;
    
    /**
     * Verifica si el jugador puede colocar un barco del tipo especificado.
     * Comprueba que no se haya alcanzado el límite máximo para ese tipo.
     * 
     * @param tipo Tipo de barco que se quiere colocar
     * @return true si se puede colocar el barco, false si se alcanzó el límite
     */
    public boolean puedeColocarBarco(TipoBarco tipo) {
        switch (tipo) {
            case PORTAVIONES:
                return portaviones < 1;
            case SUBMARINO:
                return submarinos < 2;
            case DESTRUCTOR:
                return destructores < 3;
            case FRAGATA:
                return fragatas < 4;
            default:
                return false;
        }
    }
    
    /**
     * Registra la colocación de un barco del tipo especificado.
     * Incrementa el contador correspondiente al tipo de barco.
     * 
     * @param tipo Tipo de barco que se ha colocado
     */
    public void colocarBarco(TipoBarco tipo) {
        switch (tipo) {
            case PORTAVIONES:
                portaviones++;
                break;
            case SUBMARINO:
                submarinos++;
                break;
            case DESTRUCTOR:
                destructores++;
                break;
            case FRAGATA:
                fragatas++;
                break;
        }
    }
    
    /**
     * Verifica si el jugador ha colocado todos los barcos requeridos.
     * Un jugador tiene todas las naves cuando ha colocado exactamente:
     * 1 portaviones, 2 submarinos, 3 destructores y 4 fragatas.
     * 
     * @return true si se han colocado todos los barcos requeridos
     */
    public boolean tieneTodasLasNaves() {
        return portaviones == 1 && 
               submarinos == 2 && 
               destructores == 3 && 
               fragatas == 4;
    }
    
    /**
     * Obtiene un mensaje descriptivo de los barcos que aún faltan por colocar.
     * Solo muestra los tipos de barco que no han alcanzado su cantidad máxima.
     * 
     * @return String con los barcos restantes por colocar
     */
    public String obtenerBarcosRestantes() {
        StringBuilder sb = new StringBuilder("Barcos restantes: ");
        
        if (portaviones < 1) sb.append("Portaviones(").append(1 - portaviones).append(") ");
        if (submarinos < 2) sb.append("Submarinos(").append(2 - submarinos).append(") ");
        if (destructores < 3) sb.append("Destructores(").append(3 - destructores).append(") ");
        if (fragatas < 4) sb.append("Fragatas(").append(4 - fragatas).append(") ");
        
        return sb.toString();
    }

    /**
     * Obtiene la cantidad de portaviones colocados.
     * 
     * @return Número de portaviones colocados
     */
    public int getPortaviones() { 
        return portaviones; 
    }

    /**
     * Obtiene la cantidad de submarinos colocados.
     * 
     * @return Número de submarinos colocados
     */
    public int getSubmarinos() { 
        return submarinos; 
    }

    /**
     * Obtiene la cantidad de destructores colocados.
     * 
     * @return Número de destructores colocados
     */
    public int getDestructores() { 
        return destructores; 
    }

    /**
     * Obtiene la cantidad de fragatas colocadas.
     * 
     * @return Número de fragatas colocadas
     */
    public int getFragatas() { 
        return fragatas; 
    }
}