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

/**
 * Clase principal del servidor del juego Hundir la Flota.
 * Gestiona todas las conexiones de clientes, partidas activas, autenticación de usuarios
 * y coordinación del juego. Implementa un servidor multihilo que puede manejar
 * múltiples partidas simultáneas.
 * 
 * Funcionalidades principales:
 * - Servidor TCP multihilo en puerto 7896
 * - Gestión de usuarios y autenticación
 * - Creación y administración de partidas
 * - Coordinación de turnos y ataques
 * - Persistencia de datos de usuarios y partidas
 * - Validación de colocación de barcos
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class Servidor {
    /** Puerto en el que escucha el servidor */
    private static final int PUERTO = 7896;
    /** Flag para controlar el shutdown del servidor */
    private static boolean end = false;
    
    /** Instancia singleton para operaciones de guardado */
    private static final GuardadoSimple guardar = GuardadoSimple.getInstancia();
    /** Instancia singleton para operaciones de lectura */
    private static final LecturaSimple leer = LecturaSimple.getInstancia();
    
    /** Mapa de partidas activas indexadas por ID */
    private static final ConcurrentHashMap<String, Partida> partidasActivas = new ConcurrentHashMap<>();
    /** Mapa de usuarios conectados indexados por nombre */
    private static final ConcurrentHashMap<String, Usuario> usuariosConectados = new ConcurrentHashMap<>();
    /** Mapa de estados de colocación por partida */
    private static final ConcurrentHashMap<String, EstadoColocacion> estadosColocacion = new ConcurrentHashMap<>();
    /** Mapa de conexiones activas indexadas por nombre de usuario */
    private static final ConcurrentHashMap<String, Connection> conexionesActivas = new ConcurrentHashMap<>();

    /**
     * Método principal que inicia el servidor.
     * Carga usuarios existentes e inicia el servidor TCP.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        cargarUsuariosExistentes();
        iniciarServidor();
    }
    
    /**
     * Inicia el servidor TCP y acepta conexiones de clientes.
     * Crea un hilo nuevo para cada cliente que se conecta.
     */
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
    
    /**
     * Carga los usuarios existentes desde el sistema de persistencia.
     * Se ejecuta al iniciar el servidor para recuperar datos previos.
     */
    private static void cargarUsuariosExistentes() {
        try {
            List<Usuario> usuarios = leer.getUsuarios().leer();
        } catch (Exception e) {
            System.out.println("No se pudieron cargar usuarios existentes: " + e.getMessage());
        }
    }
    
    /**
     * Valida las credenciales de un usuario contra la base de datos.
     * Verifica que el nombre y contraseña coincidan con un usuario registrado.
     * 
     * @param nombre Nombre del usuario a validar
     * @param contraseña Contraseña del usuario
     * @return Usuario válido o null si las credenciales son incorrectas
     */
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
    
    /**
     * Registra un nuevo usuario en el sistema.
     * Verifica que el nombre no esté en uso antes de crear el usuario.
     * 
     * @param nombre Nombre del usuario a registrar
     * @param contraseña Contraseña del usuario
     * @return true si el registro fue exitoso, false si el usuario ya existe
     */
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
    
    /**
     * Registra un usuario como conectado en el servidor.
     * Añade el usuario al mapa de usuarios activos.
     * 
     * @param nombre Nombre del usuario que se conecta
     * @param usuario Objeto Usuario completo
     */
    public static synchronized void conectarUsuario(String nombre, Usuario usuario) {
        usuariosConectados.put(nombre, usuario);
    }
    
    /**
     * Desconecta un usuario del servidor.
     * Remueve el usuario del mapa de usuarios activos.
     * 
     * @param nombre Nombre del usuario a desconectar
     */
    public static synchronized void desconectarUsuario(String nombre) {
        usuariosConectados.remove(nombre);
    }
    
    /**
     * Verifica si un usuario específico está conectado al servidor.
     * 
     * @param nombre Nombre del usuario a verificar
     * @return true si el usuario está conectado, false en caso contrario
     */
    public static synchronized boolean usuarioConectado(String nombre) {
        return usuariosConectados.containsKey(nombre);
    }
    
    /**
     * Crea una nueva partida con un usuario como creador.
     * Genera un ID único para la partida y la registra en el servidor.
     * 
     * @param creador Usuario que crea la partida
     * @return ID único de la partida creada
     */
    public static synchronized String crearPartida(Usuario creador) {
        String idPartida = "partida_" + creador.getName() + "_" + System.currentTimeMillis();
        
        Partida partida = new Partida(creador, null);
        partidasActivas.put(idPartida, partida);
        
        return idPartida;
    }
    
    /**
     * Obtiene la lista de partidas disponibles para unirse.
     * Retorna solo partidas que necesitan un segundo jugador.
     * 
     * @return Lista de strings describiendo las partidas disponibles
     */
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
    
    /**
     * Une un jugador a una partida existente.
     * Verifica que la partida exista y tenga espacio disponible.
     * 
     * @param idPartida ID de la partida a la cual unirse
     * @param jugador Usuario que se une a la partida
     * @return true si se unió exitosamente, false en caso contrario
     */
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
    
    /**
     * Obtiene una partida específica por su ID.
     * 
     * @param idPartida ID de la partida buscada
     * @return Objeto Partida o null si no existe
     */
    public static synchronized Partida obtenerPartida(String idPartida) {
        return partidasActivas.get(idPartida);
    }
    
    /**
     * Finaliza una partida y la remueve de las partidas activas.
     * Guarda la partida finalizada en el sistema de persistencia.
     * 
     * @param idPartida ID de la partida a finalizar
     */
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
    
    /**
     * Obtiene estadísticas básicas del servidor.
     * 
     * @return String con información de usuarios conectados y partidas activas
     */
    public static synchronized String obtenerEstadoServidor() {
        return String.format("Usuarios conectados: %d | Partidas activas: %d", usuariosConectados.size(), partidasActivas.size());
    }
    
    /**
     * Inicia el proceso de shutdown del servidor.
     * 
     * @return true indicando que el servidor se está cerrando
     */
    public static synchronized boolean terminaServicio() {
        end = true;
        System.out.println("Servidor terminando...");
        return end;
    }

    /**
     * Obtiene el estado de colocación de un usuario en una partida específica.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario del cual obtener el estado
     * @return Descripción del estado de colocación o "error" si hay problemas
     */
    public static synchronized String obtenerEstadoColocacion(String idPartida, Usuario usuario) {
        Partida partida = partidasActivas.get(idPartida);
        if (partida == null) {
            return "error";
        }
        
        EstadoColocacion estado = obtenerOCrearEstadoColocacion(idPartida);
        boolean esPrincipal = usuario.equals(partida.getUsuarioPrincipal());
        
        return estado.obtenerDescripcion(esPrincipal);
    }
    
    /**
     * Obtiene o crea el estado de colocación para una partida.
     * Si no existe, crea uno nuevo e inicializado.
     * 
     * @param idPartida ID de la partida
     * @return Estado de colocación para la partida
     */
    public static synchronized EstadoColocacion obtenerOCrearEstadoColocacion(String idPartida) {
        EstadoColocacion estado = estadosColocacion.get(idPartida);
        if (estado == null) {
            estado = new EstadoColocacion();
            estadosColocacion.put(idPartida, estado);
        }
        return estado;
    }
    
    /**
     * Verifica si es el turno de un usuario específico en una partida.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario a verificar
     * @return true si es su turno, false en caso contrario
     */
    public static boolean esTurnoDeUsuario(String idPartida, Usuario usuario) {
        Partida partida = obtenerPartida(idPartida);
        if (partida == null) return false;
        boolean esTurno = usuario.equals(partida.getTurnoActual());
        return esTurno;
    }

    /**
     * Cambia el turno al otro jugador en una partida.
     * 
     * @param idPartida ID de la partida donde cambiar el turno
     */
    public static void cambiarTurno(String idPartida) {
        Partida partida = obtenerPartida(idPartida);
        if (partida != null) {
            partida.cambiarTurno();
        }
    }
    
    /**
     * Verifica si es el turno de un usuario para realizar acciones.
     * Método interno que considera el estado de colocación.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario a verificar
     * @return true si puede realizar acciones, false en caso contrario
     */
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
    
    /**
     * Verifica si un usuario puede colocar un tipo específico de barco.
     * 
     * @param estado Estado de colocación de la partida
     * @param tipoBarco Tipo de barco a verificar
     * @param esPrincipal Si el usuario es el jugador principal
     * @return true si puede colocar el barco, false en caso contrario
     */
    private static boolean puedeColocarBarco(EstadoColocacion estado, String tipoBarco, boolean esPrincipal) {
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            return false;
        }
        
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);
        return contador.puedeColocarBarco(tipo);
    }

    /**
     * Actualiza el contador de barcos después de colocar uno.
     * 
     * @param estado Estado de colocación de la partida
     * @param tipoBarco Tipo de barco que se colocó
     * @param esPrincipal Si el usuario es el jugador principal
     */
    private static void actualizarContadorBarcos(EstadoColocacion estado, String tipoBarco, boolean esPrincipal) {
        TipoBarco tipo = TipoBarco.fromString(tipoBarco);
        if (tipo == null) {
            return;
        }
        
        ContadorBarcosJugador contador = estado.getContador(esPrincipal);
        contador.colocarBarco(tipo);
    }

    /**
     * Coloca físicamente un barco en el tablero.
     * Valida la posición y crea el barco usando la fábrica correspondiente.
     * 
     * @param tablero Tablero donde colocar el barco
     * @param tipoBarco Tipo de barco a colocar
     * @param fila Fila de colocación
     * @param columna Columna de colocación
     * @param orientacion Orientación del barco
     * @return true si se colocó exitosamente, false en caso contrario
     */
    private static boolean colocarBarcoEnTablero(Tablero tablero, String tipoBarco, int fila, int columna, String orientacion) {
        ValidadorColocacion validador = new ValidadorColocacion(tablero);
        
        if (!validador.esValidaColocacion(tipoBarco, fila, columna, orientacion)) {
            return false;
        }
        
        FabricaBarcos fabrica = new FabricaBarcos(tablero);
        Barco barco = fabrica.crearBarco(tipoBarco, fila, columna, orientacion);
        
        return barco != null;
    }

    /**
     * Verifica si un usuario ha completado la colocación de todos sus barcos.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario a verificar
     * @return true si ha colocado todos los barcos requeridos, false en caso contrario
     */
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

    /**
     * Obtiene la cantidad de barcos restantes por colocar para un usuario.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario del cual obtener los barcos restantes
     * @return String formateado con los barcos restantes por tipo
     */
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

    /**
     * Finaliza la colocación de barcos para un usuario específico.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario que finaliza la colocación
     * @return true si la finalización fue exitosa, false en caso contrario
     */
    public static boolean finalizarColocacionUsuario(String idPartida, Usuario usuario) {
        boolean completo = usuarioCompletoColocacion(idPartida, usuario);
        return completo;
    }

    /**
     * Verifica si ambos jugadores han completado la colocación de barcos.
     * 
     * @param idPartida ID de la partida a verificar
     * @return true si ambos jugadores están listos, false en caso contrario
     */
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

    /**
     * Coloca un barco en el tablero de un usuario.
     * Coordina la validación, colocación física y actualización de contadores.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario que coloca el barco
     * @param tipoBarco Tipo de barco a colocar
     * @param fila Fila de colocación
     * @param columna Columna de colocación
     * @param orientacion Orientación del barco
     * @return true si se colocó exitosamente, false en caso contrario
     */
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

    /**
     * Verifica si un usuario puede colocar un tipo específico de barco.
     * 
     * @param idPartida ID de la partida
     * @param usuario Usuario que quiere colocar el barco
     * @param tipoBarco Tipo de barco a verificar
     * @return true si puede colocar el barco, false en caso contrario
     */
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

    /**
     * Guarda una partida específica en el sistema de persistencia.
     * 
     * @param idPartida ID de la partida a guardar
     */
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

    /**
     * Registra una conexión activa asociada a un usuario.
     * 
     * @param nombreUsuario Nombre del usuario
     * @param conexion Objeto Connection del usuario
     */
    public static void registrarConexion(String nombreUsuario, Connection conexion) {
        conexionesActivas.put(nombreUsuario, conexion);
    }

    /**
     * Elimina una conexión del registro de conexiones activas.
     * 
     * @param nombreUsuario Nombre del usuario cuya conexión eliminar
     */
    public static void eliminarConexion(String nombreUsuario) {
        conexionesActivas.remove(nombreUsuario);
    }

    /**
     * Obtiene la conexión del rival de un usuario en una partida específica.
     * 
     * @param idPartida ID de la partida
     * @param usuarioActual Usuario del cual obtener el rival
     * @return Connection del rival o null si no existe
     */
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

    /**
     * Procesa un ataque de un usuario hacia su rival en una partida.
     * Ejecuta la lógica del ataque y retorna el resultado.
     * 
     * @param idPartida ID de la partida donde ocurre el ataque
     * @param atacante Usuario que realiza el ataque
     * @param fila Fila de la coordenada objetivo
     * @param columna Columna de la coordenada objetivo
     * @return Resultado del ataque (tocado, hundido, agua, error)
     */
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