package Tablero;
import Estados.DesconocidoAgua;
import Estados.DesconocidoBarco;
import Barcos.Barco;
import Estados.Estado;

public class Casilla {
    private final Coordenadas id;
    private Barco barco;
    private Estado estado;
    
    public Casilla(Coordenadas id) {
        this.id = id;
        this.estado = DesconocidoAgua.getInstancia();
        this.barco = null;
    }
    
    public Estado getEstado() {
        return this.estado;
    }
    
    public String toString() {
        return this.id.toString();
    }
    
    public void getDaño() {
        this.estado = this.estado.getDaño();
    }

    public int getFila() {
        return this.id.getAbcisasAsInt();
    }

    public int getColumna() {
        return this.id.getOrdenadas();
    }
    
    public void setBarco(Barco barco) {
        this.barco = barco;
        this.estado = DesconocidoBarco.getInstancia();
    }
    
    public Barco getBarco() {
        return this.barco;
    }
    
    public boolean tieneBarco() {
        return this.barco != null;
    }
}
