package Estados;

public class Agua implements Estado{
	private static Estado instancia;
	
	public static Estado getInstancia() {
		if (instancia == null) {
			instancia = new Agua();
		}
		return instancia;
	}
	
	public Estado getDaño() {
		return Agua.getInstancia();
	}
}
