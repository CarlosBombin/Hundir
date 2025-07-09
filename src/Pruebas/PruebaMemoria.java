package Pruebas;

import java.util.ArrayList;
import java.util.List;
import Cliente.Usuario;
import Persistencia.Usuarios.GuardarUsuariosJson;
import Persistencia.Usuarios.LeerUsuariosJson;

public class PruebaMemoria {
    
    public static void main(String[] args) {
        System.out.println("🚀 INICIANDO PRUEBAS DE MEMORIA - LEER Y GUARDAR USUARIOS JSON");
        System.out.println("=".repeat(70));
        
        LeerUsuariosJson lector = new LeerUsuariosJson("usuarios.json");
        GuardarUsuariosJson guardador = new GuardarUsuariosJson("usuarios.json");
        
        // PRUEBA 1: Estado inicial
        try {
            System.out.println("\n📋 PRUEBA 1: Estado inicial del archivo");
            System.out.println("Archivo existe: " + lector.archivoExiste());
            System.out.println("Archivo está vacío: " + lector.archivoEstaVacio());
            System.out.println("Número de usuarios: " + lector.obtenerNumeroUsuarios());
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 1: " + e.getMessage());
        }
        
        // PRUEBA 2: Guardar usuarios individuales
        try {
            System.out.println("\n💾 PRUEBA 2: Guardando usuarios individuales");
            
            Usuario usuario1 = new Usuario("juan", "password123");
            guardador.guardar(usuario1);
            System.out.println("✅ Usuario 'juan' guardado");
        } catch (Exception e) {
            System.err.println("❌ ERROR guardando 'juan': " + e.getMessage());
        }
        
        try {
            Usuario usuario2 = new Usuario("ana", "password456");
            guardador.guardar(usuario2);
            System.out.println("✅ Usuario 'ana' guardado");
        } catch (Exception e) {
            System.err.println("❌ ERROR guardando 'ana': " + e.getMessage());
        }
        
        try {
            Usuario usuario3 = new Usuario("luis", "password789");
            guardador.guardar(usuario3);
            System.out.println("✅ Usuario 'luis' guardado");
        } catch (Exception e) {
            System.err.println("❌ ERROR guardando 'luis': " + e.getMessage());
        }
        
        // PRUEBA 3: Leer usuarios
        try {
            System.out.println("\n📖 PRUEBA 3: Leyendo usuarios");
            List<Usuario> usuariosLeidos = lector.leer();
            System.out.println("Total usuarios leídos: " + usuariosLeidos.size());
            
            for (Usuario u : usuariosLeidos) {
                System.out.println("  - " + u.getName() + " (password: " + u.getPassword() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 3: " + e.getMessage());
        }
        
        // PRUEBA 4: Buscar usuarios específicos
        try {
            System.out.println("\n🔍 PRUEBA 4: Buscando usuarios específicos");
            
            Usuario juanEncontrado = lector.buscarUsuario("juan");
            if (juanEncontrado != null) {
                System.out.println("✅ Usuario 'juan' encontrado: " + juanEncontrado.getName());
            } else {
                System.out.println("❌ Usuario 'juan' NO encontrado");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR buscando 'juan': " + e.getMessage());
        }
        
        try {
            Usuario pedroEncontrado = lector.buscarUsuario("pedro");
            if (pedroEncontrado != null) {
                System.out.println("✅ Usuario 'pedro' encontrado: " + pedroEncontrado.getName());
            } else {
                System.out.println("ℹ️ Usuario 'pedro' NO existe (esperado)");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR buscando 'pedro': " + e.getMessage());
        }
        
        // PRUEBA 5: Verificar existencia
        try {
            System.out.println("\n✔️ PRUEBA 5: Verificando existencia de usuarios");
            System.out.println("¿Existe 'ana'?: " + lector.existeUsuario("ana"));
            System.out.println("¿Existe 'maria'?: " + lector.existeUsuario("maria"));
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 5: " + e.getMessage());
        }
        
        // PRUEBA 6: Intentar duplicado
        try {
            System.out.println("\n🚫 PRUEBA 6: Intentando guardar duplicado");
            Usuario usuarioDuplicado = new Usuario("juan", "nueva_password");
            guardador.guardar(usuarioDuplicado);
            System.out.println("❌ ERROR: Se permitió duplicado");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Duplicado rechazado correctamente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado en PRUEBA 6: " + e.getMessage());
        }
        
        // PRUEBA 7: Guardar lista completa
        try {
            System.out.println("\n📝 PRUEBA 7: Guardando lista completa (reemplazar)");
            List<Usuario> nuevaLista = new ArrayList<>();
            nuevaLista.add(new Usuario("admin", "admin123"));
            nuevaLista.add(new Usuario("user1", "user123"));
            nuevaLista.add(new Usuario("user2", "user456"));
            
            guardador.guardar(nuevaLista);
            System.out.println("✅ Lista completa guardada");
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 7: " + e.getMessage());
        }
        
        // PRUEBA 8: Verificar reemplazo
        try {
            System.out.println("\n🔄 PRUEBA 8: Verificando reemplazo");
            List<Usuario> usuariosDespuesReemplazo = lector.leer();
            System.out.println("Usuarios después del reemplazo: " + usuariosDespuesReemplazo.size());
            
            for (Usuario u : usuariosDespuesReemplazo) {
                System.out.println("  - " + u.getName());
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 8: " + e.getMessage());
        }
        
        // PRUEBA 9: Volver a añadir usuarios individualmente
        try {
            System.out.println("\n➕ PRUEBA 9: Añadiendo más usuarios individuales");
            
            Usuario usuario4 = new Usuario("maria", "maria123");
            guardador.guardar(usuario4);
            System.out.println("✅ Usuario 'maria' añadido");
        } catch (Exception e) {
            System.err.println("❌ ERROR añadiendo 'maria': " + e.getMessage());
        }
        
        try {
            Usuario usuario5 = new Usuario("carlos", "carlos456");
            guardador.guardar(usuario5);
            System.out.println("✅ Usuario 'carlos' añadido");
        } catch (Exception e) {
            System.err.println("❌ ERROR añadiendo 'carlos': " + e.getMessage());
        }
        
        // PRUEBA 10: Estado final
        try {
            System.out.println("\n📊 PRUEBA 10: Estado final");
            System.out.println("Archivo existe: " + lector.archivoExiste());
            System.out.println("Archivo está vacío: " + lector.archivoEstaVacio());
            System.out.println("Número final de usuarios: " + lector.obtenerNumeroUsuarios());
            
            List<Usuario> usuariosFinales = lector.leer();
            System.out.println("Lista final de usuarios:");
            for (Usuario u : usuariosFinales) {
                System.out.println("  - " + u.getName() + " (password: " + u.getPassword() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 10: " + e.getMessage());
        }
        
        // PRUEBA 11: Pruebas de validación
        try {
            System.out.println("\n🔒 PRUEBA 11: Pruebas de validación");
            testearValidaciones(guardador, lector);
        } catch (Exception e) {
            System.err.println("❌ ERROR en PRUEBA 11: " + e.getMessage());
        }
        
        System.out.println("\n🎉 TODAS LAS PRUEBAS COMPLETADAS");
        System.out.println("📁 Revisa el archivo 'usuarios.json' para ver el resultado");
    }
    
    private static void testearValidaciones(GuardarUsuariosJson guardador, LeerUsuariosJson lector) {
        // Probar datos null
        try {
            guardador.guardar(null);
            System.out.println("❌ ERROR: Se permitió guardar null");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Null rechazado correctamente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado con null: " + e.getMessage());
        }
        
        // Probar usuario null
        try {
            Usuario usuarioNull = null;
            guardador.guardar(usuarioNull);
            System.out.println("❌ ERROR: Se permitió guardar usuario null");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Usuario null rechazado correctamente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado con usuario null: " + e.getMessage());
        }
        
        // Probar buscar con nombre null
        try {
            lector.buscarUsuario(null);
            System.out.println("❌ ERROR: Se permitió buscar con nombre null");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Búsqueda con null rechazada correctamente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado buscando null: " + e.getMessage());
        }
        
        // Probar buscar con nombre vacío
        try {
            lector.buscarUsuario("");
            System.out.println("❌ ERROR: Se permitió buscar con nombre vacío");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Búsqueda con nombre vacío rechazada correctamente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado buscando vacío: " + e.getMessage());
        }
        
        // Probar lista con tipos incorrectos
        try {
            List<String> listaIncorrecta = new ArrayList<>();
            listaIncorrecta.add("no_es_usuario");
            guardador.guardar(listaIncorrecta);
            System.out.println("❌ ERROR: Se permitió lista con tipos incorrectos");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Lista incorrecta rechazada correctamente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado con lista incorrecta: " + e.getMessage());
        }
    }
}
