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
            // IMPORTANTE: Configurar timeout para evitar bloqueos
            clienteSocket.setSoTimeout(30000); // 30 segundos timeout
            
            entrada = new DataInputStream(clienteSocket.getInputStream());
            salida = new DataOutputStream(clienteSocket.getOutputStream());
            usuarioActual = null;
            partidaActual = null;
            
            System.out.println("Nueva conexión desde: " + aClienteSocket.getRemoteSocketAddress());
            
        } catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            boolean fin = false;
            
            // Autenticar UNA SOLA VEZ al inicio
            if (!autenticarUsuario()) {
                System.out.println("Autenticación fallida, cerrando conexión");
                return;
            }
            
            System.out.println("Usuario " + usuarioActual.getName() + " autenticado correctamente");
            
            // IMPORTANTE: Enviar confirmación de que está listo
            salida.writeUTF("ready_for_commands");
            salida.flush(); // IMPORTANTE
            
            // Registrar conexión
            Servidor.registrarConexion(usuarioActual.getName(), this);
            
            while(!fin && usuarioActual != null) {
                try {
                    String pedido = entrada.readUTF();
                    System.out.println("[COMANDO] Usuario " + usuarioActual.getName() + " envió: " + pedido);
                    
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
                            salida.flush(); // IMPORTANTE
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
                            System.out.println("[DEBUG] quien_empieza recibido de " + usuarioActual.getName());
                            if (Servidor.esTurnoDeUsuario(partidaActual, usuarioActual)) {
                                salida.writeUTF("tu_turno");
                                System.out.println("[DEBUG] Respondiendo tu_turno a " + usuarioActual.getName());
                            } else {
                                salida.writeUTF("turno_rival");
                                System.out.println("[DEBUG] Respondiendo turno_rival a " + usuarioActual.getName());
                            }
                            salida.flush();
                            break;
                            
                        default:
                            System.err.println("[ERROR] Comando no reconocido: " + pedido);
                            salida.writeUTF("error:Comando no reconocido: " + pedido);
                            salida.flush(); // IMPORTANTE
                            break;
                    }
                    
                    // IMPORTANTE: Pequeña pausa para evitar saturación
                    Thread.sleep(10);
                    
                } catch (java.net.SocketTimeoutException e) {
                    // Timeout normal - continuar
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
            
            System.out.println("Autenticando: " + tipoAuth + " para " + nombre);
            
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
        
        System.out.println("Partida creada: " + idPartida + " por " + usuarioActual.getName());
        
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
        salida.flush(); // IMPORTANTE
    }
    
    private void unirseAPartidaSeleccionada(String idPartida) throws IOException {
        System.out.println("Usuario " + usuarioActual.getName() + " intenta unirse a partida: " + idPartida);
        
        if (Servidor.unirseAPartida(idPartida, usuarioActual)) {
            partidaActual = idPartida;
            salida.writeUTF("unido_exitoso:Te has unido a la partida " + idPartida);
            salida.flush();
            
            System.out.println("Usuario " + usuarioActual.getName() + " unido exitosamente a partida " + idPartida);
            
            // Verificar si la partida está completa
            Partida partida = Servidor.obtenerPartida(idPartida);
            if (partida != null && partida.getUsuarioRival() != null) {
                // Partida completa - iniciar fase de colocación
                notificarPartidaCompleta(partida);
            } else {
                // Esperando rival
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
            
            System.out.println("Notificando partida completa a " + usuarioActual.getName() + " - rival: " + nombreRival);
            
            salida.writeUTF("rival_encontrado:" + nombreRival);
            salida.flush();
            
            Thread.sleep(500); // Pausa para procesar mensaje
            
            salida.writeUTF("partida_lista:Ambos jugadores conectados");
            salida.flush();
            
            Thread.sleep(500); // Pausa para procesar mensaje
            
            salida.writeUTF("turno_colocacion:Puede empezar a colocar barcos");
            salida.flush();
            
            System.out.println("Notificación enviada correctamente a " + usuarioActual.getName());
            
        } catch (IOException e) {
            System.err.println("Error notificando partida completa: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Error en pausa de notificación: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restaurar estado de interrupción
        }
    }
    
    private void esperarRival(String idPartida) {
        new Thread(() -> {
            try {
                int intentos = 0;
                final int MAX_INTENTOS = 150; // 5 minutos
                
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
            // Confirmar que puede colocar barcos
            salida.writeUTF("colocacion_activa:Puede colocar barcos");
            salida.flush();
            
            Thread.sleep(200);
            
            // Enviar instrucciones
            enviarInstruccionesColocacion();
            
        } catch (InterruptedException e) {
            System.err.println("Error en pausa de inicialización: " + e.getMessage());
            Thread.currentThread().interrupt();
            // Continuar sin la pausa
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

        // --- COMPROBAR LÍMITE ANTES DE COLOCAR ---
        if (!Servidor.puedeColocarBarco(partidaActual, usuarioActual, tipoBarco)) {
            salida.writeUTF("error_colocacion:Límite alcanzado para " + tipoBarco);
            salida.flush();
            return;
        }

        boolean colocado = Servidor.colocarBarco(partidaActual, usuarioActual, tipoBarco, fila, columna, orientacion);

        if (colocado) {
            salida.writeUTF("barco_colocado:Barco " + tipoBarco + " colocado correctamente en (" + fila + "," + columna + ")");
            salida.flush();

            // SIEMPRE enviar barcos_restantes después de cada colocación
            String restantes = Servidor.obtenerBarcosRestantes(partidaActual, usuarioActual);
            salida.writeUTF("barcos_restantes:" + restantes);
            salida.flush();

            // (Opcional) Si quieres, puedes dejar verificarBarcosRestantes solo para la lógica de finalización
        } else {
            salida.writeUTF("error_colocacion:No se pudo colocar el barco en esa posición - Posición ocupada o inválida");
            salida.flush();
        }
    }
    
    private boolean esEntradaValida(String tipoBarco, int fila, int columna, String orientacion) {
        // Validar tipo de barco
        if (tipoBarco == null || 
            (!tipoBarco.equals("PORTAVIONES") && !tipoBarco.equals("SUBMARINO") && 
             !tipoBarco.equals("DESTRUCTOR") && !tipoBarco.equals("FRAGATA"))) {
            System.err.println("Tipo de barco inválido: " + tipoBarco);
            return false;
        }
        
        // Validar coordenadas
        if (fila < 0 || fila >= 8 || columna < 0 || columna >= 8) {
            System.err.println("Coordenadas fuera de rango: (" + fila + "," + columna + ")");
            return false;
        }
        
        // Validar orientación
        if (orientacion == null || 
            (!orientacion.equals("HORIZONTAL") && !orientacion.equals("VERTICAL"))) {
            System.err.println("Orientación inválida: " + orientacion);
            return false;
        }
        
        return true;
    }
    
    private void verificarBarcosRestantes() throws IOException {
        try {
            // Verificar si los métodos existen en Servidor
            boolean completado = false;
            String restantes = "";
            
            try {
                completado = Servidor.usuarioCompletoColocacion(partidaActual, usuarioActual);
            } catch (Exception e) {
                System.err.println("Método usuarioCompletoColocacion no implementado: " + e.getMessage());
                // Fallback: asumir no completado por ahora
                completado = false;
            }
            
            if (completado) {
                salida.writeUTF("colocacion_completa:Has colocado todos tus barcos");
                salida.flush(); // ASEGURARSE DE HACER FLUSH INMEDIATAMENTE
            } else {
                try {
                    restantes = Servidor.obtenerBarcosRestantes(partidaActual, usuarioActual);
                    salida.writeUTF("barcos_restantes:" + restantes);
                    salida.flush(); // ASEGURARSE DE HACER FLUSH INMEDIATAMENTE
                } catch (Exception e) {
                    System.err.println("Método obtenerBarcosRestantes no implementado: " + e.getMessage());
                    // Fallback: mensaje genérico
                    salida.writeUTF("barcos_restantes:Continúe colocando barcos");
                    salida.flush(); // ASEGURARSE DE HACER FLUSH INMEDIATAMENTE
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando barcos restantes: " + e.getMessage());
            salida.writeUTF("barcos_restantes:Error verificando estado - Continúe colocando");
            salida.flush(); // ASEGURARSE DE HACER FLUSH INMEDIATAMENTE
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
        partida.inicializarTurno(); // El usuario principal empieza
        System.out.println("[DEBUG] Turno inicializado: " + partida.getTurnoActual().getName());
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
                // Fallback: asumir finalizado por ahora
                finalizado = true;
            }
            
            if (finalizado) {
                try {
                    ambosListos = Servidor.ambosJugadoresListos(partidaActual);
                } catch (Exception e) {
                    System.err.println("Método ambosJugadoresListos no implementado: " + e.getMessage());
                    // Fallback: asumir no están listos
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
        System.out.println("[DEBUG] procesarFinalizarColocacion llamado por: " + usuarioActual.getName());
        boolean completo = Servidor.finalizarColocacionUsuario(partidaActual, usuarioActual);

        System.out.println("[DEBUG] ¿Usuario ha colocado todos los barcos? " + completo);

        if (!completo) {
            System.out.println("[DEBUG] Usuario NO ha colocado todos los barcos: " + usuarioActual.getName());
            salida.writeUTF("error_colocacion:No has colocado todos los barcos");
            salida.flush();
            return;
        }

        boolean ambosListos = Servidor.ambosJugadoresListos(partidaActual);
        System.out.println("[DEBUG] ¿Ambos jugadores listos? " + ambosListos);

        if (ambosListos) {
            System.out.println("[DEBUG] Ambos listos, guardando partida y notificando a ambos jugadores.");
            Servidor.guardarPartida(partidaActual);

            // INICIALIZAR TURNO AQUÍ
            Partida partida = Servidor.obtenerPartida(partidaActual);
            if (partida != null) {
                partida.inicializarTurno(); // El usuario principal empieza
                System.out.println("[DEBUG] Turno inicializado: " + partida.getTurnoActual().getName());
            }

            Connection conn1 = this;
            Connection conn2 = Servidor.getConexionRival(partidaActual, usuarioActual);

            if (conn1 != null) {
                System.out.println("[DEBUG] Enviando partida_ready a " + usuarioActual.getName());
                conn1.enviarPartidaReady();
            }
            if (conn2 != null) {
                System.out.println("[DEBUG] Enviando partida_ready a rival: " + (conn2.getUsuarioActual() != null ? conn2.getUsuarioActual().getName() : "null"));
                conn2.enviarPartidaReady();
            } else {
                System.out.println("[DEBUG] No se encontró conexión del rival para notificar partida_ready.");
            }
        } else {
            System.out.println("[DEBUG] Solo este usuario ha terminado, esperando rival...");
            salida.writeUTF("colocacion_finalizada:Esperando que el rival termine de colocar...");
            salida.flush();
        }
    }

    // Método auxiliar para notificar al cliente
    public void enviarPartidaReady() throws IOException {
        salida.writeUTF("partida_ready:Ambos jugadores listos - ¡Comienza la batalla!");
        salida.flush();
    }
    
    private void limpiarRecursos() {
        System.out.println("Limpiando recursos para usuario: " + 
                          (usuarioActual != null ? usuarioActual.getName() : "desconocido"));
        
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
        
        // Eliminar conexión registrada
        try {
            Servidor.eliminarConexion(usuarioActual.getName());
        } catch (Exception e) {
            System.err.println("Error eliminando conexión: " + e.getMessage());
        }
        
        System.out.println("Recursos limpiados para conexión");
    }
    
    // AGREGAR getters necesarios (si no existen):
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
        // Leer coordenadas
        String coords = entrada.readUTF();
        String[] partes = coords.split(",");
        int fila = Integer.parseInt(partes[0]);
        int columna = Integer.parseInt(partes[1]);

        // Verificar turno
        if (!Servidor.esTurnoDeUsuario(partidaActual, usuarioActual)) {
            salida.writeUTF("error:No es tu turno");
            salida.flush();
            return;
        }

        // Realizar ataque
        String resultado = Servidor.procesarAtaque(partidaActual, usuarioActual, fila, columna);

        // Enviar resultado al atacante
        salida.writeUTF("resultado_ataque:" + resultado);
        salida.flush();

        // Notificar al rival (para actualizar su tablero)
        Connection rivalConn = Servidor.getConexionRival(partidaActual, usuarioActual);
        if (rivalConn != null) {
            rivalConn.notificarAtaqueRecibido(fila, columna, resultado);
        }

        // --- FINALIZAR PARTIDA SI TODOS LOS BARCOS DEL RIVAL HAN SIDO HUNDIDOS ---
        Partida partida = Servidor.obtenerPartida(partidaActual);
        if (partida != null) {
            Usuario rival = usuarioActual.equals(partida.getUsuarioPrincipal())
                ? partida.getUsuarioRival()
                : partida.getUsuarioPrincipal();
            Tablero tableroRival = partida.getTableroJugador(rival);
            if (tableroRival != null && tableroRival.todosBarcosHundidos()) {
                // Notificar a ambos jugadores
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

        // Cambiar turno si la partida no ha terminado
        Servidor.cambiarTurno(partidaActual);
    }

    public void notificarAtaqueRecibido(int fila, int columna, String resultado) {
        try {
            // Envía un mensaje al cliente rival con la información del ataque recibido
            salida.writeUTF("ataque_recibido:" + fila + "," + columna + "," + resultado);
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error notificando ataque recibido al rival: " + e.getMessage());
        }
    }
}