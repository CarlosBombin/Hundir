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

/**
 * Clase que gestiona la comunicación bidireccional con el servidor del juego.
 * Utiliza threads separados para envío y recepción de mensajes, garantizando
 * que la interfaz gráfica no se bloquee durante las operaciones de red.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ComunicacionServidor {
    
    /** Flujo de entrada de datos desde el servidor */
    private final DataInputStream entrada;
    /** Flujo de salida de datos hacia el servidor */
    private final DataOutputStream salida;
    /** Estado de la conexión con el servidor */
    private final AtomicBoolean conexionActiva;
    
    /** Cola thread-safe para comandos pendientes de envío */
    private final BlockingQueue<ComandoPendiente> colaComandos;
    /** Referencia al comando actualmente siendo procesado */
    private final AtomicReference<ComandoPendiente> comandoActual;
    
    /** Thread dedicado a escuchar mensajes del servidor */
    private final Thread threadEscucha;
    /** Thread dedicado a enviar comandos al servidor */
    private final Thread threadEnvio;
    
    /** Manejador de mensajes recibidos del servidor */
    private Consumer<String> manejadorMensajes;
    /** Manejador de errores de comunicación */
    private Consumer<String> manejadorErrores;
    
    /**
     * Constructor que inicializa la comunicación con el servidor.
     * Crea los threads de envío y recepción pero no los inicia hasta
     * recibir confirmación inicial del servidor.
     * 
     * @param entrada Flujo de entrada desde el servidor
     * @param salida Flujo de salida hacia el servidor
     */
    public ComunicacionServidor(DataInputStream entrada, DataOutputStream salida) {
        this.entrada = entrada;
        this.salida = salida;
        this.conexionActiva = new AtomicBoolean(true);
        this.colaComandos = new LinkedBlockingQueue<>();
        this.comandoActual = new AtomicReference<>();
        
        this.threadEscucha = new Thread(this::escucharServidor, "Thread-Escucha");
        this.threadEnvio = new Thread(this::procesarComandos, "Thread-Envio");
        
        esperarConfirmacionInicial();
    }
    
    /**
     * Espera el mensaje inicial del servidor antes de activar los threads.
     * Garantiza que la comunicación esté establecida correctamente.
     */
    private void esperarConfirmacionInicial() {
        new Thread(() -> {
            try {
                String mensaje = entrada.readUTF();
                System.out.println("[INIT] Mensaje inicial del servidor: " + mensaje);
                
                threadEscucha.start();
                threadEnvio.start();
                
            } catch (IOException e) {
                conexionActiva.set(false);
            }
        }, "Thread-Confirmacion").start();
    }
    
    /**
     * Configura los manejadores de mensajes y errores, e inicia la escucha.
     * 
     * @param onMensaje Callback para procesar mensajes del servidor
     * @param onError Callback para manejar errores de comunicación
     */
    public void iniciarEscucha(Consumer<String> onMensaje, Consumer<String> onError) {
        this.manejadorMensajes = onMensaje;
        this.manejadorErrores = onError;
    }
    
    /**
     * Thread principal de escucha de mensajes del servidor.
     * Procesa mensajes de forma continua hasta que se cierre la conexión.
     */
    private void escucharServidor() {
        
        while (conexionActiva.get()) {
            try {
                if (entrada.available() > 0) {
                    String mensaje = entrada.readUTF();
                    
                    // Mensajes que se procesan directamente sin esperar respuesta
                    if (mensaje.startsWith("rival_encontrado:") ||
                        mensaje.startsWith("partida_lista:") ||
                        mensaje.startsWith("turno_colocacion:")) {
                        
                        if (manejadorMensajes != null) {
                            final String mensajeFinal = mensaje;
                            SwingUtilities.invokeLater(() -> manejadorMensajes.accept(mensajeFinal));
                        }
                        
                        continue;
                    }
                    
                    // Procesar respuesta a comando pendiente
                    ComandoPendiente comando = comandoActual.get();
                    
                    if (comando != null && !comando.estaCompletado()) {
                        comando.procesarRespuesta(mensaje);
                        comandoActual.set(null);
                    } else {
                        // Mensaje no solicitado, enviar al manejador general
                        if (manejadorMensajes != null) {
                            final String mensajeFinal = mensaje;
                            SwingUtilities.invokeLater(() -> manejadorMensajes.accept(mensajeFinal));
                        }
                    }
                }
                
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                break;
            } catch (IOException e) {
                if (conexionActiva.get()) {
                    e.printStackTrace();
                    if (manejadorErrores != null) {
                        final String errorMsg = e.getMessage();
                        SwingUtilities.invokeLater(() -> manejadorErrores.accept(errorMsg));
                    }
                }
                break;
            }
        }
        
    }
    
    /**
     * Thread principal de envío de comandos al servidor.
     * Procesa la cola de comandos pendientes de forma secuencial.
     */
    private void procesarComandos() {
        
        while (conexionActiva.get()) {
            try {
                ComandoPendiente comando = colaComandos.take();
                
                comandoActual.set(comando);
                
                // Envío sincronizado para evitar conflictos
                synchronized(salida) {
                    salida.writeUTF(comando.getComando());
                    
                    for (String parametro : comando.getParametros()) {
                        salida.writeUTF(parametro);
                    }
                    
                    salida.flush();
                }
                
                comando.marcarEnviado();
                
                Thread.sleep(50);
                
            } catch (InterruptedException e) {
                break;
            } catch (IOException e) {
                
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
        
    }
    
    /**
     * Envía un comando simple sin parámetros al servidor.
     * 
     * @param comando Nombre del comando a enviar
     * @param callback Función a ejecutar cuando se reciba la respuesta
     */
    public void enviarComando(String comando, Consumer<String> callback) {
        enviarComandoConParametros(comando, new String[0], callback);
    }
    
    /**
     * Envía un comando con un parámetro al servidor.
     * 
     * @param comando Nombre del comando a enviar
     * @param parametro Parámetro del comando
     * @param callback Función a ejecutar cuando se reciba la respuesta
     */
    public void enviarComandoConParametro(String comando, String parametro, Consumer<String> callback) {
        enviarComandoConParametros(comando, new String[]{parametro}, callback);
    }
    
    /**
     * Envía un comando con múltiples parámetros al servidor.
     * 
     * @param comando Nombre del comando a enviar
     * @param parametros Array de parámetros del comando
     * @param callback Función a ejecutar cuando se reciba la respuesta
     */
    public void enviarComandoConParametros(String comando, String[] parametros, Consumer<String> callback) {
        if (!conexionActiva.get()) {
            SwingUtilities.invokeLater(() -> callback.accept("ERROR: Conexión no activa"));
            return;
        }
        
        ComandoPendiente comandoPendiente = new ComandoPendiente(comando, parametros, callback);
        
        try {
            boolean agregado = colaComandos.offer(comandoPendiente);
            if (agregado) {
                // Comando agregado exitosamente a la cola
            } else {
                SwingUtilities.invokeLater(() -> callback.accept("ERROR: Cola de comandos llena"));
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> callback.accept("ERROR: " + e.getMessage()));
        }
    }
    
    /**
     * Envía una colocación de barco al servidor usando un protocolo especial.
     * Maneja tanto la respuesta de colocación como mensajes adicionales.
     * 
     * @param colocacion Datos de la colocación del barco
     * @param callback Función para procesar la respuesta de colocación
     * @param errorCallback Función para manejar errores
     */
    public void enviarColocacionBarco(ColocacionBarco colocacion, 
                                     Consumer<String> callback, 
                                     Consumer<String> errorCallback) {
        
        new Thread(() -> {
            try {
                synchronized(this) {
                    salida.writeUTF("colocar_barco");
                    salida.writeUTF(colocacion.getTipoBarco());
                    salida.writeUTF(String.valueOf(colocacion.getFila()));
                    salida.writeUTF(String.valueOf(colocacion.getColumna()));
                    salida.writeUTF(colocacion.getOrientacion());
                    salida.flush();
                    
                    // Leer respuesta inmediata
                    String respuesta1 = entrada.readUTF();
                    
                    // Leer mensaje adicional (barcos restantes o estado)
                    String respuesta2 = entrada.readUTF();
                    
                    final String r1 = respuesta1;
                    final String r2 = respuesta2;
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            if (r1.startsWith("barco_colocado:")) {
                                callback.accept(r1);
                                
                                // Enviar mensaje adicional al manejador general
                                if (manejadorMensajes != null) {
                                    manejadorMensajes.accept(r2);
                                }
                            } else if (r1.startsWith("error_colocacion:")) {
                                errorCallback.accept(r1.substring(17));
                            } else {
                                errorCallback.accept("Respuesta inesperada: " + r1);
                            }
                        } catch (Exception e) {
                            errorCallback.accept("Error: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                
                SwingUtilities.invokeLater(() -> {
                    errorCallback.accept("Error de comunicación: " + e.getMessage());
                });
            }
        }, "Thread-ColocacionBarco").start();
    }
    
    /**
     * Lee el siguiente mensaje disponible del servidor de forma asíncrona.
     * 
     * @param callback Función para procesar el mensaje leído
     */
    public void leerSiguienteMensaje(Consumer<String> callback) {
        new Thread(() -> {
            try {
                Thread.sleep(200);
                
                if (entrada.available() > 0) {
                    String mensaje = entrada.readUTF();
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            callback.accept(mensaje);
                        } catch (Exception e) {
                            // Ignorar errores en el callback
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        callback.accept("error:No hay mensaje disponible");
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    callback.accept("error:" + e.getMessage());
                });
            }
        }, "Thread-LecturaMensaje").start();
    }
    
    /**
     * Verifica si la conexión con el servidor está activa.
     * 
     * @return true si la conexión está activa, false en caso contrario
     */
    public boolean estaConectado() {
        return conexionActiva.get();
    }
    
    /**
     * Cierra la comunicación con el servidor de forma ordenada.
     * Envía comando de terminación y detiene los threads.
     */
    public void cerrar() {
        conexionActiva.set(false);
        
        try {
            if (salida != null) {
                salida.writeUTF("termina_servicio");
                salida.flush();
            }
        } catch (IOException e) {
            // Ignorar errores al cerrar
        }
        
        if (threadEscucha != null && threadEscucha.isAlive()) {
            threadEscucha.interrupt();
        }
        if (threadEnvio != null && threadEnvio.isAlive()) {
            threadEnvio.interrupt();
        }
    }
    
    /**
     * Clase interna que encapsula un comando pendiente de envío y su respuesta.
     * Gestiona el estado del comando y el callback asociado.
     */
    private static class ComandoPendiente {
        /** Nombre del comando */
        private final String comando;
        /** Parámetros del comando */
        private final String[] parametros;
        /** Callback para procesar la respuesta */
        private final Consumer<String> callback;
        /** Indica si el comando ha sido enviado */
        private volatile boolean enviado = false;
        /** Indica si el comando ha sido completado */
        private volatile boolean completado = false;
        
        /**
         * Constructor del comando pendiente.
         * 
         * @param comando Nombre del comando
         * @param parametros Parámetros del comando
         * @param callback Función para procesar la respuesta
         */
        public ComandoPendiente(String comando, String[] parametros, Consumer<String> callback) {
            this.comando = comando;
            this.parametros = parametros != null ? parametros : new String[0];
            this.callback = callback;
        }
        
        /**
         * Obtiene el nombre del comando.
         * @return Nombre del comando
         */
        public String getComando() { return comando; }
        
        /**
         * Obtiene los parámetros del comando.
         * @return Array de parámetros
         */
        public String[] getParametros() { return parametros; }
        
        /**
         * Marca el comando como enviado al servidor.
         */
        public void marcarEnviado() { 
            this.enviado = true; 
        }
        
        /**
         * Verifica si el comando ha sido enviado.
         * @return true si ha sido enviado, false en caso contrario
         */
        public boolean estaEnviado() { 
            return enviado; 
        }
        
        /**
         * Verifica si el comando ha sido completado (recibida respuesta).
         * @return true si está completado, false en caso contrario
         */
        public boolean estaCompletado() { 
            return completado; 
        }
        
        /**
         * Procesa la respuesta recibida del servidor.
         * Ejecuta el callback asociado en el thread de la UI.
         * 
         * @param respuesta Respuesta recibida del servidor
         */
        public void procesarRespuesta(String respuesta) {
            if (!completado) {
                completado = true;
                if (callback != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(respuesta));
                }
            }
        }
        
        /**
         * Procesa un error en el comando.
         * Ejecuta el callback con un mensaje de error.
         * 
         * @param error Descripción del error ocurrido
         */
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