package Tablero;

import Sistema.Transformer;

/**
 * Representa las coordenadas de una casilla en el tablero de juego.
 * Utiliza un sistema de coordenadas alfanumérico donde las columnas
 * se representan con letras (A-H) y las filas con números (0-7).
 * 
 * Proporciona métodos para conversión entre diferentes formatos
 * de coordenadas y validación de posiciones válidas.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Coordenadas {
    /** Letra que representa la columna (A-H) */
    private final char abcisas;
    /** Número que representa la fila (0-7) */
    private final int ordenadas;
    
    /**
     * Constructor que crea coordenadas con letra y número.
     * 
     * @param abcisas Letra de la columna (A-H)
     * @param ordenadas Número de la fila (0-7)
     */
    public Coordenadas(char abcisas, int ordenadas) {
        this.abcisas = abcisas;
        this.ordenadas = ordenadas;
    }
    
    /**
     * Constructor que crea coordenadas a partir de índices numéricos.
     * Convierte automáticamente el índice de columna a letra.
     * 
     * @param columna Índice de columna (0-7)
     * @param fila Índice de fila (0-7)
     */
    public Coordenadas(int columna, int fila) {
        this.abcisas = Transformer.numToLetter(columna);
        this.ordenadas = fila;
    }
    
    /**
     * Obtiene la letra de la columna.
     * 
     * @return Letra que representa la columna
     */
    public char getAbcisas() {
        return this.abcisas;
    }
    
    /**
     * Obtiene el número de la fila.
     * 
     * @return Número que representa la fila
     */
    public int getOrdenadas() {
        return this.ordenadas;
    }
    
    /**
     * Convierte la letra de columna a su índice numérico.
     * A=0, B=1, C=2, etc.
     * 
     * @return Índice numérico de la columna
     */
    public int getAbcisasAsInt() {
        return Transformer.letterToNum(this.abcisas);
    }
    
    /**
     * Representación textual de las coordenadas.
     * Formato: "LetraNumero" (ejemplo: "A0", "B3", "H7")
     * 
     * @return String con las coordenadas en formato alfanumérico
     */
    @Override
    public String toString() {
        return String.valueOf(abcisas) + ordenadas;
    }
}

    
    
    
    

