package GUI;

import javax.swing.SwingUtilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;

public class ComunicacionServidor {
    
    private final DataInputStream entrada;
    private final DataOutputStream salida;
    private final AtomicBoolean conexionActiva;
    
    // CAMBIO: Una sola cola más simple para comandos
    private final BlockingQueue<ComandoPendiente> colaComandos;
    private final AtomicReference<ComandoPendiente> comandoActual;
    
    private final Thread threadEscucha;
    private final Thread threadEnvio;
    
    // Callback para mensajes no solicitados
    private Consumer<String> manejadorMensajes;
    private Consumer<String> manejadorErrores;
    
    public ComunicacionServidor(DataInputStream entrada, DataOutputStream salida) {
        this.entrada = entrada;
        this.salida = salida;
        this.conexionActiva = new AtomicBoolean(true);
        this.colaComandos = new LinkedBlockingQueue<>();
        this.comandoActual = new AtomicReference<>();
        
        // Iniciar threads de comunicación
        this.threadEscucha = new Thread(this::escucharServidor, "Thread-Escucha");
        this.threadEnvio = new Thread(this::procesarComandos, "Thread-Envio");
        
        // Esperar confirmación inicial del servidor
        esperarConfirmacionInicial();
    }
    
    private void esperarConfirmacionInicial() {
        new Thread(() -> {
            try {
                // Esperar mensaje de confirmación o bienvenida
                String mensaje = entrada.readUTF();
                System.out.println("[INIT] Mensaje inicial del servidor: " + mensaje);
                
                // Iniciar threads después de la confirmación
                threadEscucha.start();
                threadEnvio.start();
                
            } catch (IOException e) {
                System.err.println("[INIT] Error leyendo confirmación inicial: " + e.getMessage());
                conexionActiva.set(false);
            }
        }, "Thread-Confirmacion").start();
    }
    
    public void iniciarEscucha(Consumer<String> onMensaje, Consumer<String> onError) {
        this.manejadorMensajes = onMensaje;
        this.manejadorErrores = onError;
    }
    
    // Thread dedicado SOLO para escuchar mensajes del servidor
    private void escucharServidor() {
        System.out.println("[ESCUCHA] Thread de escucha iniciado");
        
        while (conexionActiva.get()) {
            try {
                if (entrada.available() > 0) {
                    String mensaje = entrada.readUTF();
                    System.out.println("[ESCUCHA] Mensaje recibido: " + mensaje);
                    
                    // CORRECCIÓN CRÍTICA: Primero manejar mensajes específicos de inicio de partida
                    if (mensaje.startsWith("rival_encontrado:") ||
                        mensaje.startsWith("partida_lista:") ||
                        mensaje.startsWith("turno_colocacion:")) {
                        
                        System.out.println("[ESCUCHA] Procesando mensaje de inicio partida: " + mensaje);
                        // Pasar al manejador en el thread de UI
                        if (manejadorMensajes != null) {
                            final String mensajeFinal = mensaje;
                            SwingUtilities.invokeLater(() -> manejadorMensajes.accept(mensajeFinal));
                        } else {
                            System.out.println("[ESCUCHA] No hay manejador para mensaje: " + mensaje);
                        }
                        
                        // Continuar al siguiente mensaje
                        continue;
                    }
                    
                    // PROCESO NORMAL: Verificar si hay comando esperando respuesta
                    ComandoPendiente comando = comandoActual.get();
                    
                    if (comando != null && !comando.estaCompletado()) {
                        // Es respuesta a comando pendiente
                        System.out.println("[ESCUCHA] Procesando como respuesta a: " + comando.getComando());
                        comando.procesarRespuesta(mensaje);
                        comandoActual.set(null); // Limpiar comando actual
                    } else {
                        // Es mensaje no solicitado (evento del servidor)
                        System.out.println("[ESCUCHA] Procesando como mensaje no solicitado");
                        if (manejadorMensajes != null) {
                            final String mensajeFinal = mensaje;
                            SwingUtilities.invokeLater(() -> manejadorMensajes.accept(mensajeFinal));
                        }
                    }
                }
                
                // Pequeña pausa para no saturar CPU
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                System.out.println("[ESCUCHA] Thread de escucha interrumpido");
                break;
            } catch (IOException e) {
                if (conexionActiva.get()) {
                    System.err.println("[ESCUCHA] Error en thread de escucha: " + e.getMessage());
                    e.printStackTrace();
                    if (manejadorErrores != null) {
                        final String errorMsg = e.getMessage();
                        SwingUtilities.invokeLater(() -> manejadorErrores.accept(errorMsg));
                    }
                }
                break;
            }
        }
        
        System.out.println("[ESCUCHA] Thread de escucha terminado");
    }
    
