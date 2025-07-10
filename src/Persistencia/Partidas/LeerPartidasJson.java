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

public class LeerPartidasJson implements LeerPartidas {
    
    private static final String CARPETA_PARTIDAS = "partidas";
    private static final String EXTENSION = ".json";
    private static final String ENCODING = "UTF-8";
    
    private final Gson gson;
    private final Path carpetaPartidas;
    
    public LeerPartidasJson() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.carpetaPartidas = Paths.get(CARPETA_PARTIDAS);
    }
    
    @Override
    public List<Partida> leer() {
        return cargarTodasLasPartidas();
    }
    
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
    
    public List<Partida> buscarPartidasPorUsuario(Usuario nombreUsuario) {
        if (nombreUsuario == null) {
            throw new IllegalArgumentException("El usuario ha de existir.");
        }
        
        List<Partida> partidasDelUsuario = new ArrayList<>();
        List<Partida> todasLasPartidas = cargarTodasLasPartidas();
        
        for (Partida partida : todasLasPartidas) {
            if (partida.getUsuarioPrincipal().equals(nombreUsuario) || partida.getUsuarioRival().equals(nombreUsuario)) {
                partidasDelUsuario.add(partida);
            }
        }
        
        return partidasDelUsuario;
    }
    
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
    
    public boolean existePartida(Partida partida) {
        if (partida == null || partida.toString().isEmpty()) {
            return false;
        }
        
        String nombreArchivo = partida.toString() + EXTENSION;
        Path archivoPartida = carpetaPartidas.resolve(nombreArchivo);
        return Files.exists(archivoPartida);
    }
    
    private String leerArchivo(Path archivo) throws IOException {
        return new String(Files.readAllBytes(archivo), ENCODING);
    }

    public List<String> listarIdsPartidas() {
        List<String> ids = new ArrayList<>();
        
        try {
            if (Files.exists(carpetaPartidas)) {
                Files.list(carpetaPartidas).filter(path -> path.toString().endsWith(EXTENSION)).forEach(path -> {
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
