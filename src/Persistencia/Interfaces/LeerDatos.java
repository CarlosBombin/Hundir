package Persistencia.Interfaces;

import java.util.List;

/**
 * Interfaz base para todas las operaciones de lectura de datos.
 * Define el contrato común que deben cumplir todos los lectores de datos
 * del sistema de persistencia, independientemente del tipo de dato específico.
 * 
 * Forma parte del patrón Strategy para permitir diferentes implementaciones
 * de lectura (JSON, XML, base de datos, etc.) de forma intercambiable.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface LeerDatos {
    
    /**
     * Lee datos desde el origen de datos configurado.
     * Cada implementación define el tipo específico de datos que retorna
     * y el formato desde el cual los lee.
     * 
     * @return Lista de objetos leídos desde la fuente de datos
     */
    public List<?> leer();
}
