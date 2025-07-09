package Cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

import GUI.DialogoAutenticacion;
import GUI.VentanaJuego;

public class Cliente {
    static Socket s = null;
    static int serverPort = 7896;
    private static Usuario miUsuario;
    
    public static void main(String args[]) {
        configurarLookAndFeel();
        
        SwingUtilities.invokeLater(() -> {
            try {
                DialogoAutenticacion dialogoAuth = new DialogoAutenticacion();
                Usuario usuario = dialogoAuth.mostrarDialogo();
                
                if (usuario == null) {
                    System.out.println("Autenticación cancelada por el usuario");
                    System.exit(0);
                    return;
                }
                
                miUsuario = usuario;
                System.out.println("Iniciando conexión para usuario: " + usuario.getName());
                
                conectarAlServidor(usuario);
                
            } catch (Exception e) {
                System.err.println("Error inesperado en main: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            System.out.println("Look & Feel configurado: " + UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            System.err.println("No se pudo configurar Look & Feel: " + e.getMessage());
            System.err.println("Usando Look & Feel por defecto");
        }
    }
    
    private static void conectarAlServidor(Usuario usuario) {
        try {
            System.out.println("Conectando al servidor localhost:" + serverPort);
            s = new Socket("localhost", serverPort);
            System.out.println("Conexión establecida exitosamente");
            
            DataInputStream entrada = new DataInputStream(s.getInputStream());
            DataOutputStream salida = new DataOutputStream(s.getOutputStream());
            
            // Autenticar UNA SOLA VEZ
            if (autenticarConServidor(usuario, entrada, salida)) {
                System.out.println("Autenticación exitosa - Abriendo ventana principal");
                
                // Usar LA MISMA conexión para el juego
                new VentanaJuego(entrada, salida);
                
                // NO cerrar la conexión aquí - VentanaJuego la maneja
                
            } else {
                System.err.println("Fallo en la autenticación");
                JOptionPane.showMessageDialog(null, 
                    "Error de autenticación.\nVerifique sus credenciales.", 
                    "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                cerrarConexionSegura();
                System.exit(1);
            }
            
        } catch (UnknownHostException e) {
            System.err.println("Host no encontrado: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "No se pudo conectar al servidor.\nAsegúrese de que el servidor esté ejecutándose en localhost:" + serverPort, 
                "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            
        } catch (IOException e) {
            System.err.println("Error de I/O: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error de comunicación con el servidor:\n" + e.getMessage(), 
                "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    // Método para autenticar con el servidor (mejorado)
    private static boolean autenticarConServidor(Usuario usuario, DataInputStream entrada, DataOutputStream salida) {
        try {
            System.out.println("Iniciando proceso de autenticación...");
            
            // Leer mensaje inicial del servidor
            String respuesta = entrada.readUTF();
            System.out.println("Respuesta inicial del servidor: " + respuesta);
            
            if (!"auth_required".equals(respuesta)) {
                System.err.println("Protocolo de autenticación inesperado: " + respuesta);
                return false;
            }
            
            // Enviar tipo de operación
            String tipoOperacion = usuario.esNuevo() ? "registro" : "login";
            System.out.println("Enviando tipo de operación: " + tipoOperacion);
            salida.writeUTF(tipoOperacion);
            salida.flush(); // IMPORTANTE: Forzar envío
            
            // Enviar credenciales
            System.out.println("Enviando credenciales para usuario: " + usuario.getName());
            salida.writeUTF(usuario.getName());
            salida.writeUTF(usuario.getPassword());
            salida.flush(); // IMPORTANTE: Forzar envío
            
            // Leer resultado
            String resultado = entrada.readUTF();
            System.out.println("Resultado de autenticación: " + resultado);
            
            if (resultado.startsWith("auth_success:")) {
                System.out.println("Autenticación exitosa: " + resultado.substring(13));
                return true;
            } else if (resultado.startsWith("auth_error:")) {
                System.err.println("Error de autenticación: " + resultado.substring(11));
                return false;
            } else {
                System.err.println("Respuesta de autenticación no reconocida: " + resultado);
                return false;
            }
            
        } catch (IOException e) {
            System.err.println("Error de comunicación durante autenticación: " + e.getMessage());
            e.printStackTrace(); // DEBUG
            return false;
        } catch (Exception e) {
            System.err.println("Error inesperado durante autenticación: " + e.getMessage());
            e.printStackTrace(); // DEBUG
            return false;
        }
    }
    
    // Método para obtener el usuario actual (para uso en ventana)
    public static Usuario getUsuarioActual() {
        return miUsuario;
    }
    
    // Método para cerrar conexión de forma segura (mejorado)
    public static void cerrarConexion() {
        cerrarConexionSegura();
    }
    
    private static void cerrarConexionSegura() {
        if (s != null && !s.isClosed()) {
            try {
                System.out.println("Cerrando conexión con el servidor...");
                s.close();
                System.out.println("Conexión cerrada exitosamente");
            } catch (IOException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        } else {
            System.out.println("Conexión ya estaba cerrada o era null");
        }
    }
    
    // Método auxiliar para validar tipos de barco
    public static boolean esTipoBarcoValido(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return false;
        }
        
        String tipoUpper = tipo.toUpperCase().trim();
        return tipoUpper.equals("PORTAVIONES") || tipoUpper.equals("SUBMARINO") || 
               tipoUpper.equals("DESTRUCTOR") || tipoUpper.equals("FRAGATA");
    }
    
    // Método para obtener información de conexión
    public static String getInfoConexion() {
        if (s != null && !s.isClosed()) {
            return "Conectado a " + s.getRemoteSocketAddress();
        } else {
            return "No conectado";
        }
    }
    
    // Método para verificar si está conectado
    public static boolean estaConectado() {
        return s != null && !s.isClosed() && s.isConnected();
    }
    
    // Método para obtener configuración del servidor
    public static String getServidorInfo() {
        return "localhost:" + serverPort;
    }
    
    // Agregar al final de la clase Cliente este método de debug:
    public static void debugEstadoConexion() {
        System.out.println("=== DEBUG ESTADO CONEXIÓN ===");
        System.out.println("Socket: " + (s != null ? "Existe" : "NULL"));
        System.out.println("Conectado: " + (s != null && !s.isClosed() && s.isConnected()));
        System.out.println("Usuario: " + (miUsuario != null ? miUsuario.getName() : "NULL"));
        System.out.println("Puerto: " + serverPort);
        System.out.println("=============================");
    }
    
    // Agregar método para enviar debug al servidor:
    public static void enviarDebugAlServidor(DataOutputStream salida) {
        try {
            System.out.println("Enviando debug al servidor...");
            salida.writeUTF("debug_cliente");
            salida.writeUTF("Cliente activo: " + (miUsuario != null ? miUsuario.getName() : "sin_usuario"));
        } catch (IOException e) {
            System.err.println("Error enviando debug: " + e.getMessage());
        }
    }
}
