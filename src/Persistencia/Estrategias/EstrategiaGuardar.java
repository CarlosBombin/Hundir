package Persistencia.Estrategias;

/**
 * Interfaz que define el contrato para las estrategias de guardado de datos.
 * Implementa el patrón Strategy para permitir diferentes métodos de persistencia
 * sin acoplar el código a una implementación específica.
 * 
 * Las clases que implementen esta interfaz deben proporcionar métodos para
 * acceder a los diferentes tipos de guardadores (usuarios, usuarios activos, partidas).
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 * @see GuardadoSimple
 */
public interface EstrategiaGuardar {

}
