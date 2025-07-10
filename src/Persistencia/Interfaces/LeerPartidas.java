package Persistencia.Interfaces;

import java.util.List;

import Partida.Partida;

/**
 * Interfaz especializada para la lectura de datos de partidas.
 * Extiende LeerDatos especificando que el tipo de retorno son objetos Partida.
 * 
 * Las implementaciones de esta interfaz deben ser capaces de leer partidas
 * desde diferentes fuentes (archivos JSON, bases de datos, etc.) y
 * convertirlas a objetos Partida del dominio con toda su información asociada.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface LeerPartidas extends LeerDatos{
    
    /**
     * Lee una lista de partidas desde la fuente de datos configurada.
     * Especializa el método base para retornar específicamente objetos Partida.
     * 
     * @return Lista de partidas leídas desde la fuente de datos
     */
    public List<Partida> leer();
}
