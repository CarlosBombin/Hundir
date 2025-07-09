package Servidor;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Barcos.*;
import Cliente.Usuario;
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
import Tablero.Tablero;

import java.io.*;

public class Servidor {
    private static final int PUERTO = 7896;
    private static boolean end = false;
    
    private static final GuardadoSimple guardar = GuardadoSimple.getInstancia();
    private static final LecturaSimple leer = LecturaSimple.getInstancia();
    
    private static final ConcurrentHashMap<String, Partida> partidasActivas = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Usuario> usuariosConectados = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, EstadoColocacion> estadosColocacion = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor Hundir la Flota iniciado en puerto " + PUERTO);
        System.out.println("Cargando usuarios existentes...");
        
        cargarUsuariosExistentes();
        iniciarServidor();
    }
    
    private static void iniciarServidor() {
        try (ServerSocket listenSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor esperando conexiones...");
            
            while (!end) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Nueva conexión desde: " + clientSocket.getInetAddress());
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
            System.out.println("Usuarios cargados: " + usuarios.size());
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
            System.out.println("Usuario registrado: " + nombre);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error registrando usuario: " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized void conectarUsuario(String nombre, Usuario usuario) {
        usuariosConectados.put(nombre, usuario);
        System.out.println("Usuario conectado: " + nombre + " (Total: " + usuariosConectados.size() + ")");
    }
    
    public static synchronized void desconectarUsuario(String nombre) {
        usuariosConectados.remove(nombre);
        System.out.println("Usuario desconectado: " + nombre + " (Total: " + usuariosConectados.size() + ")");
    }
    
    public static synchronized boolean usuarioConectado(String nombre) {
        return usuariosConectados.containsKey(nombre);
    }
    
    public static synchronized String crearPartida(Usuario creador) {
        String idPartida = "partida_" + creador.getName() + "_" + System.currentTimeMillis();
        
        Partida partida = new Partida(creador, null);
        partidasActivas.put(idPartida, partida);
        
        System.out.println("Partida creada: " + idPartida + " por " + creador.getName());
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
            
            System.out.println("Partida iniciada: " + partida.getUsuarioPrincipal().getName() + " vs " + jugador.getName());
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
            guardar.setPartidas().guardar(partida);
            System.out.println("Partida finalizada: " + idPartida);
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
            System.out.println("Nuevo estado de colocación creado para partida: " + idPartida);
        }
        return estado;
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
        // Implementación básica - adaptar según tu lógica
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return false;
        
        // Por ahora retornar false para continuar colocando
        return false;
    }

    public static String obtenerBarcosRestantes(String idPartida, Usuario usuario) {
        // Implementación básica
        return "Portaviones:1, Submarinos:2, Destructores:3, Fragatas:4";
    }

    public static boolean finalizarColocacionUsuario(String idPartida, Usuario usuario) {
        // Implementación básica
        System.out.println("Finalizando colocación para " + usuario.getName() + " en partida " + idPartida);
        return true;
    }

    public static boolean ambosJugadoresListos(String idPartida) {
        // Implementación básica
        Partida partida = obtenerPartida(idPartida);
        return partida != null && partida.getUsuarioRival() != null;
    }

    public static boolean colocarBarco(String idPartida, Usuario usuario, String tipoBarco, int fila, int columna, String orientacion) {
        // Implementación básica - adaptar según tu lógica de tableros
        System.out.println("Colocando " + tipoBarco + " para " + usuario.getName() + " en (" + fila + "," + columna + ") " + orientacion);
        return true; // Por ahora siempre exitoso
    }
}