package Persistencia.Interfaces;

/**
 * Interfaz especializada para el guardado de datos de partidas.
 * Extiende GuardarDatos para operaciones específicas con objetos Partida.
 * 
 * Las implementaciones de esta interfaz deben ser capaces de persistir partidas
 * en diferentes destinos (archivos JSON, bases de datos, etc.) conservando
 * toda la información de la partida incluyendo jugadores, movimientos y estado.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public interface GuardarPartidas extends GuardarDatos{
    
    /**
     * Guarda datos de partida en el destino configurado.
     * Especializa el método base para manejar específicamente objetos Partida
     * o colecciones de partidas.
     * 
     * @param datos Partida o colección de partidas a persistir
     */
    public void guardar(Object datos);
}
