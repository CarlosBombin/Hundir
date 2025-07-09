package Tablero;
import Sistema.Transformer;

public class EstrategiaTableroSimple implements EstrategiaTablero {
	public static EstrategiaTablero instancia;
	
	@Override
	public Casilla[][] crearTablero() {
		Casilla[][] CasillasTmp = new Casilla[8][8];
		
		for (int i = 0;i < 8;i++) {
			for (int j = 0;j < 8;j++) {
				CasillasTmp[i][j] = new Casilla(new Coordenadas(Transformer.numToLetter(j), i + 1));
			}
		}
		
		return CasillasTmp;
	}
	
	public static EstrategiaTablero getInstancia() {
		if (instancia == null) {
			instancia = new EstrategiaTableroSimple();
		}
		return instancia;
	}
	
	
}
