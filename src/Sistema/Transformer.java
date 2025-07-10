package Sistema;

/**
 * Clase utilitaria para transformaciones entre números y letras.
 * Proporciona métodos para convertir índices numéricos a letras del alfabeto
 * y viceversa, útil para el manejo de coordenadas del tablero de juego.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Transformer {
    /** Array con las letras del alfabeto para las transformaciones */
    private static String letras[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    
    /**
     * Convierte un número en su letra correspondiente del alfabeto.
     * El índice 0 corresponde a 'A', 1 a 'B', etc.
     * 
     * @param i Índice numérico (0-25)
     * @return Letra correspondiente al índice o '?' si está fuera de rango
     */
    public static char numToLetter(int i) {
        if (i >= 0 && i <= 25) {
            return letras[i].charAt(0);
        } else {
            return '?';
        }
    }
    
    /**
     * Convierte una letra en su índice numérico correspondiente.
     * 'A' corresponde a 0, 'B' a 1, etc.
     * 
     * @param a Letra del alfabeto a convertir
     * @return Índice numérico correspondiente o 999 si no se encuentra
     */
    public static int letterToNum(char a) {
        for (int i = 0; i <= 25; i++) {
            if (String.valueOf(a).equals(letras[i])) {
                return i;
            }
        }
        return 999;
    }
}
