package Sistema;

public enum TipoBarco {
    PORTAVIONES("PORTAVIONES", 4, 1),
    SUBMARINO("SUBMARINO", 3, 2),
    DESTRUCTOR("DESTRUCTOR", 2, 3),
    FRAGATA("FRAGATA", 1, 4);
    
    private final String nombre;
    private final int tamaño;
    private final int cantidadMaxima;
    
    TipoBarco(String nombre, int tamaño, int cantidadMaxima) {
        this.nombre = nombre;
        this.tamaño = tamaño;
        this.cantidadMaxima = cantidadMaxima;
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public int getTamaño() { 
        return tamaño; 
    }
    
    public int getCantidadMaxima() { 
        return cantidadMaxima; 
    }
    
    public static TipoBarco fromString(String nombre) {
        if (nombre == null) {
            return null;
        }
        
        for (TipoBarco tipo : TipoBarco.values()) {
            if (tipo.nombre.equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        return null;
    }
}
