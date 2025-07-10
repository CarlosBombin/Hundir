package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

public class VentanaJuego extends JFrame {
    
    private final ComunicacionServidor comunicacion;
    private final ComponentesUI componentes;
    private final ManejadorTablero tablero;
    private final ValidadorColocacionLocal validador;
    private GameLogger logger;
    
    private boolean colocandoBarcos;
    private Timer colocacionTimer;
    
    private JPanel panelPrincipal;
    
    public VentanaJuego(DataInputStream entrada, DataOutputStream salida) {
        this.comunicacion = new ComunicacionServidor(entrada, salida);
        this.componentes = new ComponentesUI();
        this.tablero = new ManejadorTablero(8);
        this.validador = new ValidadorColocacionLocal();
        this.colocandoBarcos = false;
        
        inicializar();
    }
    
    private void inicializar() {
        try {
            configurarVentana();
            inicializarComponentes();
            
            this.logger = new GameLogger(componentes.getAreaLog());
            
            configurarLayout();
            configurarEventos();
            iniciarComunicacion();
            
            logger.log("Cliente iniciado correctamente");
            
            setVisible(true);
            
        } catch (Exception e) {
            System.err.println("Error inicializando ventana: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void configurarVentana() {
        setTitle("Hundir la Flota - Cliente");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
            }
        });
    }
    
    private void inicializarComponentes() {
        componentes.inicializar();
        tablero.crearTableroVisual(this::seleccionarCasilla);
        
        tablero.setDependencias(componentes.getLabelEstado(), this::enviarColocacionBarco);
    }
    
    private void configurarLayout() {
        panelPrincipal = new JPanel(new BorderLayout());
        
        panelPrincipal.add(componentes.getLabelEstado(), BorderLayout.NORTH);
        
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(tablero.getTableroPanel(), BorderLayout.CENTER);
        panelCentral.add(componentes.getPanelColocacion(), BorderLayout.EAST);
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(componentes.getPanelBotones(), BorderLayout.NORTH);
        panelInferior.add(componentes.getScrollLog(), BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    private void configurarEventos() {
        componentes.setActionListeners(
            e -> crearPartida(),
            e -> unirseAPartida(),
            e -> verEstadoServidor(),
            e -> cerrarAplicacion(),
            e -> colocarBarco(),
            e -> finalizarColocacion()
        );
        
        componentes.configurarTipoBarcoListener();
    }
    
    private void iniciarComunicacion() {
        comunicacion.iniciarEscucha(
            this::procesarMensajeServidor,
            this::manejarErrorComunicacion
        );
    }
    
    private void crearPartida() {
        logger.log("Iniciando creación de partida...");
        componentes.deshabilitarBotonesMenu(); 
        
        comunicacion.enviarComando("crear_partida", respuesta -> {
            logger.log("Respuesta crear partida: " + respuesta);
            
            if (respuesta.startsWith("partida_creada:")) {
                logger.logSuccess("Partida creada: " + respuesta.substring(15));
                componentes.actualizarEstado("Esperando rival...");
            } else if (respuesta.startsWith("ERROR:")) {
                logger.logError("Error creando partida: " + respuesta.substring(6));
                componentes.mostrarError("Error creando partida: " + respuesta.substring(6));
                componentes.habilitarBotonesMenu(); 
            } else {
                logger.logError("Respuesta inesperada: " + respuesta);
                componentes.mostrarError("Error inesperado en el servidor");
                componentes.habilitarBotonesMenu();
            }
        });
    }
    
    private void unirseAPartida() {
        logger.log("=== INICIO UNIRSE A PARTIDA ===");
        logger.log("Solicitando partidas disponibles...");
        
        comunicacion.enviarComando("unirse_partida", respuesta -> {
            logger.log("=== RESPUESTA UNIRSE PARTIDA ===");
            logger.log("Respuesta completa: '" + respuesta + "'");
            logger.log("Longitud: " + respuesta.length());
            logger.log("Empieza con 'no_partidas:': " + respuesta.startsWith("no_partidas:"));
            logger.log("Empieza con 'partidas_disponibles:': " + respuesta.startsWith("partidas_disponibles:"));
            
            if (respuesta.startsWith("no_partidas:")) {
                String mensaje = respuesta.substring(12);
                logger.log("No hay partidas: " + mensaje);
                componentes.mostrarInformacion("No hay partidas disponibles");
                
            } else if (respuesta.startsWith("partidas_disponibles:")) {
                String partidasStr = respuesta.substring(21);
                logger.log("Partidas encontradas: '" + partidasStr + "'");
                mostrarDialogoPartidas(partidasStr);
                
            } else if (respuesta.startsWith("ERROR:")) {
                logger.logError("Error obteniendo partidas: " + respuesta.substring(6));
                componentes.mostrarError("Error obteniendo partidas: " + respuesta.substring(6));
                
            } else {
                logger.logWarning("Respuesta inesperada completa: '" + respuesta + "'");
                componentes.mostrarError("Respuesta inesperada del servidor: " + respuesta);
            }
            
            logger.log("=== FIN RESPUESTA UNIRSE PARTIDA ===");
        });
    }
    
    private void verEstadoServidor() {
        logger.log("Solicitando estado del servidor...");
        
        comunicacion.enviarComando("estado_servidor", respuesta -> {
            logger.log("Estado del servidor recibido: " + respuesta);
            
            if (respuesta.startsWith("ERROR:")) {
                logger.logError("Error obteniendo estado: " + respuesta.substring(6));
                componentes.mostrarError("Error obteniendo estado del servidor");
            } else {
                componentes.mostrarInformacion("Estado del servidor:\n" + respuesta);
            }
        });
    }
    
    private void colocarBarco() {
        logger.log("=== INICIO COLOCAR BARCO ===");
        
        if (!validarEstadoColocacion()) {
            return;
        }
        
        ColocacionBarco colocacion = crearColocacionDesdeComponentes();
        if (colocacion == null) {
            return;
        }
        
        if (!validador.esValidaColocacion(colocacion, tablero)) {
            return;
        }
        
        logger.log("Enviando colocación: " + colocacion.getTipoBarco() + " en (" + 
                   colocacion.getFila() + "," + colocacion.getColumna() + ") " + 
                   colocacion.getOrientacion());
        
        componentes.deshabilitarBotonColocar();
        
        comunicacion.enviarColocacionBarco(
            colocacion,
            respuesta -> {
                logger.log("=== PROCESANDO RESPUESTA COLOCACIÓN ===");
                logger.log("Respuesta: " + respuesta);
                
                if (respuesta.startsWith("barco_colocado:")) {
                    String mensaje = respuesta.substring(15);
                    logger.logSuccess("Barco colocado exitosamente: " + mensaje);
                    
                    tablero.marcarBarco(colocacion);
                    componentes.limpiarSeleccion();
                    componentes.actualizarEstado("Barco colocado: " + mensaje);
                    
                } else {
                    logger.logWarning("Respuesta no esperada: " + respuesta);
                    componentes.habilitarBotonColocar();
                }
                
                logger.log("=== FIN PROCESAMIENTO RESPUESTA COLOCACIÓN ===");
            },
            error -> {
                logger.logError("Error colocando barco: " + error);
                componentes.mostrarError("Error colocando barco: " + error);
                componentes.habilitarBotonColocar();
            }
        );
        
        logger.log("=== FIN INICIO COLOCAR BARCO ===");
    }
    
    private boolean validarEstadoColocacion() {
        if (!colocandoBarcos) {
            componentes.mostrarError("No está en fase de colocación");
            return false;
        }
        return true;
    }
    
    private ColocacionBarco crearColocacionDesdeComponentes() {
        try {
            String tipoBarco = componentes.getTipoBarcoSeleccionado().toUpperCase();
            Integer fila = componentes.getFilaSeleccionada();
            Integer columna = componentes.getColumnaSeleccionada();
            String orientacion = componentes.getOrientacionSeleccionada();
            
            if (tipoBarco == null || fila == null || columna == null || orientacion == null) {
                logger.logError("Datos de colocación incompletos");
                return null;
            }
            
            return new ColocacionBarco(tipoBarco, fila, columna, orientacion);
            
        } catch (Exception e) {
            logger.logError("Error obteniendo datos de colocación: " + e.getMessage());
            return null;
        }
    }
    
    private void procesarRespuestaColocacion(String respuesta, ColocacionBarco colocacion) {
        if (respuesta.startsWith("barco_colocado:")) {
            String mensaje = respuesta.substring(15);
            logger.logSuccess("Barco colocado exitosamente: " + mensaje);
            
            SwingUtilities.invokeLater(() -> {
                tablero.marcarBarco(colocacion);
                componentes.actualizarEstado("Barco colocado: " + mensaje);
                componentes.limpiarSeleccion();
            });
            
        } else if (respuesta.startsWith("error_colocacion:")) {
            String error = respuesta.substring(17);
            logger.logError("Error colocando barco: " + error);
            componentes.mostrarError("Error colocando barco: " + error);
            componentes.habilitarBotonColocar();
            
        } else {
            logger.logWarning("Respuesta no reconocida: " + respuesta);
            componentes.habilitarBotonColocar();
        }
    }
    
    private void procesarMensajeBarcoColocado(String mensaje) {
        logger.log("Procesando mensaje adicional: " + mensaje);
    
        if (mensaje.startsWith("barcos_restantes:")) {
            String restantes = mensaje.substring(17);
            logger.log("Barcos restantes: " + restantes);
            componentes.actualizarEstado("Barcos restantes: " + restantes);
            
            componentes.habilitarBotonColocar();
            logger.log("Botón colocar habilitado nuevamente");
            
        } else if (mensaje.startsWith("colocacion_completa:")) {
            String info = mensaje.substring(20);
            logger.logSuccess("Colocación completa: " + info);
            componentes.actualizarEstado("Colocación completa. " + info);
            componentes.habilitarBotonFinalizar();
            
        } else {
            logger.logWarning("Mensaje adicional no reconocido: " + mensaje);
            componentes.habilitarBotonColocar();
        }
    }
    
    private void finalizarColocacion() {
        comunicacion.enviarComando("finalizar_colocacion", respuesta -> {
            if (respuesta.startsWith("colocacion_finalizada:")) {
                mostrarPantallaEsperaRival();
            } else if (respuesta.startsWith("partida_ready:")) {
                iniciarJuego();
            } else {
                logger.logError("Error finalizando: " + respuesta);
            }
        });
    }
    
    private void procesarMensajeServidor(String mensaje) {
        logger.log("=== PROCESANDO MENSAJE SERVIDOR ===");
        logger.log("Mensaje: " + mensaje);

        if (mensaje.startsWith("partida_ready:")) {
            iniciarJuego();
            return;
        }
        
        if (mensaje.startsWith("barcos_restantes:")) {
            String restantes = mensaje.substring(17);
            logger.log("Barcos restantes: " + restantes);
            componentes.actualizarEstado("Barcos restantes: " + restantes);

            Map<String, Integer> barcosRestantes = parsearBarcosRestantes(restantes);
            componentes.actualizarSelectorBarcos(barcosRestantes);

            boolean quedanBarcos = barcosRestantes.values().stream().anyMatch(v -> v > 0);
            if (quedanBarcos) {
                componentes.habilitarBotonColocar();
                componentes.deshabilitarBotonFinalizar();
            } else if (!quedanBarcos) {
                componentes.deshabilitarBotonColocar();
                componentes.habilitarBotonFinalizar();
            }
            logger.log("Botón colocar " + (quedanBarcos ? "habilitado" : "deshabilitado") +
                       ", botón finalizar " + (quedanBarcos ? "deshabilitado" : "habilitado"));
            return;
        }
        
        if (mensaje.startsWith("rival_encontrado:")) {
            String nombreRival = mensaje.substring(16);
            logger.logSuccess("¡Rival encontrado! " + nombreRival);
            componentes.actualizarEstado("Jugando contra: " + nombreRival);
            return;
        }
        
        if (mensaje.startsWith("partida_lista:")) {
            String info = mensaje.substring(14);
            logger.logSuccess("Partida lista: " + info);
            componentes.actualizarEstado("Partida iniciada - " + info);
            return;
        }
        
        if (mensaje.startsWith("turno_colocacion:")) {
            String info = mensaje.substring(16);
            logger.logSuccess("Turno de colocación: " + info);
            componentes.actualizarEstado(info);
            iniciarFaseColocacion();
            return;
        }
        
        if (mensaje.startsWith("ataque_recibido:")) {
            procesarAtaqueRecibido(mensaje);
            return;
        }
        
        if (mensaje.startsWith("fin_partida:")) {
            String info = mensaje.substring("fin_partida:".length());
            componentes.actualizarEstado(info);
            tablero.deshabilitarAtaqueRival();
            tablero.deshabilitarSeleccion();
            JOptionPane.showMessageDialog(this, info, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
    }
    
    private void iniciarFaseColocacion() {
        colocandoBarcos = true;
        componentes.actualizarEstado("Coloque sus barcos en el tablero");
        
        componentes.mostrarPanelColocacion();
        componentes.habilitarBotonColocar();
        tablero.habilitarSeleccion();
    }
    
    private void finalizarFaseColocacion() {
        colocandoBarcos = false;
        componentes.ocultarPanelColocacion();
        tablero.deshabilitarSeleccion();
        componentes.actualizarEstado("Esperando inicio del juego...");
    }
    
    private void iniciarJuego() {
        finalizarFaseColocacion();
        componentes.ocultarLabelEsperaRival(panelPrincipal);
        componentes.ocultarBotonComprobarRival(panelPrincipal);
        componentes.actualizarEstado("¡Comienza la batalla!");
        mostrarTablerosDeBatalla();
        comunicacion.enviarComando("quien_empieza", respuesta -> {
            if (respuesta.startsWith("tu_turno")) {
                tablero.habilitarAtaqueRival((fila, columna) -> enviarAtaque(fila, columna));
            } else {
                tablero.deshabilitarAtaqueRival();
            }
        });
    }

    private void mostrarTablerosDeBatalla() {
        JPanel panelBatalla = new JPanel(new GridLayout(1, 2, 20, 0));
        panelBatalla.add(tablero.getTableroPanelRival());
        setContentPane(panelBatalla);
        revalidate();
        repaint();
        tablero.habilitarAtaqueRival((fila, columna) -> enviarAtaque(fila, columna));
    }

    private void enviarAtaque(Integer fila, Integer columna) {
        comunicacion.enviarComandoConParametro("atacar", fila + "," + columna, respuesta -> {
            procesarRespuestaAtaque(fila, columna, respuesta);
        });
    }
    
    private void mostrarDialogoPartidas(String partidasStr) {
        
        if (partidasStr == null || partidasStr.trim().isEmpty()) {
            componentes.mostrarInformacion("No hay partidas disponibles");
            return;
        }
        
        SelectorPartidas selector = new SelectorPartidas(this, partidasStr.trim());
        String partidaSeleccionada = selector.mostrarDialogo();
        
        if (partidaSeleccionada != null) {
            unirseAPartidaSeleccionada(partidaSeleccionada);
        } else {
            logger.log("Usuario canceló selección de partida");
        }
    }
    
    private void unirseAPartidaSeleccionada(String partidaSeleccionada) {
        String idPartida = partidaSeleccionada.split(" - ")[0];
        
        componentes.deshabilitarBotonesMenu();
        
        comunicacion.enviarComandoConParametro("seleccionar_partida", idPartida, resultado -> {

            
            if (resultado.startsWith("unido_exitoso:")) {
                componentes.actualizarEstado("Unido a partida: " + idPartida);
            } else if (resultado.startsWith("ERROR:")) {
                componentes.mostrarError("Error uniéndose a la partida: " + resultado.substring(6));
                componentes.habilitarBotonesMenu();
            } else if (resultado.startsWith("error")) {
                componentes.mostrarError("Error del servidor: " + resultado.substring(6));
                componentes.habilitarBotonesMenu();
            } else {
                componentes.mostrarError("Respuesta inesperada del servidor");
                componentes.habilitarBotonesMenu();
            }
        });
    }
    
    private void seleccionarCasilla(int fila, int columna) {
        if (colocandoBarcos) {
            componentes.seleccionarCasilla(fila, columna);
        }
    }
    
    private void manejarErrorComunicacion(String error) {
        componentes.actualizarEstado("Desconectado");
        componentes.habilitarBotonesMenu();
    }
    
    private void cerrarAplicacion() {
        comunicacion.cerrar();
        System.exit(0);
    }
    
    private void enviarColocacionBarco(ColocacionBarco colocacion) {
        
        comunicacion.enviarColocacionBarco(
            colocacion,
            respuesta -> {                
                if (respuesta.startsWith("barco_colocado:")) {
                    String mensaje = respuesta.substring(15);
                    componentes.actualizarEstado("Barco colocado: " + mensaje);
                } else if (respuesta.startsWith("error_colocacion:")) {
                    String error = respuesta.substring(17);
                    componentes.mostrarError("Error colocando barco: " + error);
                } else {
                }
            },
            error -> {
                componentes.mostrarError("Error de comunicación: " + error);
            }
        );
    }
    
    private Map<String, Integer> parsearBarcosRestantes(String texto) {
        Map<String, Integer> mapa = new HashMap<>();
        String[] partes = texto.split(",");
        for (String parte : partes) {
            String[] kv = parte.trim().split(":");
            if (kv.length == 2) {
                mapa.put(kv[0].trim().toUpperCase(), Integer.parseInt(kv[1].trim()));
            }
        }
        return mapa;
    }
    
    private void mostrarPantallaEsperaRival() {
        componentes.actualizarEstado("Esperando a que el rival termine de colocar...");
        componentes.ocultarPanelColocacion();
        componentes.mostrarLabelEsperaRival(panelPrincipal);
        componentes.mostrarBotonComprobarRival(panelPrincipal, e -> comprobarRivalListo());
    }

    private void comprobarRivalListo() {
        comunicacion.enviarComando("comprobar_listo", respuesta -> {
            if (respuesta.startsWith("partida_ready:")) {
                iniciarJuego();
            } else if (respuesta.startsWith("aun_esperando:")) {
                componentes.actualizarEstado("Aún esperando al rival...");
            } else {
                componentes.mostrarError("Respuesta inesperada: " + respuesta);
            }
        });
    }

    private void procesarRespuestaAtaque(int fila, int columna, String respuesta) {
        if (respuesta.startsWith("resultado_ataque:")) {
            String resultado = respuesta.substring("resultado_ataque:".length());
            switch (resultado) {
                case "agua":
                    tablero.marcarAguaEnRival(fila, columna);
                    componentes.actualizarEstado("¡Agua!");
                    break;
                case "tocado":
                    tablero.marcarTocadoEnRival(fila, columna);
                    componentes.actualizarEstado("¡Tocado!");
                    break;
                case "hundido":
                    tablero.marcarHundidoEnRival(fila, columna);
                    componentes.actualizarEstado("¡Hundido!");
                    break;
                default:
                    componentes.mostrarError("Respuesta inesperada: " + resultado);
            }
            tablero.deshabilitarAtaqueRival();
        }
    }

    private void procesarAtaqueRecibido(String mensaje) {
        String datos = mensaje.substring("ataque_recibido:".length());
        String[] partes = datos.split(",");
        int fila = Integer.parseInt(partes[0]);
        int columna = Integer.parseInt(partes[1]);
        String resultado = partes[2];

        switch (resultado) {
            case "agua":
                tablero.marcarAguaEnPropio(fila, columna);
                break;
            case "tocado":
                tablero.marcarTocadoEnPropio(fila, columna);
                break;
            case "hundido":
                tablero.marcarHundidoEnPropio(fila, columna);
                break;
            default:
                System.out.println("Error.");
        }

        tablero.habilitarAtaqueRival((f, c) -> enviarAtaque(f, c));
    }
}