package Persistencia.Interfaces;

/**
 * Interfaz base para todas las operaciones de guardado de datos.
 * Define el contrato común que deben cumplir todos los guardadores de datos
 * del sistema de persistencia, independientemente del tipo de dato específico.
 * 
 * Forma parte del patrón Strategy para permitir diferentes implementaciones
 * de guardado (JSON, XML, base de datos, etc.) de forma intercambiable.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface GuardarDatos {
    
    /**
     * Guarda un objeto en el destino de datos configurado.
     * Cada implementación define cómo serializar y persistir el objeto
     * según el formato y destino específico.
     * 
     * @param datos Objeto a guardar en la fuente de datos
     */
    public void guardar(Object datos);
}
