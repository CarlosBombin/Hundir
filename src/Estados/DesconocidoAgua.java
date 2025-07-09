package Estados;
public class DesconocidoAgua implements Estado{
	private static Estado instancia;
	
	public static Estado getInstancia() {
		if (instancia == null) {
			instancia = new DesconocidoAgua();
		}
		return instancia;
	}
	
	public Estado getDaño() {
		return Agua.getInstancia();
	}
}
