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
import Persistencia.Interfaces.LeerUsuarios;

/**
 * Implementación concreta para leer usuarios desde archivos JSON.
 * Utiliza la librería Gson para deserializar archivos JSON a listas de usuarios.
 * Proporciona métodos para búsqueda, validación y operaciones utilitarias
 * sobre los datos de usuarios almacenados.
 * 
 * Características:
 * - Lee usuarios desde un archivo JSON único
 * - Soporte para búsqueda por nombre de usuario
 * - Validación de credenciales de usuario
 * - Métodos utilitarios para gestión de archivos
 * - Manejo robusto de errores de E/O
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class LeerUsuariosJson implements LeerUsuarios {
    
    /** Codificación de caracteres utilizada para los archivos */
    private static final String ENCODING = "UTF-8";
    
    /** Instancia de Gson para deserialización JSON */
    private final Gson gson;
    /** Ruta del archivo donde se almacenan los usuarios */
    private final Path archivoPath;
    
    /**
     * Constructor que inicializa el lector de usuarios JSON.
     * Configura Gson con formato pretty-printing y establece la ruta del archivo.
     * 
     * @param nombreArchivo Nombre del archivo desde donde leer los usuarios
     */
    public LeerUsuariosJson(String nombreArchivo) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.archivoPath = Paths.get(nombreArchivo);
    }
    
    /**
     * Lee todos los usuarios desde el archivo JSON.
     * Implementa el método de la interfaz LeerUsuarios.
     * 
     * @return Lista de usuarios encontrados en el archivo
     */
    @Override
    public List<Usuario> leer() {
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
            System.err.println("Error al cargar usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca un usuario específico por su nombre de usuario.
     * Realiza búsqueda lineal en la lista de usuarios cargados.
     * 
     * @param nombreUsuario Nombre del usuario a buscar
     * @return Usuario encontrado o null si no existe
     * @throws IllegalArgumentException Si el nombre es null o vacío
     */
    public Usuario buscarUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacio");
        }
        
        List<Usuario> usuarios = leer();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getName().equals(nombreUsuario)) {
                return usuarios.get(i);
            }
        }

        return null;
    }
    
    /**
     * Verifica si un usuario con el nombre especificado existe en el archivo.
     * Método de conveniencia que utiliza buscarUsuario() internamente.
     * 
     * @param nombreUsuario Nombre del usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    public boolean existeUsuario(String nombreUsuario) {
        return buscarUsuario(nombreUsuario) != null;
    }
    
    /**
     * Obtiene el número total de usuarios almacenados.
     * Útil para estadísticas y validaciones de sistema.
     * 
     * @return Cantidad de usuarios en el archivo
     */
    public int obtenerNumeroUsuarios() {
        return leer().size();
    }
    
    /**
     * Verifica si el archivo de usuarios existe en el sistema de archivos.
     * Método utilitario para verificaciones de estado.
     * 
     * @return true si el archivo existe, false en caso contrario
     */
    public boolean archivoExiste() {
        return Files.exists(archivoPath);
    }
    
    /**
     * Verifica si el archivo de usuarios está vacío.
     * Considera vacío un archivo que no existe o que contiene solo espacios en blanco.
     * 
     * @return true si el archivo está vacío, false en caso contrario
     */
    public boolean archivoEstaVacio() {
        try {
            if (!Files.exists(archivoPath)) {
                return true;
            }
            
            String contenido = leerArchivo();
            return contenido.trim().isEmpty() || contenido.trim().equals("[]");
            
        } catch (IOException e) {
            return true;
        }
    }
    
    private String leerArchivo() throws IOException {
        return new String(Files.readAllBytes(archivoPath), ENCODING);
    }

    public boolean validarCredenciales(String nombre, String contraseña) {
        Usuario usuario = buscarUsuario(nombre);
        return usuario != null && usuario.getPassword().equals(contraseña);
    }
}
