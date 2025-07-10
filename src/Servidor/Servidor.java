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
    
    public static boolean esTurnoDeUsuario(String idPartida, Usuario usuario) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return false;
        boolean esTurno = usuario.equals(partida.getTurnoActual());
        System.out.println("[DEBUG] esTurnoDeUsuario: " + usuario.getName() + " -> " + esTurno + " (turno actual: " +
            (partida.getTurnoActual() != null ? partida.getTurnoActual().getName() : "null") + ")");
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
            System.out.println("[DEBUG] usuarioCompletoColocacion: partida no encontrada para " + idPartida);
            return false;
        }

        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);

        System.out.println("[DEBUG] usuarioCompletoColocacion para " + usuario.getName() + ": " +
            "Portaviones=" + contador.getPortaviones() +
            ", Submarinos=" + contador.getSubmarinos() +
            ", Destructores=" + contador.getDestructores() +
            ", Fragatas=" + contador.getFragatas());

        boolean completo = contador.getPortaviones() == 1 &&
                           contador.getSubmarinos() == 2 &&
                           contador.getDestructores() == 3 &&
                           contador.getFragatas() == 4;

        System.out.println("[DEBUG] usuarioCompletoColocacion: " + usuario.getName() + " completo=" + completo);
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
        System.out.println("[DEBUG] Finalizando colocación para " + usuario.getName() + " en partida " + idPartida);
        boolean completo = usuarioCompletoColocacion(idPartida, usuario);
        System.out.println("[DEBUG] ¿Ha colocado todos los barcos? " + completo);
        return completo;
    }

    public static boolean ambosJugadoresListos(String idPartida) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            System.out.println("[DEBUG] ambosJugadoresListos: partida no encontrada");
            return false;
        }
        Usuario principal = partida.getUsuarioPrincipal();
        Usuario rival = partida.getUsuarioRival();
        boolean principalListo = usuarioCompletoColocacion(idPartida, principal);
        boolean rivalListo = rival != null && usuarioCompletoColocacion(idPartida, rival);

        System.out.println("[DEBUG] ambosJugadoresListos: principal=" + (principal != null ? principal.getName() : "null") +
            " listo=" + principalListo + ", rival=" + (rival != null ? rival.getName() : "null") +
            " listo=" + rivalListo);

        return principalListo && rivalListo;
    }

    public static boolean colocarBarco(String idPartida, Usuario usuario, String tipoBarco, int fila, int columna, String orientacion) {
        System.out.println("[DEBUG] colocarBarco: " + tipoBarco + " (" + fila + "," + columna + ") " + orientacion + " para " + usuario.getName());
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            System.out.println("[DEBUG] Partida no encontrada");
            return false;
        }

        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);

        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            System.out.println("[DEBUG] Tipo de barco no válido: " + tipoBarco);
            return false;
        }

        if (!contador.puedeColocarBarco(tipo)) {
            System.out.println("[DEBUG] Límite alcanzado para " + tipoBarco);
            return false;
        }

        Tablero tablero = partida.getTableroJugador(usuario);
        if (tablero == null) {
            System.out.println("[DEBUG] Tablero no encontrado para usuario");
            return false;
        }

        boolean exito = colocarBarcoEnTablero(tablero, tipoBarco, fila, columna, orientacion);
        System.out.println("[DEBUG] Resultado colocarBarcoEnTablero: " + exito);

        if (!exito) {
            return false;
        }

        contador.colocarBarco(tipo);
        System.out.println("[DEBUG] Barco colocado y contador actualizado");
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
            System.out.println("[DEBUG] guardarPartida: partida no encontrada para " + idPartida);
            return;
        }
        try {
            String nombreArchivo = "partidas/" + partida.getUsuarioPrincipal().getName() + "vs" + partida.getUsuarioRival().getName() + ".json";
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(partida);
            Files.write(Paths.get(nombreArchivo), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("[DEBUG] Partida guardada en " + nombreArchivo);
        } catch (Exception e) {
            System.err.println("[DEBUG] Error guardando partida: " + e.getMessage());
        }
    }

    // Cuando crees una Connection, regístrala:
    public static void registrarConexion(String nombreUsuario, Connection conexion) {
        conexionesActivas.put(nombreUsuario, conexion);
    }

    // Cuando un usuario se desconecte, elimínala:
    public static void eliminarConexion(String nombreUsuario) {
        conexionesActivas.remove(nombreUsuario);
    }

    /**
     * Devuelve la Connection del rival en la partida, o null si no está conectado.
     */
    public static Connection getConexionRival(String idPartida, Usuario usuarioActual) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) {
            System.out.println("[DEBUG] getConexionRival: partida no encontrada para " + idPartida);
            return null;
        }
        Usuario rival = usuarioActual.equals(partida.getUsuarioPrincipal())
            ? partida.getUsuarioRival()
            : partida.getUsuarioPrincipal();
        if (rival == null) {
            System.out.println("[DEBUG] getConexionRival: rival es null");
            return null;
        }
        Connection conn = conexionesActivas.get(rival.getName());
        System.out.println("[DEBUG] getConexionRival: buscando conexión para " + rival.getName() + " -> " + (conn != null ? "ENCONTRADA" : "NO ENCONTRADA"));
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
            System.out.println("[DEBUG] getCasilla: no encontrada " + fila + "," + columna);
            return "error:Coordenada inválida";
        }

        Estado estadoAntes = casilla.getEstado();
        System.out.println("[DEBUG] procesarAtaque: atacante=" + atacante.getName() + " coords=" + fila + "," + columna);
        System.out.println("[DEBUG] Estado antes: " + estadoAntes.getClass().getSimpleName());

        // Si ya fue atacada
        if (estadoAntes.equals(Estados.Agua.getInstancia()) || estadoAntes.equals(Estados.Tocado.getInstancia())) {
            System.out.println("[DEBUG] Casilla ya atacada o es agua: " + fila + "," + columna);
            return "error:Casilla ya atacada";
        }

        casilla.getDaño();
        Estado estadoDespues = casilla.getEstado();
        System.out.println("[DEBUG] Estado después: " + estadoDespues.getClass().getSimpleName());

        // Si era DesconocidoAgua y ahora es Agua, es agua
        if (estadoAntes instanceof Estados.DesconocidoAgua && estadoDespues.equals(Estados.Agua.getInstancia())) {
            return "agua";
        }
        // Si era DesconocidoBarco y ahora es Tocado, es tocado/hundido
        if (estadoAntes instanceof Estados.DesconocidoBarco && estadoDespues.equals(Estados.Tocado.getInstancia())) {
            Barco barco = casilla.getBarco();
            if (barco != null && barco.estaHundido()) {
                System.out.println("[DEBUG] Barco hundido en " + fila + "," + columna);
                return "hundido";
            }
            return "tocado";
        }
        // Si tras el ataque es hundido (si tienes ese estado)
        if (estadoDespues.getClass().getSimpleName().contains("Hundido")) {
            return "hundido";
        }

        System.out.println("[DEBUG] Estado inesperado tras ataque: " + estadoDespues.getClass().getSimpleName());
        return "error:Estado inesperado";
    }
}