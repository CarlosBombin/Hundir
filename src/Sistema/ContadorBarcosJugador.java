package Sistema;

public class ContadorBarcosJugador {
    
    private int portaviones = 0;
    private int submarinos = 0;
    private int destructores = 0;
    private int fragatas = 0;
    
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
    
    public boolean tieneTodasLasNaves() {
        return portaviones == 1 && 
               submarinos == 2 && 
               destructores == 3 && 
               fragatas == 4;
    }
    
    public String obtenerBarcosRestantes() {
        StringBuilder sb = new StringBuilder("Barcos restantes: ");
        
        if (portaviones < 1) sb.append("Portaviones(").append(1 - portaviones).append(") ");
        if (submarinos < 2) sb.append("Submarinos(").append(2 - submarinos).append(") ");
        if (destructores < 3) sb.append("Destructores(").append(3 - destructores).append(") ");
        if (fragatas < 4) sb.append("Fragatas(").append(4 - fragatas).append(") ");
        
        return sb.toString();
    }

    public int getPortaviones() { 
        return portaviones; 
    }

    public int getSubmarinos() { 
        return submarinos; 
    }

    public int getDestructores() { 
        return destructores; 
    }

    public int getFragatas() { 
        return fragatas; 
    }
}