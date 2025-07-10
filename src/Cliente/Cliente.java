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

/**
 * Clase principal del cliente del juego Hundir la Flota.
 * Gestiona la conexión con el servidor, la autenticación de usuarios
 * y el inicio de la interfaz gráfica del juego.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Cliente {
    
    /** Socket de conexión con el servidor */
    static Socket s = null;
    /** Puerto del servidor al que se conecta el cliente */
    static int serverPort = 7896;
    /** Usuario actualmente conectado */
    private static Usuario miUsuario;

    /**
     * Método principal que inicia la aplicación cliente.
     * Configura la interfaz gráfica y muestra el diálogo de autenticación.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String args[]) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                DialogoAutenticacion dialogoAuth = new DialogoAutenticacion();
                Usuario usuario = dialogoAuth.mostrarDialogo();

                if (usuario == null) {
                    System.exit(0);
                    return;
                }

                miUsuario = usuario;
                conectarAlServidor(usuario);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Configura el Look and Feel de la interfaz gráfica.
     * Establece el tema Nimbus para una apariencia moderna.
     */
    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Usar look and feel por defecto si falla
        }
    }

    /**
     * Establece la conexión con el servidor y autentica al usuario.
     * Si la autenticación es exitosa, inicia la ventana del juego.
     * 
     * @param usuario Usuario a autenticar
     */
    private static void conectarAlServidor(Usuario usuario) {
        try {
            s = new Socket("localhost", serverPort);

            DataInputStream entrada = new DataInputStream(s.getInputStream());
            DataOutputStream salida = new DataOutputStream(s.getOutputStream());

            if (autenticarConServidor(usuario, entrada, salida)) {
                new VentanaJuego(entrada, salida);
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Error de autenticación.\nVerifique sus credenciales.", 
                    "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                cerrarConexionSegura();
                System.exit(1);
            }

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo conectar al servidor.\nAsegúrese de que el servidor esté ejecutándose en localhost:" + serverPort, 
                "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error de comunicación con el servidor:\n" + e.getMessage(), 
                "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Autentica al usuario con el servidor mediante login o registro.
     * Envía las credenciales y procesa la respuesta del servidor.
     * 
     * @param usuario Usuario a autenticar
     * @param entrada Flujo de entrada del servidor
     * @param salida Flujo de salida al servidor
     * @return true si la autenticación es exitosa, false en caso contrario
     */
    private static boolean autenticarConServidor(Usuario usuario, DataInputStream entrada, DataOutputStream salida) {
        try {
            String respuesta = entrada.readUTF();

            if (!"auth_required".equals(respuesta)) {
                return false;
            }

            String tipoOperacion = usuario.esNuevo() ? "registro" : "login";
            salida.writeUTF(tipoOperacion);
            salida.flush();

            salida.writeUTF(usuario.getName());
            salida.writeUTF(usuario.getPassword());
            salida.flush();

            String resultado = entrada.readUTF();

            if (resultado.startsWith("auth_success:")) {
                return true;
            } else if (resultado.startsWith("auth_error:")) {
                return false;
            } else {
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el usuario actualmente conectado.
     * 
     * @return Usuario actual o null si no hay usuario conectado
     */
    public static Usuario getUsuarioActual() {
        return miUsuario;
    }

    /**
     * Cierra la conexión con el servidor.
     * Método público para cerrar la conexión desde otras clases.
     */
    public static void cerrarConexion() {
        cerrarConexionSegura();
    }

    /**
     * Cierra la conexión con el servidor de forma segura.
     * Verifica que el socket existe y está abierto antes de cerrarlo.
     */
    private static void cerrarConexionSegura() {
        if (s != null && !s.isClosed()) {
            try {
                s.close();
            } catch (IOException e) {
                // Ignorar errores al cerrar
            }
        }
    }

    /**
     * Valida si un tipo de barco es válido en el juego.
     * Tipos válidos: PORTAVIONES, SUBMARINO, DESTRUCTOR, FRAGATA.
     * 
     * @param tipo Tipo de barco a validar
     * @return true si el tipo es válido, false en caso contrario
     */
    public static boolean esTipoBarcoValido(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return false;
        }

        String tipoUpper = tipo.toUpperCase().trim();
        return tipoUpper.equals("PORTAVIONES") || tipoUpper.equals("SUBMARINO") || 
               tipoUpper.equals("DESTRUCTOR") || tipoUpper.equals("FRAGATA");
    }

    /**
     * Obtiene información sobre la conexión actual.
     * 
     * @return String con información de conexión o estado de desconexión
     */
    public static String getInfoConexion() {
        if (s != null && !s.isClosed()) {
            return "Conectado a " + s.getRemoteSocketAddress();
        } else {
            return "No conectado";
        }
    }

    /**
     * Verifica si el cliente está conectado al servidor.
     * 
     * @return true si hay conexión activa, false en caso contrario
     */
    public static boolean estaConectado() {
        return s != null && !s.isClosed() && s.isConnected();
    }

    /**
     * Obtiene información del servidor configurado.
     * 
     * @return String con la dirección y puerto del servidor
     */
    public static String getServidorInfo() {
        return "localhost:" + serverPort;
    }

    /**
     * Muestra información de debug sobre el estado de la conexión.
     * Útil para diagnóstico y resolución de problemas.
     */
    public static void debugEstadoConexion() {
        System.out.println("=== DEBUG ESTADO CONEXIÓN ===");
        System.out.println("Socket: " + (s != null ? "Existe" : "NULL"));
        System.out.println("Conectado: " + (s != null && !s.isClosed() && s.isConnected()));
        System.out.println("Usuario: " + (miUsuario != null ? miUsuario.getName() : "NULL"));
        System.out.println("Puerto: " + serverPort);
        System.out.println("=============================");
    }

    /**
     * Envía información de debug al servidor.
     * Comunica al servidor el estado actual del cliente.
     * 
     * @param salida Flujo de salida al servidor
     */
    public static void enviarDebugAlServidor(DataOutputStream salida) {
        try {
            salida.writeUTF("debug_cliente");
            salida.writeUTF("Cliente activo: " + (miUsuario != null ? miUsuario.getName() : "sin_usuario"));
        } catch (IOException e) {
            // Ignorar errores de comunicación
        }
    }
}