    // Thread dedicado SOLO para enviar comandos
    private void procesarComandos() {
        System.out.println("[ENVIO] Thread de envío iniciado");
        
        while (conexionActiva.get()) {
            try {
                ComandoPendiente comando = colaComandos.take(); // Bloquea hasta que hay comando
                
                System.out.println("[ENVIO] Procesando comando: " + comando.getComando());
                
                // Establecer como comando actual ANTES de enviar
                comandoActual.set(comando);
                
                // Enviar comando al servidor
                synchronized(salida) {
                    salida.writeUTF(comando.getComando());
                    
                    // Si hay parámetros adicionales, enviarlos
                    for (String parametro : comando.getParametros()) {
                        salida.writeUTF(parametro);
                        System.out.println("[ENVIO] Parámetro enviado: " + parametro);
                    }
                    
                    salida.flush();
                }
                
                System.out.println("[ENVIO] Comando enviado: " + comando.getComando());
                comando.marcarEnviado();
                
                // IMPORTANTE: Pausa pequeña para evitar saturación
                Thread.sleep(50);
                
            } catch (InterruptedException e) {
                System.out.println("[ENVIO] Thread de envío interrumpido");
                break;
            } catch (IOException e) {
                System.err.println("[ENVIO] Error enviando comando: " + e.getMessage());
                
                // Limpiar comando actual en caso de error
                ComandoPendiente comandoFallido = comandoActual.getAndSet(null);
                if (comandoFallido != null) {
                    comandoFallido.procesarError("Error de comunicación: " + e.getMessage());
                }
                
                if (manejadorErrores != null) {
                    SwingUtilities.invokeLater(() -> manejadorErrores.accept(e.getMessage()));
                }
                break;
            }
        }
        
        System.out.println("[ENVIO] Thread de envío terminado");
    }
    
    public void enviarComando(String comando, Consumer<String> callback) {
        enviarComandoConParametros(comando, new String[0], callback);
    }
    
    public void enviarComandoConParametro(String comando, String parametro, Consumer<String> callback) {
        enviarComandoConParametros(comando, new String[]{parametro}, callback);
    }
    
    public void enviarComandoConParametros(String comando, String[] parametros, Consumer<String> callback) {
        if (!conexionActiva.get()) {
            System.err.println("[ENVIO] Conexión no activa - no se puede enviar: " + comando);
            SwingUtilities.invokeLater(() -> callback.accept("ERROR: Conexión no activa"));
            return;
        }
        
        ComandoPendiente comandoPendiente = new ComandoPendiente(comando, parametros, callback);
        
        try {
            boolean agregado = colaComandos.offer(comandoPendiente);
            if (agregado) {
                System.out.println("[COLA] Comando añadido a cola: " + comando);
            } else {
                System.err.println("[COLA] No se pudo añadir comando a cola: " + comando);
                SwingUtilities.invokeLater(() -> callback.accept("ERROR: Cola de comandos llena"));
            }
        } catch (Exception e) {
            System.err.println("[COLA] Error añadiendo comando a cola: " + e.getMessage());
            SwingUtilities.invokeLater(() -> callback.accept("ERROR: " + e.getMessage()));
        }
    }
    
