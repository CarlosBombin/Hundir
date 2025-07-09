package Estados;

public class DesconocidoBarco implements Estado{
	private static Estado instancia;
	
	public static Estado getInstancia() {
		if (instancia == null) {
			instancia = new DesconocidoBarco();
		}
		return instancia;
	}
	
	public Estado getDaño() {
		return Tocado.getInstancia();
	}
}
