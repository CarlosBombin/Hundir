package Persistencia.Partidas;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Partida.Partida;
import Persistencia.Interfaces.GuardarPartidas;

public class GuardarPartidasJson implements GuardarPartidas {
    
    private static final String CARPETA_PARTIDAS = "partidas";
    private static final String EXTENSION = ".json";
    private static final String ENCODING = "UTF-8";
    
    private final Gson gson;
    private final Path carpetaPartidas;
    
    public GuardarPartidasJson() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.carpetaPartidas = Paths.get(CARPETA_PARTIDAS);
        crearCarpetaSiNoExiste();
    }
    
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
    
    private String generarNombreArchivo(Partida partida) {
        String nombre = partida.toString();
        return nombre + EXTENSION;
    }
    
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
    
    private String leerArchivo(Path archivo) throws IOException {
        return new String(Files.readAllBytes(archivo), ENCODING);
    }
    
    private void escribirArchivo(Path archivo, String contenido) throws IOException {
        try (FileWriter writer = new FileWriter(archivo.toFile(), false)) {
            writer.write(contenido);
        }
    }
}
