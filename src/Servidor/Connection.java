package Servidor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import Cliente.Usuario;
import Partida.Partida;
import Tablero.Tablero;

class Connection extends Thread {
    
    private DataInputStream entrada;
    private DataOutputStream salida;
    private Socket clienteSocket;
    private Usuario usuarioActual;
    private String partidaActual;
    
    public Connection(Socket aClienteSocket) {
        try {
            clienteSocket = aClienteSocket;
            clienteSocket.setSoTimeout(30000);
            
            entrada = new DataInputStream(clienteSocket.getInputStream());
            salida = new DataOutputStream(clienteSocket.getOutputStream());
            usuarioActual = null;
            partidaActual = null;
            
        } catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            boolean fin = false;
            if (!autenticarUsuario()) {
                System.out.println("Autenticación fallida, cerrando conexión");
                return;
            }
            
            salida.writeUTF("ready_for_commands");
            salida.flush();
            
            Servidor.registrarConexion(usuarioActual.getName(), this);
            
            while(!fin && usuarioActual != null) {
                try {
                    String pedido = entrada.readUTF();
                    
                    switch (pedido) {
                        case "crear_partida":
                            crearNuevaPartida();
                            break;
                            
                        case "unirse_partida":
                            mostrarPartidasDisponibles();
                            break;
                            
                        case "seleccionar_partida":
                            String idPartida = entrada.readUTF();
                            unirseAPartidaSeleccionada(idPartida);
                            break;
                            
                        case "iniciar_colocacion":
                            iniciarFaseColocacion();
                            break;
                            
                        case "colocar_barco":
                            procesarColocacionBarco();
                            break;
                            
                        case "finalizar_colocacion":
                            finalizarColocacionBarcos();
                            break;
                            
                        case "estado_servidor":
                            salida.writeUTF(Servidor.obtenerEstadoServidor());
                            salida.flush();
                            break;
                            
                        case "termina_servicio":
                            fin = true;
                            break;
                            
                        case "comprobar_listo":
                            boolean ambosListos = Servidor.ambosJugadoresListos(partidaActual);
                            if (ambosListos) {
                                enviarPartidaReady();
                            } else {
                                salida.writeUTF("aun_esperando:El rival aún no ha terminado.");
                                salida.flush();
                            }
                            break;
                            
                        case "atacar":
                            procesarAtaque();
                            break;
                            
                        case "quien_empieza":
                            if (Servidor.esTurnoDeUsuario(partidaActual, usuarioActual)) {
                                salida.writeUTF("tu_turno");
                            } else {
                                salida.writeUTF("turno_rival");
                            }
                            salida.flush();
                            break;
                            
                        default:
                            System.err.println("[ERROR] Comando no reconocido: " + pedido);
                            salida.writeUTF("error:Comando no reconocido: " + pedido);
                            salida.flush();
                            break;
                    }
                    
                    Thread.sleep(10);
                    
                } catch (java.net.SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    System.out.println("Error leyendo comando: " + e.getMessage());
                    break;
                } catch (InterruptedException e) {
                    System.out.println("Thread interrumpido");
                    break;
                }
            }
        
        } catch (Exception e) {
            System.out.println("Error en conexión: " + e.getMessage());
            e.printStackTrace();
        } finally { 
            limpiarRecursos();
        }
    }
    
    public void setUsuarioAutenticado(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    private boolean autenticarUsuario() throws IOException {
        try {
            salida.writeUTF("auth_required");
            salida.flush();
            
            String tipoAuth = entrada.readUTF();
            String nombre = entrada.readUTF();
            String contraseña = entrada.readUTF();
            
            if ("registro".equals(tipoAuth)) {
                return procesarRegistro(nombre, contraseña);
            } else if ("login".equals(tipoAuth)) {
                return procesarLogin(nombre, contraseña);
            }
            
            salida.writeUTF("auth_error:Tipo de autenticación no válido");
            salida.flush();
            return false;
            
        } catch (IOException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            throw e;
        }
    }
    
    private boolean procesarRegistro(String nombre, String contraseña) throws IOException {
        if (Servidor.registrarUsuario(nombre, contraseña)) {
            usuarioActual = new Usuario(nombre, contraseña);
            Servidor.conectarUsuario(nombre, usuarioActual);
            salida.writeUTF("auth_success:Usuario registrado correctamente");
            return true;
        } else {
            salida.writeUTF("auth_error:El usuario ya existe");
            return false;
        }
    }
    
    private boolean procesarLogin(String nombre, String contraseña) throws IOException {
        Usuario usuario = Servidor.validarUsuario(nombre, contraseña);
        if (usuario != null) {
            if (Servidor.usuarioConectado(nombre)) {
                salida.writeUTF("auth_error:Usuario ya conectado");
                return false;
            }
            
            usuarioActual = usuario;
            Servidor.conectarUsuario(nombre, usuarioActual);
            salida.writeUTF("auth_success:Login exitoso");
            return true;
        } else {
            salida.writeUTF("auth_error:Credenciales incorrectas");
            return false;
        }
    }
    
    private void mostrarMenuPrincipal() throws IOException {
        String menu = "=== HUNDIR LA FLOTA ===\n" +
                     "Bienvenido: " + usuarioActual.getName() + "\n" +
                     "1. Crear nueva partida\n" +
                     "2. Unirse a partida existente\n" +
                     "3. Ver estado del servidor\n" +
                     "4. Salir";
        
        salida.writeUTF("menu:" + menu);
    }
    
    private void crearNuevaPartida() throws IOException {
        String idPartida = Servidor.crearPartida(usuarioActual);
        partidaActual = idPartida;
        
        salida.writeUTF("partida_creada:" + idPartida);
        salida.flush();
        
        salida.writeUTF("esperando_rival:Esperando que se una otro jugador...");
        salida.flush();
        
        esperarRival(idPartida);
    }
    
    private void mostrarPartidasDisponibles() throws IOException {
        List<String> partidas = Servidor.obtenerPartidasDisponibles();
        
        if (partidas.isEmpty()) {
            salida.writeUTF("no_partidas:No hay partidas disponibles");
        } else {
            String partidasStr = String.join("|", partidas);
            salida.writeUTF("partidas_disponibles:" + partidasStr);
        }
        salida.flush();
    }
    
    private void unirseAPartidaSeleccionada(String idPartida) throws IOException {
        
        if (Servidor.unirseAPartida(idPartida, usuarioActual)) {
            partidaActual = idPartida;
            salida.writeUTF("unido_exitoso:Te has unido a la partida " + idPartida);
            salida.flush();
            
            Partida partida = Servidor.obtenerPartida(idPartida);
            if (partida != null && partida.getUsuarioRival() != null) {
                notificarPartidaCompleta(partida);
            } else {
                salida.writeUTF("esperando_rival:Esperando que se una otro jugador...");
                salida.flush();
                esperarRival(idPartida);
            }
        } else {
            salida.writeUTF("error:No se pudo unir a la partida - Puede estar completa o no existir");
            salida.flush();
        }
    }
    
    private void notificarPartidaCompleta(Partida partida) {
        try {
            String nombreRival = usuarioActual.equals(partida.getUsuarioPrincipal()) 
                                ? partida.getUsuarioRival().getName()
                                : partida.getUsuarioPrincipal().getName();
            
            
            salida.writeUTF("rival_encontrado:" + nombreRival);
            salida.flush();
            
            Thread.sleep(500);
            
            salida.writeUTF("partida_lista:Ambos jugadores conectados");
            salida.flush();
            
            Thread.sleep(500);
            
            salida.writeUTF("turno_colocacion:Puede empezar a colocar barcos");
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error notificando partida completa: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Error en pausa de notificación: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    private void esperarRival(String idPartida) {
        new Thread(() -> {
            try {
                int intentos = 0;
                final int MAX_INTENTOS = 150;
                
                while (intentos < MAX_INTENTOS) {
                    Thread.sleep(2000);
                    intentos++;
                    
                    Partida partida = Servidor.obtenerPartida(idPartida);
                    if (partida == null) {
                        System.err.println("Partida eliminada mientras se esperaba rival");
                        salida.writeUTF("error:Partida no encontrada");
                        salida.flush();
                        break;
                    }
                    
                    if (partida.getUsuarioRival() != null) {
                        System.out.println("Rival encontrado para partida " + idPartida);
                        notificarPartidaCompleta(partida);
                        break;
                    }
                }
                
                if (intentos >= MAX_INTENTOS) {
                    salida.writeUTF("error:Tiempo de espera agotado (5 minutos)");
                    salida.flush();
                }
                
            } catch (Exception e) {
                System.err.println("Error esperando rival: " + e.getMessage());
                try {
                    salida.writeUTF("error:Error interno esperando rival");
                    salida.flush();
                } catch (IOException ex) {
                    System.err.println("Error enviando mensaje de error: " + ex.getMessage());
                }
            }
        }, "EsperarRival-" + idPartida).start();
    }
    
    private void iniciarFaseColocacion() throws IOException {
        if (partidaActual == null) {
            salida.writeUTF("error:No estás en ninguna partida");
            salida.flush();
            return;
        }
        
        Partida partida = Servidor.obtenerPartida(partidaActual);
        if (partida == null) {
            salida.writeUTF("error:Partida no encontrada");
            salida.flush();
            return;
        }
        
        if (partida.getUsuarioRival() == null) {
            salida.writeUTF("error:Esperando rival para iniciar colocación");
            salida.flush();
            return;
        }
        
        try {
            salida.writeUTF("colocacion_activa:Puede colocar barcos");
            salida.flush();
            
            Thread.sleep(200);
            
            enviarInstruccionesColocacion();
            
        } catch (InterruptedException e) {
            System.err.println("Error en pausa de inicialización: " + e.getMessage());
            Thread.currentThread().interrupt();
            enviarInstruccionesColocacion();
        }
    }
    
    private void enviarInstruccionesColocacion() throws IOException {
        String instrucciones = "Portaviones:1, Submarinos:2, Destructores:3, Fragatas:4";
        salida.writeUTF("instrucciones:" + instrucciones);
        salida.flush();
    }
    
    private void procesarColocacionBarco() throws IOException {
        if (partidaActual == null) {
            salida.writeUTF("error_colocacion:No estás en ninguna partida");
            salida.flush();
            return;
        }

        String tipoBarco = entrada.readUTF();
        String filaStr = entrada.readUTF();
        String columnaStr = entrada.readUTF();
        String orientacion = entrada.readUTF();

        int fila = Integer.parseInt(filaStr);
        int columna = Integer.parseInt(columnaStr);

        if (!Servidor.puedeColocarBarco(partidaActual, usuarioActual, tipoBarco)) {
            salida.writeUTF("error_colocacion:Límite alcanzado para " + tipoBarco);
            salida.flush();
            return;
        }

        boolean colocado = Servidor.colocarBarco(partidaActual, usuarioActual, tipoBarco, fila, columna, orientacion);

        if (colocado) {
            salida.writeUTF("barco_colocado:Barco " + tipoBarco + " colocado correctamente en (" + fila + "," + columna + ")");
            salida.flush();

            String restantes = Servidor.obtenerBarcosRestantes(partidaActual, usuarioActual);
            salida.writeUTF("barcos_restantes:" + restantes);
            salida.flush();

        } else {
            salida.writeUTF("error_colocacion:No se pudo colocar el barco en esa posición - Posición ocupada o inválida");
            salida.flush();
        }
    }
    
    private boolean esEntradaValida(String tipoBarco, int fila, int columna, String orientacion) {
        if (tipoBarco == null || 
            (!tipoBarco.equals("PORTAVIONES") && !tipoBarco.equals("SUBMARINO") && 
             !tipoBarco.equals("DESTRUCTOR") && !tipoBarco.equals("FRAGATA"))) {
            return false;
        }
        
        if (fila < 0 || fila >= 8 || columna < 0 || columna >= 8) {
            return false;
        }
        
        if (orientacion == null || 
            (!orientacion.equals("HORIZONTAL") && !orientacion.equals("VERTICAL"))) {
            return false;
        }
        
        return true;
    }
    
    private void verificarBarcosRestantes() throws IOException {
        try {
            boolean completado = false;
            String restantes = "";
            
            try {
                completado = Servidor.usuarioCompletoColocacion(partidaActual, usuarioActual);
            } catch (Exception e) {
                System.err.println("Método usuarioCompletoColocacion no implementado: " + e.getMessage());
                completado = false;
            }
            
            if (completado) {
                salida.writeUTF("colocacion_completa:Has colocado todos tus barcos");
                salida.flush(); 
            } else {
                try {
                    restantes = Servidor.obtenerBarcosRestantes(partidaActual, usuarioActual);
                    salida.writeUTF("barcos_restantes:" + restantes);
                    salida.flush(); 
                } catch (Exception e) {
                    System.err.println("Método obtenerBarcosRestantes no implementado: " + e.getMessage());
                    salida.writeUTF("barcos_restantes:Continúe colocando barcos");
                    salida.flush();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando barcos restantes: " + e.getMessage());
            salida.writeUTF("barcos_restantes:Error verificando estado - Continúe colocando");
            salida.flush();
        }
    }
    
    private void finalizarColocacionBarcos() throws IOException {
        if (partidaActual == null) {
            salida.writeUTF("error:No estás en ninguna partida");
            salida.flush();
            return;
        }
        
        Partida partida = Servidor.obtenerPartida(partidaActual);
        if (partida != null) {
        partida.inicializarTurno(); 
        }
        
        if (partida == null) {
            salida.writeUTF("error:Partida no encontrada");
            salida.flush();
            return;
        }
        
        try {
            boolean finalizado = false;
            boolean ambosListos = false;
            
            try {
                finalizado = Servidor.finalizarColocacionUsuario(partidaActual, usuarioActual);
            } catch (Exception e) {
                System.err.println("Método finalizarColocacionUsuario no implementado: " + e.getMessage());
                finalizado = true;
            }
            
            if (finalizado) {
                try {
                    ambosListos = Servidor.ambosJugadoresListos(partidaActual);
                } catch (Exception e) {
                    System.err.println("Método ambosJugadoresListos no implementado: " + e.getMessage());
                    ambosListos = false;
                }
                
                if (ambosListos) {
                    salida.writeUTF("partida_ready:Ambos jugadores listos - ¡Comienza la batalla!");
                } else {
                    salida.writeUTF("colocacion_finalizada:Esperando que el rival termine de colocar...");
                }
            } else {
                salida.writeUTF("error:No se pudo finalizar - Faltan barcos por colocar");
                
                try {
                    String restantes = Servidor.obtenerBarcosRestantes(partidaActual, usuarioActual);
                    salida.writeUTF("barcos_restantes:" + restantes);
                } catch (Exception e) {
                    salida.writeUTF("barcos_restantes:Verifique que todos los barcos estén colocados");
                }
            }
            salida.flush();
            
        } catch (Exception e) {
            System.err.println("Error finalizando colocación: " + e.getMessage());
            salida.writeUTF("error:Error interno finalizando colocación: " + e.getMessage());
            salida.flush();
        }
    }
    
    private void procesarFinalizarColocacion() throws IOException {
        boolean completo = Servidor.finalizarColocacionUsuario(partidaActual, usuarioActual);

        if (!completo) {
            salida.writeUTF("error_colocacion:No has colocado todos los barcos");
            salida.flush();
            return;
        }

        boolean ambosListos = Servidor.ambosJugadoresListos(partidaActual);
        
        if (ambosListos) {
            Servidor.guardarPartida(partidaActual);

            Partida partida = Servidor.obtenerPartida(partidaActual);
            if (partida != null) {
                partida.inicializarTurno();
            }

            Connection conn1 = this;
            Connection conn2 = Servidor.getConexionRival(partidaActual, usuarioActual);

            if (conn1 != null) {
                conn1.enviarPartidaReady();
            }
            if (conn2 != null) {
                conn2.enviarPartidaReady();
            }
        } else {
            salida.writeUTF("colocacion_finalizada:Esperando que el rival termine de colocar...");
            salida.flush();
        }
    }

    public void enviarPartidaReady() throws IOException {
        salida.writeUTF("partida_ready:Ambos jugadores listos - ¡Comienza la batalla!");
        salida.flush();
    }
    
    private void limpiarRecursos() {
        
        if (usuarioActual != null) {
            try {
                Servidor.desconectarUsuario(usuarioActual.getName());
            } catch (Exception e) {
                System.err.println("Error desconectando usuario: " + e.getMessage());
            }
        }
        
        try {
            if (entrada != null) {
                entrada.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando entrada: " + e.getMessage());
        }
        
        try {
            if (salida != null) {
                salida.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando salida: " + e.getMessage());
        }
        
        try {
            if (clienteSocket != null && !clienteSocket.isClosed()) {
                clienteSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando socket: " + e.getMessage());
        }
        
        try {
            Servidor.eliminarConexion(usuarioActual.getName());
        } catch (Exception e) {
            System.err.println("Error eliminando conexión: " + e.getMessage());
        }
    }
    
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public DataOutputStream getSalida() {
        return salida;
    }

    public String getPartidaActual() {
        return partidaActual;
    }

    private void procesarAtaque() throws IOException {
        String coords = entrada.readUTF();
        String[] partes = coords.split(",");
        int fila = Integer.parseInt(partes[0]);
        int columna = Integer.parseInt(partes[1]);

        if (!Servidor.esTurnoDeUsuario(partidaActual, usuarioActual)) {
            salida.writeUTF("error:No es tu turno");
            salida.flush();
            return;
        }

        String resultado = Servidor.procesarAtaque(partidaActual, usuarioActual, fila, columna);

        salida.writeUTF("resultado_ataque:" + resultado);
        salida.flush();

        Connection rivalConn = Servidor.getConexionRival(partidaActual, usuarioActual);
        if (rivalConn != null) {
            rivalConn.notificarAtaqueRecibido(fila, columna, resultado);
        }

        Partida partida = Servidor.obtenerPartida(partidaActual);
        if (partida != null) {
            Usuario rival = usuarioActual.equals(partida.getUsuarioPrincipal())
                ? partida.getUsuarioRival()
                : partida.getUsuarioPrincipal();
            Tablero tableroRival = partida.getTableroJugador(rival);
            if (tableroRival != null && tableroRival.todosBarcosHundidos()) {
                salida.writeUTF("fin_partida:¡Has ganado! Todos los barcos rivales han sido hundidos.");
                salida.flush();
                if (rivalConn != null) {
                    rivalConn.getSalida().writeUTF("fin_partida:¡Has perdido! Todos tus barcos han sido hundidos.");
                    rivalConn.getSalida().flush();
                }
                Servidor.finalizarPartida(partidaActual);
                return;
            }
        }

        Servidor.cambiarTurno(partidaActual);
    }

    public void notificarAtaqueRecibido(int fila, int columna, String resultado) {
        try {
            salida.writeUTF("ataque_recibido:" + fila + "," + columna + "," + resultado);
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error notificando ataque recibido al rival: " + e.getMessage());
        }
    }
}