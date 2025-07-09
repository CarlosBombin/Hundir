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

public class GuardarUsuariosJson implements GuardarUsuarios {
    private static final String ENCODING = "UTF-8";
    
    private final Gson gson;
    private final Path archivoPath;
    
    public GuardarUsuariosJson(String nombreArchivo) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.archivoPath = Paths.get(nombreArchivo);
    }
    
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
    
    private void guardarListaUsuarios(List<Usuario> usuarios) {
        try {
            crearArchivoSiNoExiste();
            String json = gson.toJson(usuarios);
            escribirArchivo(json);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar usuarios: " + e.getMessage());
        }
    }
    
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
    
    private boolean existeUsuario(List<Usuario> usuarios, String nombreUsuario) {
        return usuarios.stream()
                .anyMatch(usuario -> usuario.getName().equals(nombreUsuario));
    }
    
    private void crearArchivoSiNoExiste() throws IOException {
        if (!Files.exists(archivoPath)) {
            Files.createFile(archivoPath);
        }
    }
    
    private String leerArchivo() throws IOException {
        return new String(Files.readAllBytes(archivoPath), ENCODING);
    }
    
    private void escribirArchivo(String contenido) throws IOException {
        try (FileWriter writer = new FileWriter(archivoPath.toFile(), false)) {
            writer.write(contenido);
        }
    }
}
