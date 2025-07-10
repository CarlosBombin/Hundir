package Tablero;
import Sistema.Transformer;

/**
 * Implementación simple de la estrategia de creación de tableros.
 * Crea un tablero estándar de 8x8 casillas con coordenadas alfanuméricas
 * (A-H para columnas, 0-7 para filas).
 * 
 * Implementa el patrón Singleton para garantizar una única instancia
 * y el patrón Strategy para permitir intercambio de estrategias.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class EstrategiaTableroSimple implements EstrategiaTablero {
    /** Instancia única del singleton */
    public static EstrategiaTablero instancia;
    
    /**
     * Crea un tablero de 8x8 casillas con coordenadas alfanuméricas.
     * Cada casilla se inicializa con sus coordenadas correspondientes
     * usando el sistema A-H para columnas y 0-7 para filas.
     * 
     * @return Matriz 8x8 de casillas inicializadas
     */
    @Override
    public Casilla[][] crearTablero() {
        Casilla[][] CasillasTmp = new Casilla[8][8];
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CasillasTmp[i][j] = new Casilla(new Coordenadas(Transformer.numToLetter(j), i));
            }
        }
        
        return CasillasTmp;
    }
    
    /**
     * Obtiene la instancia única de la estrategia simple.
     * Implementa el patrón Singleton de forma lazy (creación bajo demanda).
     * 
     * @return Instancia única de EstrategiaTableroSimple
     */
    public static EstrategiaTablero getInstancia() {
        if (instancia == null) {
            instancia = new EstrategiaTableroSimple();
        }
        return instancia;
    }
}