    // REEMPLAZAR el método:
    public void enviarColocacionBarco(ColocacionBarco colocacion, 
                                     Consumer<String> callback, 
                                     Consumer<String> errorCallback) {
        System.out.println("[DEBUG] Enviando colocación: " + colocacion);
        
        new Thread(() -> {
            try {
                // Bloquear para evitar interferencias con otros threads
                synchronized(this) {
                    // 1. Enviar comando y parámetros
                    salida.writeUTF("colocar_barco");
                    salida.writeUTF(colocacion.getTipoBarco());
                    salida.writeUTF(String.valueOf(colocacion.getFila()));
                    salida.writeUTF(String.valueOf(colocacion.getColumna()));
                    salida.writeUTF(colocacion.getOrientacion());
                    salida.flush();
                    System.out.println("[DEBUG] Comando y parámetros enviados");
                    
                    // 2. Leer AMBAS respuestas en secuencia
                    String respuesta1 = entrada.readUTF(); // Primera respuesta (barco_colocado)
                    System.out.println("[DEBUG] Primera respuesta: " + respuesta1);
                    
                    String respuesta2 = entrada.readUTF(); // Segunda respuesta (barcos_restantes)
                    System.out.println("[DEBUG] Segunda respuesta: " + respuesta2);
                    
                    // 3. Procesar respuestas en thread de UI
                    final String r1 = respuesta1;
                    final String r2 = respuesta2;
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // Procesar primera respuesta (barco_colocado)
                            if (r1.startsWith("barco_colocado:")) {
                                callback.accept(r1);
                                
                                // Luego procesar segunda respuesta (barcos_restantes)
                                if (manejadorMensajes != null) {
                                    manejadorMensajes.accept(r2);
                                }
                            } else if (r1.startsWith("error_colocacion:")) {
                                // Error en la colocación
                                errorCallback.accept(r1.substring(17));
                            } else {
                                // Respuesta inesperada
                                System.err.println("[ERROR] Respuesta inesperada: " + r1);
                                errorCallback.accept("Respuesta inesperada: " + r1);
                            }
                        } catch (Exception e) {
                            System.err.println("[ERROR] Error procesando respuestas: " + e.getMessage());
                            errorCallback.accept("Error: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error en colocación: " + e.getMessage());
                e.printStackTrace();
                
                SwingUtilities.invokeLater(() -> {
                    errorCallback.accept("Error de comunicación: " + e.getMessage());
                });
            }
        }, "Thread-ColocacionBarco").start();
    }
    
    // REEMPLAZAR el método leerSiguienteMensaje:
    public void leerSiguienteMensaje(Consumer<String> callback) {
        System.out.println("[LECTURA] Esperando mensaje adicional...");
        
        // Crear thread para no bloquear UI
        new Thread(() -> {
            try {
                // Esperar brevemente para asegurarnos que el servidor responda
                Thread.sleep(200);
                
                // Verificar si hay datos disponibles
                if (entrada.available() > 0) {
                    String mensaje = entrada.readUTF();
                    System.out.println("[LECTURA] Mensaje recibido: " + mensaje);
                    
                    // Procesar en thread de UI
                    SwingUtilities.invokeLater(() -> {
                        try {
                            callback.accept(mensaje);
                        } catch (Exception e) {
                            System.err.println("[ERROR] Error en callback: " + e.getMessage());
                        }
                    });
                } else {
                    System.out.println("[LECTURA] No hay datos disponibles para leer");
                    SwingUtilities.invokeLater(() -> {
                        callback.accept("error:No hay mensaje disponible");
                    });
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error leyendo mensaje: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    callback.accept("error:" + e.getMessage());
                });
            }
        }, "Thread-LecturaMensaje").start();
    }
    
    public boolean estaConectado() {
        return conexionActiva.get();
    }
    
    public void cerrar() {
        System.out.println("[CIERRE] Cerrando comunicación...");
        conexionActiva.set(false);
        
        try {
            if (salida != null) {
                salida.writeUTF("termina_servicio");
                salida.flush();
            }
        } catch (IOException e) {
            System.err.println("[CIERRE] Error enviando comando de cierre: " + e.getMessage());
        }
        
        // Interrumpir threads
        if (threadEscucha != null && threadEscucha.isAlive()) {
            threadEscucha.interrupt();
        }
        if (threadEnvio != null && threadEnvio.isAlive()) {
            threadEnvio.interrupt();
        }
        
        System.out.println("[CIERRE] Comunicación cerrada");
    }
    
    // Clase interna mejorada para manejar comandos pendientes
    private static class ComandoPendiente {
        private final String comando;
        private final String[] parametros;
        private final Consumer<String> callback;
        private volatile boolean enviado = false;
        private volatile boolean completado = false;
        
        public ComandoPendiente(String comando, String[] parametros, Consumer<String> callback) {
            this.comando = comando;
            this.parametros = parametros != null ? parametros : new String[0];
            this.callback = callback;
        }
        
        public String getComando() { return comando; }
        public String[] getParametros() { return parametros; }
        
        public void marcarEnviado() { 
            this.enviado = true; 
        }
        
        public boolean estaEnviado() { 
            return enviado; 
        }
        
        public boolean estaCompletado() { 
            return completado; 
        }
        
        public void procesarRespuesta(String respuesta) {
            if (!completado) {
                completado = true;
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(respuesta));
                }
            }
        }
        
        public void procesarError(String error) {
            if (!completado) {
                completado = true;
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept("ERROR: " + error));
                }
            }
        }
    }
}