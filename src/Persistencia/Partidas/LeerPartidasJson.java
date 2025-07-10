package Persistencia.Partidas;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import Partida.Partida;
import Persistencia.Interfaces.LeerPartidas;
import Cliente.Usuario;

/**
 * Implementación concreta para leer partidas desde archivos JSON.
 * Utiliza la librería Gson para deserializar archivos JSON a objetos Partida.
 * Proporciona métodos para cargar partidas individuales, buscar por usuario
 * y obtener listados completos de partidas almacenadas.
 * 
 * Características:
 * - Lee partidas desde archivos JSON individuales
 * - Soporte para búsqueda por usuario específico
 * - Búsqueda de partidas entre dos usuarios
 * - Listado de IDs de partidas disponibles
 * - Manejo robusto de errores de E/S
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class LeerPartidasJson implements LeerPartidas {
    
    /** Nombre de la carpeta donde se almacenan las partidas */
    private static final String CARPETA_PARTIDAS = "partidas";
    /** Extensión de archivo para las partidas guardadas */
    private static final String EXTENSION = ".json";
    /** Codificación de caracteres utilizada para los archivos */
    private static final String ENCODING = "UTF-8";
    
    /** Instancia de Gson para deserialización JSON */
    private final Gson gson;
    /** Ruta de la carpeta donde se almacenan las partidas */
    private final Path carpetaPartidas;
    
    /**
     * Constructor que inicializa el lector de partidas JSON.
     * Configura Gson con formato pretty-printing y establece la ruta de partidas.
     */
    public LeerPartidasJson() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.carpetaPartidas = Paths.get(CARPETA_PARTIDAS);
    }
    
    /**
     * Lee todas las partidas disponibles en el sistema.
     * Implementa el método de la interfaz LeerPartidas.
     * 
     * @return Lista de todas las partidas encontradas
     */
    @Override
    public List<Partida> leer() {
        return cargarTodasLasPartidas();
    }
    
    /**
     * Carga todas las partidas disponibles desde el sistema de archivos.
     * Recorre todos los archivos JSON en la carpeta de partidas y los deserializa.
     * 
     * @return Lista de partidas cargadas exitosamente
     */
    public List<Partida> cargarTodasLasPartidas() {
        List<Partida> partidas = new ArrayList<>();
        
        try {
            if (!Files.exists(carpetaPartidas)) {
                return partidas;
            }
            
            List<String> idsPartidas = listarIdsPartidas();
            
            for (String id : idsPartidas) {
                Partida partida = cargarPartida(id);
                if (partida != null) {
                    partidas.add(partida);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar todas las partidas: " + e.getMessage());
        }
        
        return partidas;
    }
    
    /**
     * Carga una partida específica por su ID.
     * El ID corresponde al nombre del archivo sin la extensión.
     * 
     * @param idPartida Identificador único de la partida
     * @return Partida cargada o null si no existe o hay error
     * @throws IllegalArgumentException Si el ID es null o vacío
     */
    public Partida cargarPartida(String idPartida) {
        if (idPartida == null || idPartida.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de partida no puede estar vacío.");
        }
        
        try {
            String nombreArchivo = idPartida + EXTENSION;
            Path archivoPartida = carpetaPartidas.resolve(nombreArchivo);
            
            if (!Files.exists(archivoPartida)) {
                return null;
            }
            
            String contenido = leerArchivo(archivoPartida);
            if (contenido.trim().isEmpty()) {
                return null;
            }
            
            return gson.fromJson(contenido, Partida.class);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Busca todas las partidas en las que participa un usuario específico.
     * Un usuario puede ser tanto el jugador principal como el rival.
     * 
     * @param nombreUsuario Usuario del cual buscar partidas
     * @return Lista de partidas donde participa el usuario
     * @throws IllegalArgumentException Si el usuario es null
     */
    public List<Partida> buscarPartidasPorUsuario(Usuario nombreUsuario) {
        if (nombreUsuario == null) {
            throw new IllegalArgumentException("El usuario ha de existir.");
        }
        
        List<Partida> partidasDelUsuario = new ArrayList<>();
        List<Partida> todasLasPartidas = cargarTodasLasPartidas();
        
        for (Partida partida : todasLasPartidas) {
            if (partida.getUsuarioPrincipal().equals(nombreUsuario) || 
                partida.getUsuarioRival().equals(nombreUsuario)) {
                partidasDelUsuario.add(partida);
            }
        }
        
        return partidasDelUsuario;
    }
    
    /**
     * Busca una partida específica entre dos usuarios.
     * Útil para verificar si dos usuarios ya tienen una partida en curso.
     * 
     * @param usuario1 Primer usuario de la partida
     * @param usuario2 Segundo usuario de la partida
     * @return Partida entre los usuarios o null si no existe
     * @throws IllegalArgumentException Si alguno de los usuarios es null
     */
    public Partida buscarPartidaEntreUsuarios(Usuario usuario1, Usuario usuario2) {
        if (usuario1 == null || usuario2 == null) {
            throw new IllegalArgumentException("Los usuarios han de existir.");
        }
        
        List<Partida> todasLasPartidas = cargarTodasLasPartidas();
        
        for (Partida partida : todasLasPartidas) {
            Usuario principal = partida.getUsuarioPrincipal();
            Usuario rival = partida.getUsuarioRival();
            
            if ((principal.equals(usuario1) && rival.equals(usuario2)) ||
                (principal.equals(usuario2) && rival.equals(usuario1))) {
                return partida;
            }
        }
        
        return null;
    }
    
    /**
     * Verifica si una partida específica existe en el sistema de archivos.
     * Utiliza el método toString() de la partida para generar el nombre del archivo.
     * 
     * @param partida Partida a verificar
     * @return true si la partida existe, false en caso contrario
     */
    public boolean existePartida(Partida partida) {
        if (partida == null || partida.toString().isEmpty()) {
            return false;
        }
        
        String nombreArchivo = partida.toString() + EXTENSION;
        Path archivoPartida = carpetaPartidas.resolve(nombreArchivo);
        return Files.exists(archivoPartida);
    }
    
    /**
     * Lee el contenido completo de un archivo.
     * Método utilitario para operaciones de E/S de archivos.
     * 
     * @param archivo Ruta del archivo a leer
     * @return Contenido del archivo como String
     * @throws IOException Si ocurre un error de lectura
     */
    private String leerArchivo(Path archivo) throws IOException {
        return new String(Files.readAllBytes(archivo), ENCODING);
    }

    /**
     * Lista todos los IDs de partidas disponibles en el sistema.
     * Extrae los nombres de archivo sin la extensión JSON.
     * 
     * @return Lista de IDs de partidas encontradas
     */
    public List<String> listarIdsPartidas() {
        List<String> ids = new ArrayList<>();
        
        try {
            if (Files.exists(carpetaPartidas)) {
                Files.list(carpetaPartidas)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .forEach(path -> {
                        String nombre = path.getFileName().toString();
                        String id = nombre.substring(0, nombre.length() - EXTENSION.length());
                        ids.add(id);
                    });
            }
        } catch (Exception e) {
            System.err.println("Error al listar IDs de partidas: " + e.getMessage());
        }
        
        return ids;
    }
}
