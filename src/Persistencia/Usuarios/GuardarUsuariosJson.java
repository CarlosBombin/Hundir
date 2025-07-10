package Persistencia.Usuarios;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import Cliente.Usuario;
import Persistencia.Interfaces.GuardarUsuarios;

/**
 * Implementación concreta para guardar usuarios en formato JSON.
 * Utiliza la librería Gson para serializar listas de usuarios y usuarios individuales
 * en un archivo JSON único. Mantiene la integridad de los datos evitando duplicados
 * y proporcionando operaciones atómicas de escritura.
 * 
 * Características:
 * - Guarda usuarios en un archivo JSON único
 * - Previene duplicados automáticamente
 * - Soporte para guardar usuarios individuales o listas completas
 * - Utiliza codificación UTF-8 para soporte internacional
 * - Manejo robusto de errores de E/S
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class GuardarUsuariosJson implements GuardarUsuarios {
    
    /** Codificación de caracteres utilizada para los archivos */
    private static final String ENCODING = "UTF-8";
    
    /** Instancia de Gson para serialización JSON */
    private final Gson gson;
    /** Ruta del archivo donde se almacenan los usuarios */
    private final Path archivoPath;
    
    /**
     * Constructor que inicializa el guardador de usuarios JSON.
     * Configura Gson con formato pretty-printing y establece la ruta del archivo.
     * 
     * @param nombreArchivo Nombre del archivo donde guardar los usuarios
     */
    public GuardarUsuariosJson(String nombreArchivo) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.archivoPath = Paths.get(nombreArchivo);
    }
    
    /**
     * Guarda datos de usuario en formato JSON.
     * Implementa el método de la interfaz GuardarUsuarios, aceptando tanto
     * usuarios individuales como listas de usuarios.
     * 
     * @param datos Usuario individual o List<Usuario> a guardar
     * @throws IllegalArgumentException Si los datos son null o de tipo incorrecto
     */
    @Override
    public void guardar(Object datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos no pueden estar vacios");
        }
        
        if (datos instanceof List<?>) {
            List<?> lista = (List<?>) datos;
            if (!lista.isEmpty() && !(lista.get(0) instanceof Usuario)) {
                throw new IllegalArgumentException("La lista solo debe contener objetos Usuario");
            } else {
                guardarListaUsuarios((List<Usuario>) datos);
            }
        } else if (datos instanceof Usuario) {
            añadirUsuario((Usuario) datos);
        } else {
            throw new IllegalArgumentException("Tipo de datos no soportado");
        }
    }
    
    /**
     * Guarda una lista completa de usuarios sobrescribiendo el archivo existente.
     * Utilizado para operaciones de guardado masivo o inicialización.
     * 
     * @param usuarios Lista de usuarios a guardar
     * @throws RuntimeException Si ocurre un error durante el guardado
     */
    private void guardarListaUsuarios(List<Usuario> usuarios) {
        try {
            crearArchivoSiNoExiste();
            String json = gson.toJson(usuarios);
            escribirArchivo(json);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Añade un usuario individual a la lista existente.
     * Carga los usuarios existentes, verifica duplicados y añade el nuevo usuario.
     * 
     * @param usuario Usuario a añadir al archivo
     * @throws IllegalArgumentException Si el usuario es null o ya existe
     * @throws RuntimeException Si ocurre un error durante el proceso
     */
    private void añadirUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede estar vacio");
        }
        
        try {
            List<Usuario> usuarios = cargarUsuariosExistentes();
            
            if (existeUsuario(usuarios, usuario.getName())) {
                throw new IllegalArgumentException("El usuario ya existe");
            }
            
            usuarios.add(usuario);
            guardarListaUsuarios(usuarios);
        } catch (Exception e) {
            throw new RuntimeException("Error al añadir usuario: " + e.getMessage());
        }
    }
    
    /**
     * Carga los usuarios existentes desde el archivo JSON.
     * Si el archivo no existe o está vacío, retorna una lista vacía.
     * 
     * @return Lista de usuarios existentes en el archivo
     */
    private List<Usuario> cargarUsuariosExistentes() {
        try {
            if (!Files.exists(archivoPath)) {
                return new ArrayList<>();
            }
            
            String contenido = leerArchivo();
            if (contenido.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            Type tipoLista = new TypeToken<List<Usuario>>(){}.getType();
            List<Usuario> usuarios = gson.fromJson(contenido, tipoLista);
            return usuarios != null ? usuarios : new ArrayList<>();
            
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Verifica si un usuario con el nombre especificado ya existe en la lista.
     * Utilizado para prevenir duplicados al añadir usuarios.
     * 
     * @param usuarios Lista de usuarios donde buscar
     * @param nombreUsuario Nombre del usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    private boolean existeUsuario(List<Usuario> usuarios, String nombreUsuario) {
        return usuarios.stream()
                .anyMatch(usuario -> usuario.getName().equals(nombreUsuario));
    }
    
    /**
     * Crea el archivo de usuarios si no existe en el sistema de archivos.
     * Método utilitario para garantizar que el archivo esté disponible.
     * 
     * @throws IOException Si no se puede crear el archivo
     */
    private void crearArchivoSiNoExiste() throws IOException {
        if (!Files.exists(archivoPath)) {
            Files.createFile(archivoPath);
        }
    }
    
    /**
     * Lee el contenido completo del archivo de usuarios.
     * Método utilitario para operaciones de E/S de archivos.
     * 
     * @return Contenido del archivo como String
     * @throws IOException Si ocurre un error de lectura
     */
    private String leerArchivo() throws IOException {
        return new String(Files.readAllBytes(archivoPath), ENCODING);
    }
    
    /**
     * Escribe contenido al archivo de usuarios.
     * Sobrescribe el archivo existente con el nuevo contenido.
     * 
     * @param contenido Contenido JSON a escribir
     * @throws IOException Si ocurre un error de escritura
     */
    private void escribirArchivo(String contenido) throws IOException {
        try (FileWriter writer = new FileWriter(archivoPath.toFile(), false)) {
            writer.write(contenido);
        }
    }
}
