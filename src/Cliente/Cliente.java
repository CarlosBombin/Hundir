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

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
    }

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

    public static Usuario getUsuarioActual() {
        return miUsuario;
    }

    public static void cerrarConexion() {
        cerrarConexionSegura();
    }

    private static void cerrarConexionSegura() {
        if (s != null && !s.isClosed()) {
            try {
                s.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean esTipoBarcoValido(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return false;
        }

        String tipoUpper = tipo.toUpperCase().trim();
        return tipoUpper.equals("PORTAVIONES") || tipoUpper.equals("SUBMARINO") || 
               tipoUpper.equals("DESTRUCTOR") || tipoUpper.equals("FRAGATA");
    }

    public static String getInfoConexion() {
        if (s != null && !s.isClosed()) {
            return "Conectado a " + s.getRemoteSocketAddress();
        } else {
            return "No conectado";
        }
    }

    public static boolean estaConectado() {
        return s != null && !s.isClosed() && s.isConnected();
    }

    public static String getServidorInfo() {
        return "localhost:" + serverPort;
    }

    public static void debugEstadoConexion() {
        System.out.println("=== DEBUG ESTADO CONEXIÓN ===");
        System.out.println("Socket: " + (s != null ? "Existe" : "NULL"));
        System.out.println("Conectado: " + (s != null && !s.isClosed() && s.isConnected()));
        System.out.println("Usuario: " + (miUsuario != null ? miUsuario.getName() : "NULL"));
        System.out.println("Puerto: " + serverPort);
        System.out.println("=============================");
    }

    public static void enviarDebugAlServidor(DataOutputStream salida) {
        try {
            salida.writeUTF("debug_cliente");
            salida.writeUTF("Cliente activo: " + (miUsuario != null ? miUsuario.getName() : "sin_usuario"));
        } catch (IOException e) {
        }
    }
}
