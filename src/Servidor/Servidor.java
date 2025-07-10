package Servidor;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Barcos.*;
import Cliente.Usuario;
import Estados.Estado;
import Partida.Partida;
import Persistencia.Estrategias.GuardadoSimple;
import Persistencia.Estrategias.LecturaSimple;
import Persistencia.Usuarios.LeerUsuariosJson;
import Sistema.EstadoColocacion;
import Sistema.ContadorBarcosJugador;
import Sistema.TipoBarco;
import Sistema.ValidadorColocacion;
import Sistema.FabricaBarcos;
import Persistencia.Usuarios.GuardarUsuariosJson;
import Tablero.Casilla;
import Tablero.Coordenadas;
import Tablero.Tablero;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Servidor {
    private static final int PUERTO = 7896;
    private static boolean end = false;
    
    private static final GuardadoSimple guardar = GuardadoSimple.getInstancia();
    private static final LecturaSimple leer = LecturaSimple.getInstancia();
    
    private static final ConcurrentHashMap<String, Partida> partidasActivas = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Usuario> usuariosConectados = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, EstadoColocacion> estadosColocacion = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Connection> conexionesActivas = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        cargarUsuariosExistentes();
        iniciarServidor();
    }
    
    private static void iniciarServidor() {
        try (ServerSocket listenSocket = new ServerSocket(PUERTO)) {
            
            while (!end) {
                Socket clientSocket = listenSocket.accept();
                Connection connection = new Connection(clientSocket);
                connection.start();
            }
            
        } catch (IOException e) {
            System.out.println("Error en servidor: " + e.getMessage());
        }
    }
    
    private static void cargarUsuariosExistentes() {
        try {
            List<Usuario> usuarios = leer.getUsuarios().leer();
        } catch (Exception e) {
            System.out.println("No se pudieron cargar usuarios existentes: " + e.getMessage());
        }
    }
    
    public static synchronized Usuario validarUsuario(String nombre, String contraseña) {
        if (nombre == null || contraseña == null || nombre.trim().isEmpty() || contraseña.trim().isEmpty()) {
            System.err.println("Error: credenciales vacías o nulas");
            return null;
        }
        
        try {
            LeerUsuariosJson lectorUsuarios = (LeerUsuariosJson) leer.getUsuarios();
            
            Usuario usuario = lectorUsuarios.buscarUsuario(nombre);
            if (usuario != null && usuario.getPassword().equals(contraseña)) {
                return usuario;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error validando usuario: " + e.getMessage());
            return null;
        }
    }
    
    public static synchronized boolean registrarUsuario(String nombre, String contraseña) {
        try {
            LeerUsuariosJson lectorUsuarios = (LeerUsuariosJson) leer.getUsuarios();
            GuardarUsuariosJson guardadorUsuarios = (GuardarUsuariosJson) guardar.setUsuarios();
            
            if (lectorUsuarios.existeUsuario(nombre)) {
                return false;
            }
            
            Usuario nuevoUsuario = new Usuario(nombre, contraseña);
            guardadorUsuarios.guardar(nuevoUsuario);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error registrando usuario: " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized void conectarUsuario(String nombre, Usuario usuario) {
        usuariosConectados.put(nombre, usuario);
    }
    
    public static synchronized void desconectarUsuario(String nombre) {
        usuariosConectados.remove(nombre);
    }
    
    public static synchronized boolean usuarioConectado(String nombre) {
        return usuariosConectados.containsKey(nombre);
    }
    
    public static synchronized String crearPartida(Usuario creador) {
        String idPartida = "partida_" + creador.getName() + "_" + System.currentTimeMillis();
        
        Partida partida = new Partida(creador, null);
        partidasActivas.put(idPartida, partida);
        
        return idPartida;
    }
    
    public static synchronized List<String> obtenerPartidasDisponibles() {
        List<String> disponibles = new ArrayList<>();
        
        for (String id : partidasActivas.keySet()) {
            Partida partida = partidasActivas.get(id);
            if (partida.getUsuarioRival() == null) {
                disponibles.add(id + " - Creada por: " + partida.getUsuarioPrincipal().getName());
            }
        }
        
        return disponibles;
    }
    
    public static synchronized boolean unirseAPartida(String idPartida, Usuario jugador) {
        Partida partida = partidasActivas.get(idPartida);
        
        if (partida != null && partida.getUsuarioRival() == null) {
            Partida partidaCompleta = new Partida(partida.getUsuarioPrincipal(), jugador);
            partidasActivas.put(idPartida, partidaCompleta);
            
            guardar.setPartidas().guardar(partidaCompleta);
            return true;
        }
        
        return false;
    }
    
    public static synchronized Partida obtenerPartida(String idPartida) {
        return partidasActivas.get(idPartida);
    }
    
    public static synchronized void finalizarPartida(String idPartida) {
        Partida partida = partidasActivas.remove(idPartida);
        if (partida != null) {
            try {
                Persistencia.Partidas.GuardarPartidasJson guardador = new Persistencia.Partidas.GuardarPartidasJson();
                guardador.actualizarPartida(partida);
            } catch (Exception e) {
                System.err.println("Error actualizando partida: " + e.getMessage());
            }
        }
    }
    
    public static synchronized String obtenerEstadoServidor() {
        return String.format("Usuarios conectados: %d | Partidas activas: %d", usuariosConectados.size(), partidasActivas.size());
    }
    
    public static synchronized boolean terminaServicio() {
        end = true;
        System.out.println("Servidor terminando...");
        return end;
    }

    public static synchronized String obtenerEstadoColocacion(String idPartida, Usuario usuario) {
        Partida partida = partidasActivas.get(idPartida);
        if (partida == null) {
            return "error";
        }
        
        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        
        return estado.obtenerDescripcion(esPrincipal);
    }
    
    public static synchronized EstadoColocacion obtenerOCrearEstadoColocacion(String idPartida) {
        EstadoColocacion estado = estadosColocacion.get(idPartida);
        if (estado == null) {
            estado = new EstadoColocacion();
            estadosColocacion.put(idPartida, estado);
        }
        return estado;
    }
    
    public static boolean esTurnoDeUsuario(String idPartida, Usuario usuario) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return false;
        boolean esTurno = usuario.equals(partida.getTurnoActual());
        return esTurno;
    }

    public static void cambiarTurno(String idPartida) {
        Partida partida = obtenerPartida(idPartida);
        if (partida != null) {
            partida.cambiarTurno();
        }
    }
    
    private static boolean esTurnoDelUsuario(String idPartida, Usuario usuario) {
        Partida partida = partidasActivas.get(idPartida);
        if (partida == null) {
            return false;
        }
        
        EstadoColocacion estado = estadosColocacion.get(idPartida);
        if (estado == null) {
            return false;
        }
        
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        return estado.puedeColocarBarco(usuario, esPrincipal);
    }
    
    private static boolean puedeColocarBarco(EstadoColocacion estado, String tipoBarco, boolean esPrincipal) {
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            return false;
        }
        
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);
        return contador.puedeColocarBarco(tipo);
    }

    private static void actualizarContadorBarcos(EstadoColocacion estado, String tipoBarco, boolean esPrincipal) {
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            return;
        }
        
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);
        contador.colocarBarco(tipo);
    }

    private static boolean colocarBarcoEnTablero(Tablero tablero, String tipoBarco, int fila, int columna, String orientacion) {
        ValidadorColocacion validador = new ValidadorColocacion(tablero);
        
        if (!validador.esValidaColocacion(tipoBarco, fila, columna, orientacion)) {
            return false;
        }
        
        FabricaBarcos fabrica = new FabricaBarcos(tablero);
        Barco barco = fabrica.crearBarco(tipoBarco, fila, columna, orientacion);
        
        return barco != null;
    }

    public static boolean usuarioCompletoColocacion(String idPartida, Usuario usuario) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            return false;
        }

        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);

        boolean completo = contador.getPortaviones() == 1 &&
                           contador.getSubmarinos() == 2 &&
                           contador.getDestructores() == 3 &&
                           contador.getFragatas() == 4;

        return completo;
    }

    public static String obtenerBarcosRestantes(String idPartida, Usuario usuario) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return "Portaviones:0, Submarinos:0, Destructores:0, Fragatas:0";

        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);

        int portaviones = 1 - contador.getPortaviones();
        int submarinos = 2 - contador.getSubmarinos();
        int destructores = 3 - contador.getDestructores();
        int fragatas = 4 - contador.getFragatas();

        return String.format("Portaviones:%d, Submarino:%d, Destructor:%d, Fragata:%d",
                Math.max(0, portaviones), Math.max(0, submarinos), Math.max(0, destructores), Math.max(0, fragatas));
    }

    public static boolean finalizarColocacionUsuario(String idPartida, Usuario usuario) {
        boolean completo = usuarioCompletoColocacion(idPartida, usuario);
        return completo;
    }

    public static boolean ambosJugadoresListos(String idPartida) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            return false;
        }
        Usuario principal = partida.getUsuarioPrincipal();
        Usuario rival = partida.getUsuarioRival();
        boolean principalListo = usuarioCompletoColocacion(idPartida, principal);
        boolean rivalListo = rival != null && usuarioCompletoColocacion(idPartida, rival);

        return principalListo && rivalListo;
    }

    public static boolean colocarBarco(String idPartida, Usuario usuario, String tipoBarco, int fila, int columna, String orientacion) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            return false;
        }

        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);

        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            return false;
        }

        if (!contador.puedeColocarBarco(tipo)) {
            return false;
        }

        Tablero tablero = partida.getTableroJugador(usuario);
        if (tablero == null) {
            return false;
        }

        boolean exito = colocarBarcoEnTablero(tablero, tipoBarco, fila, columna, orientacion);

        if (!exito) {
            return false;
        }

        contador.colocarBarco(tipo);
        return true;
    }

    public static boolean puedeColocarBarco(String idPartida, Usuario usuario, String tipoBarco) {
        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return false;
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) return false;
        return contador.puedeColocarBarco(tipo);
    }

    public static void guardarPartida(String idPartida) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            return;
        }
        try {
            String nombreArchivo = "partidas/" + partida.getUsuarioPrincipal().getName() + "vs" + partida.getUsuarioRival().getName() + ".json";
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(partida);
            Files.write(Paths.get(nombreArchivo), json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {}
    }

    public static void registrarConexion(String nombreUsuario, Connection conexion) {
        conexionesActivas.put(nombreUsuario, conexion);
    }

    public static void eliminarConexion(String nombreUsuario) {
        conexionesActivas.remove(nombreUsuario);
    }

    public static Connection getConexionRival(String idPartida, Usuario usuarioActual) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            return null;
        }
        Usuario rival = usuarioActual.equals(partida.getUsuarioPrincipal())
            ? partida.getUsuarioRival()
            : partida.getUsuarioPrincipal();
        if (rival == null) {
            return null;
        }
        Connection conn = conexionesActivas.get(rival.getName());
        return conn;
    }

    public static String procesarAtaque(String idPartida, Usuario atacante, int fila, int columna) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return "error:Partida no encontrada";

        Usuario defensor = atacante.equals(partida.getUsuarioPrincipal())
            ? partida.getUsuarioRival()
            : partida.getUsuarioPrincipal();

        Tablero tableroDefensor = partida.getTableroJugador(defensor);
        Coordenadas coords = new Coordenadas(fila, columna);

        Casilla casilla = tableroDefensor.getCasilla(coords);
        if (casilla == null) {
            return "error:Coordenada inválida";
        }

        Estado estadoAntes = casilla.getEstado();
        
        if (estadoAntes.equals(Estados.Agua.getInstancia()) || estadoAntes.equals(Estados.Tocado.getInstancia())) {
            return "error:Casilla ya atacada";
        }

        casilla.getDaño();
        Estado estadoDespues = casilla.getEstado();
        
        if (estadoAntes instanceof Estados.DesconocidoAgua && estadoDespues.equals(Estados.Agua.getInstancia())) {
            return "agua";
        }
        
        if (estadoAntes instanceof Estados.DesconocidoBarco && estadoDespues.equals(Estados.Tocado.getInstancia())) {
            Barco barco = casilla.getBarco();
            if (barco != null && barco.estaHundido()) {
                return "hundido";
            }
            return "tocado";
        }
        
        if (estadoDespues.getClass().getSimpleName().contains("Hundido")) {
            return "hundido";
        }

        return "error:Estado inesperado";
    }
}