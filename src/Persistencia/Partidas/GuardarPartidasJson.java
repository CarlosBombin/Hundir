package Persistencia.Partidas;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Partida.Partida;
import Persistencia.Interfaces.GuardarPartidas;

/**
 * Implementación concreta para guardar partidas en formato JSON.
 * Utiliza la librería Gson para serializar objetos Partida a archivos JSON
 * organizados en una estructura de carpetas. Cada partida se guarda en un
 * archivo individual identificado por los nombres de los jugadores.
 * 
 * Características:
 * - Guarda cada partida en un archivo JSON separado
 * - Crea automáticamente la carpeta de partidas si no existe
 * - Utiliza codificación UTF-8 para soporte internacional
 * - Formatea el JSON con indentación para legibilidad
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class GuardarPartidasJson implements GuardarPartidas {
    
    /** Nombre de la carpeta donde se almacenan las partidas */
    private static final String CARPETA_PARTIDAS = "partidas";
    /** Extensión de archivo para las partidas guardadas */
    private static final String EXTENSION = ".json";
    /** Codificación de caracteres utilizada para los archivos */
    private static final String ENCODING = "UTF-8";
    
    /** Instancia de Gson para serialización JSON */
    private final Gson gson;
    /** Ruta de la carpeta donde se almacenan las partidas */
    private final Path carpetaPartidas;
    
    /**
     * Constructor que inicializa el guardador de partidas JSON.
     * Configura Gson con formato pretty-printing y crea la carpeta de partidas.
     */
    public GuardarPartidasJson() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.carpetaPartidas = Paths.get(CARPETA_PARTIDAS);
        crearCarpetaSiNoExiste();
    }
    
    /**
     * Guarda un objeto en formato JSON.
     * Implementa el método de la interfaz GuardarPartidas validando que
     * el objeto sea una instancia de Partida.
     * 
     * @param datos Objeto a guardar, debe ser una instancia de Partida
     * @throws IllegalArgumentException Si los datos son null o no son una Partida
     */
    @Override
    public void guardar(Object datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos no pueden estar vacíos");
        }
        
        if (datos instanceof Partida) {
            guardarPartida((Partida) datos);
        } else {
            throw new IllegalArgumentException("Solo se pueden guardar objetos de tipo Partida");
        }
    }
    
    /**
     * Guarda una partida específica en un archivo JSON.
     * Crea un archivo con nombre basado en los jugadores de la partida.
     * 
     * @param partida Partida a guardar en el sistema de archivos
     * @throws IllegalArgumentException Si la partida es null
     * @throws RuntimeException Si ocurre un error durante el guardado
     */
    public void guardarPartida(Partida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser null");
        }
        
        try {
            String nombreArchivo = generarNombreArchivo(partida);
            Path archivoPartida = carpetaPartidas.resolve(nombreArchivo);
            
            String json = gson.toJson(partida);
            escribirArchivo(archivoPartida, json);
            
            System.out.println("Partida guardada: " + nombreArchivo);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar partida: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza una partida existente sobrescribiendo su archivo.
     * Útil para guardar el progreso de una partida en curso.
     * 
     * @param partida Partida con los datos actualizados
     * @throws IllegalArgumentException Si la partida es null
     * @throws RuntimeException Si ocurre un error durante la actualización
     */
    public void actualizarPartida(Partida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser null");
        }
        
        try {
            guardarPartida(partida);
            
            if (!partida.getMovimientos().isEmpty()) {
                System.out.println("Partida actualizada con nuevo movimiento");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar partida: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera el nombre del archivo basado en la representación string de la partida.
     * Utiliza el método toString() de Partida que devuelve "Usuario1 vs Usuario2".
     * 
     * @param partida Partida para la cual generar el nombre
     * @return Nombre del archivo con extensión JSON
     */
    private String generarNombreArchivo(Partida partida) {
        String nombre = partida.toString();
        return nombre + EXTENSION;
    }
    
    /**
     * Crea la carpeta de partidas si no existe en el sistema de archivos.
     * Se ejecuta automáticamente durante la inicialización del guardador.
     * 
     * @throws RuntimeException Si no se puede crear la carpeta
     */
    private void crearCarpetaSiNoExiste() {
        try {
            if (!Files.exists(carpetaPartidas)) {
                Files.createDirectories(carpetaPartidas);
                System.out.println("Carpeta de partidas creada: " + CARPETA_PARTIDAS);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al crear carpeta de partidas: " + e.getMessage(), e);
        }
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
     * Escribe contenido a un archivo específico.
     * Sobrescribe el archivo existente o crea uno nuevo si no existe.
     * 
     * @param archivo Ruta del archivo donde escribir
     * @param contenido Contenido a escribir en el archivo
     * @throws IOException Si ocurre un error de escritura
     */
    private void escribirArchivo(Path archivo, String contenido) throws IOException {
        try (FileWriter writer = new FileWriter(archivo.toFile(), false)) {
            writer.write(contenido);
        }
    }
}
