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
    
    private final BlockingQueue<ComandoPendiente> colaComandos;
    private final AtomicReference<ComandoPendiente> comandoActual;
    
    private final Thread threadEscucha;
    private final Thread threadEnvio;
    
    private Consumer<String> manejadorMensajes;
    private Consumer<String> manejadorErrores;
    
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
    
    public void iniciarEscucha(Consumer<String> onMensaje, Consumer<String> onError) {
        this.manejadorMensajes = onMensaje;
        this.manejadorErrores = onError;
    }
    
    private void escucharServidor() {
        
        while (conexionActiva.get()) {
            try {
                if (entrada.available() > 0) {
                    String mensaje = entrada.readUTF();
                    
                    if (mensaje.startsWith("rival_encontrado:") ||
                        mensaje.startsWith("partida_lista:") ||
                        mensaje.startsWith("turno_colocacion:")) {
                        
                        if (manejadorMensajes != null) {
                            final String mensajeFinal = mensaje;
                            SwingUtilities.invokeLater(() -> manejadorMensajes.accept(mensajeFinal));
                        }
                        
                        continue;
                    }
                    
                    ComandoPendiente comando = comandoActual.get();
                    
                    if (comando != null && !comando.estaCompletado()) {
                        comando.procesarRespuesta(mensaje);
                        comandoActual.set(null);
                    } else {
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
    
    private void procesarComandos() {
        
        while (conexionActiva.get()) {
            try {
                ComandoPendiente comando = colaComandos.take();
                
                comandoActual.set(comando);
                
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
    
    public void enviarComando(String comando, Consumer<String> callback) {
        enviarComandoConParametros(comando, new String[0], callback);
    }
    
    public void enviarComandoConParametro(String comando, String parametro, Consumer<String> callback) {
        enviarComandoConParametros(comando, new String[]{parametro}, callback);
    }
    
    public void enviarComandoConParametros(String comando, String[] parametros, Consumer<String> callback) {
        if (!conexionActiva.get()) {
            SwingUtilities.invokeLater(() -> callback.accept("ERROR: Conexión no activa"));
            return;
        }
        
        ComandoPendiente comandoPendiente = new ComandoPendiente(comando, parametros, callback);
        
        try {
            boolean agregado = colaComandos.offer(comandoPendiente);
            if (agregado) {
            } else {
                SwingUtilities.invokeLater(() -> callback.accept("ERROR: Cola de comandos llena"));
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> callback.accept("ERROR: " + e.getMessage()));
        }
    }
    
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
                    
                    String respuesta1 = entrada.readUTF();
                    
                    String respuesta2 = entrada.readUTF();
                    
                    final String r1 = respuesta1;
                    final String r2 = respuesta2;
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            if (r1.startsWith("barco_colocado:")) {
                                callback.accept(r1);
                                
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
    
    public void leerSiguienteMensaje(Consumer<String> callback) {
        new Thread(() -> {
            try {
                Thread.sleep(200);
                
                if (entrada.available() > 0) {
                    String mensaje = entrada.readUTF();
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            callback.accept(mensaje);
                        } catch (Exception e) {}
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
    
    public boolean estaConectado() {
        return conexionActiva.get();
    }
    
    public void cerrar() {
        conexionActiva.set(false);
        
        try {
            if (salida != null) {
                salida.writeUTF("termina_servicio");
                salida.flush();
            }
        } catch (IOException e) {}
        
        if (threadEscucha != null && threadEscucha.isAlive()) {
            threadEscucha.interrupt();
        }
        if (threadEnvio != null && threadEnvio.isAlive()) {
            threadEnvio.interrupt();
        }
    }
    
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