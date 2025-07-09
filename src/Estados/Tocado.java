package Estados;
public class Tocado implements Estado{
	private static Estado instancia;
	
	public static Estado getInstancia() {
		if (instancia == null) {
			instancia = new Tocado();
		}
		return instancia;
	}
	
	public Estado getDaño() {
		return Tocado.getInstancia();
	}
}
