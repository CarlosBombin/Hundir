package GUI;

public class ColocacionBarco {
    private final String tipoBarco;
    private final int fila;
    private final int columna;
    private final String orientacion;
    
    public ColocacionBarco(String tipoBarco, int fila, int columna, String orientacion) {
        this.tipoBarco = tipoBarco;
        this.fila = fila;
        this.columna = columna;
        this.orientacion = orientacion;
    }
    
    public String getTipoBarco() { return tipoBarco; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    public String getOrientacion() { return orientacion; }
    
    @Override
    public String toString() {
        return String.format("%s en (%d,%d) %s", tipoBarco, fila, columna, orientacion);
    }
}