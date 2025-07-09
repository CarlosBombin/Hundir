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

public class LeerUsuariosJson implements LeerUsuarios {
    
    private static final String ENCODING = "UTF-8";
    
    private final Gson gson;
    private final Path archivoPath;
    
    public LeerUsuariosJson(String nombreArchivo) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.archivoPath = Paths.get(nombreArchivo);
    }
    
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
    
    public Usuario buscarUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacio");
        }
        
        List<Usuario> usuarios = leer();
        for (int i = 0;i < usuarios.size();i++) {
            if (usuarios.get(i).getName().equals(nombreUsuario)) {
                return usuarios.get(i);
            }
        }

        return null;
    }
    
    public boolean existeUsuario(String nombreUsuario) {
        return buscarUsuario(nombreUsuario) != null;
    }
    
    public int obtenerNumeroUsuarios() {
        return leer().size();
    }
    
    public boolean archivoExiste() {
        return Files.exists(archivoPath);
    }
    
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
