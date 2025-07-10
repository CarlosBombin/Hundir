package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Ventana principal del juego Hundir la Flota del lado cliente.
 * Gestiona toda la interfaz gráfica del juego, incluyendo la comunicación con el servidor,
 * la colocación de barcos, los ataques y la visualización de tableros.
 * Coordina todos los componentes GUI y maneja el flujo completo del juego.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class VentanaJuego extends JFrame {
    
    /** Manejador de comunicación con el servidor */
    private final ComunicacionServidor comunicacion;
    /** Gestor de componentes de la interfaz de usuario */
    private final ComponentesUI componentes;
    /** Manejador de los tableros de juego */
    private final ManejadorTablero tablero;
    /** Validador local para colocación de barcos */
    private final ValidadorColocacionLocal validador;
    /** Sistema de logging del juego */
    private GameLogger logger;
    
    /** Indica si el jugador está actualmente colocando barcos */
    private boolean colocandoBarcos;
    /** Timer para controlar la fase de colocación */
    private Timer colocacionTimer;
    /** Panel principal que contiene todos los componentes */
    private JPanel panelPrincipal;
    
    /**
     * Constructor que inicializa la ventana principal del juego.
     * Configura la comunicación con el servidor y todos los componentes necesarios.
     * 
     * @param entrada Flujo de entrada de datos desde el servidor
     * @param salida Flujo de salida de datos hacia el servidor
     */
    public VentanaJuego(DataInputStream entrada, DataOutputStream salida) {
        this.comunicacion = new ComunicacionServidor(entrada, salida);
        this.componentes = new ComponentesUI();
        this.tablero = new ManejadorTablero(8);
        this.validador = new ValidadorColocacionLocal();
        this.colocandoBarcos = false;
        
        inicializar();
    }
    
    /**
     * Inicializa todos los componentes de la ventana y la hace visible.
     * Configura la ventana, componentes, layout, eventos y comunicación.
     */
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
    
    /**
     * Configura las propiedades básicas de la ventana principal.
     * Establece título, tamaño, posición y comportamiento de cierre.
     */
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
    
    /**
     * Inicializa todos los componentes de la interfaz de usuario.
     * Configura componentes UI, tablero y establece dependencias.
     */
    private void inicializarComponentes() {
        componentes.inicializar();
        tablero.crearTableroVisual(this::seleccionarCasilla);
        
        tablero.setDependencias(componentes.getLabelEstado(), this::enviarColocacionBarco);
    }
    
    /**
     * Configura el layout principal de la ventana.
     * Organiza componentes en un BorderLayout con paneles específicos.
     */
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
    
    /**
     * Configura los event listeners para todos los componentes interactivos.
     * Asocia acciones a botones y configura listeners especiales.
     */
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
    
    /**
     * Inicia la comunicación bidireccional con el servidor.
     * Configura los manejadores de mensajes y errores.
     */
    private void iniciarComunicacion() {
        comunicacion.iniciarEscucha(
            this::procesarMensajeServidor,
            this::manejarErrorComunicacion
        );
    }
    
    /**
     * Crea una nueva partida enviando la solicitud al servidor.
     * Deshabilita los botones del menú y maneja la respuesta del servidor.
     */
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
    
    /**
     * Solicita las partidas disponibles al servidor para unirse a una.
     * Maneja la respuesta y muestra el diálogo de selección si hay partidas.
     */
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
    
    /**
     * Solicita y muestra el estado actual del servidor.
     * Útil para diagnóstico y monitoreo de la conexión.
     */
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
    
    /**
     * Coloca un barco en el tablero según la selección del usuario.
     * Valida la colocación localmente antes de enviarla al servidor.
     */
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
    
    /**
     * Valida si el juego está en estado de colocación de barcos.
     * 
     * @return true si se puede colocar barcos, false en caso contrario
     */
    private boolean validarEstadoColocacion() {
        if (!colocandoBarcos) {
            componentes.mostrarError("No está en fase de colocación");
            return false;
        }
        return true;
    }
    
    /**
     * Crea un objeto ColocacionBarco a partir de los componentes de la UI.
     * 
     * @return ColocacionBarco con los datos del usuario o null si hay error
     */
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
    
    /**
     * Procesa la respuesta del servidor tras colocar un barco.
     * Actualiza la interfaz según el resultado de la colocación.
     * 
     * @param respuesta Respuesta del servidor
     * @param colocacion Datos de la colocación realizada
     */
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
    
    /**
     * Procesa mensajes adicionales relacionados con la colocación de barcos.
     * Maneja información sobre barcos restantes y estado de colocación.
     * 
     * @param mensaje Mensaje adicional del servidor
     */
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
    
    /**
     * Finaliza la fase de colocación de barcos y notifica al servidor.
     * Maneja las diferentes respuestas posibles del servidor.
     */
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
    
    /**
     * Procesa todos los mensajes recibidos del servidor.
     * Distribuye los mensajes a los manejadores específicos según su tipo.
     * 
     * @param mensaje Mensaje completo recibido del servidor
     */
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
    
    /**
     * Inicia la fase de colocación de barcos.
     * Habilita los controles necesarios y actualiza el estado.
     */
    private void iniciarFaseColocacion() {
        colocandoBarcos = true;
        componentes.actualizarEstado("Coloque sus barcos en el tablero");
        
        componentes.mostrarPanelColocacion();
        componentes.habilitarBotonColocar();
        tablero.habilitarSeleccion();
    }
    
    /**
     * Finaliza la fase de colocación de barcos.
     * Oculta los controles de colocación y actualiza el estado.
     */
    private void finalizarFaseColocacion() {
        colocandoBarcos = false;
        componentes.ocultarPanelColocacion();
        tablero.deshabilitarSeleccion();
        componentes.actualizarEstado("Esperando inicio del juego...");
    }
    
    /**
     * Inicia la fase de juego/combate.
     * Configura los tableros de batalla y determina quién ataca primero.
     */
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

    /**
     * Configura la interfaz para mostrar los tableros de batalla.
     * Cambia el contenido de la ventana a los tableros de juego.
     */
    private void mostrarTablerosDeBatalla() {
        JPanel panelBatalla = new JPanel(new GridLayout(1, 2, 20, 0));
        panelBatalla.add(tablero.getTableroPanelRival());
        setContentPane(panelBatalla);
        revalidate();
        repaint();
        tablero.habilitarAtaqueRival((fila, columna) -> enviarAtaque(fila, columna));
    }

    /**
     * Envía un ataque a una coordenada específica del tablero rival.
     * 
     * @param fila Fila del ataque
     * @param columna Columna del ataque
     */
    private void enviarAtaque(Integer fila, Integer columna) {
        comunicacion.enviarComandoConParametro("atacar", fila + "," + columna, respuesta -> {
            procesarRespuestaAtaque(fila, columna, respuesta);
        });
    }
    
    /**
     * Muestra el diálogo de selección de partidas disponibles.
     * Permite al usuario elegir una partida específica para unirse.
     * 
     * @param partidasStr String con las partidas disponibles separadas por delimitadores
     */
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
    
    /**
     * Se une a una partida específica seleccionada por el usuario.
     * Envía la solicitud al servidor y maneja la respuesta.
     * 
     * @param partidaSeleccionada Información de la partida seleccionada
     */
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
    
    /**
     * Maneja la selección de una casilla del tablero.
     * Solo procesa la selección si está en fase de colocación.
     * 
     * @param fila Fila de la casilla seleccionada
     * @param columna Columna de la casilla seleccionada
     */
    private void seleccionarCasilla(int fila, int columna) {
        if (colocandoBarcos) {
            componentes.seleccionarCasilla(fila, columna);
        }
    }
    
    /**
     * Maneja los errores de comunicación con el servidor.
     * Actualiza el estado y rehabilita controles si es necesario.
     * 
     * @param error Descripción del error de comunicación
     */
    private void manejarErrorComunicacion(String error) {
        componentes.actualizarEstado("Desconectado");
        componentes.habilitarBotonesMenu();
    }
    
    /**
     * Cierra la aplicación de forma ordenada.
     * Cierra la comunicación con el servidor y termina la aplicación.
     */
    private void cerrarAplicacion() {
        comunicacion.cerrar();
        System.exit(0);
    }
    
    /**
     * Envía una colocación de barco al servidor.
     * Callback utilizado por el manejador de tablero.
     * 
     * @param colocacion Datos de la colocación del barco
     */
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
                    // Respuesta no reconocida
                }
            },
            error -> {
                componentes.mostrarError("Error de comunicación: " + error);
            }
        );
    }
    
    /**
     * Parsea un string con información de barcos restantes.
     * Convierte el formato "TIPO:CANTIDAD,TIPO:CANTIDAD" en un Map.
     * 
     * @param texto String con la información de barcos restantes
     * @return Map con tipos de barco y cantidades restantes
     */
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
    
    /**
     * Muestra la pantalla de espera mientras el rival termina de colocar.
     * Configura los elementos visuales para la fase de espera.
     */
    private void mostrarPantallaEsperaRival() {
        componentes.actualizarEstado("Esperando a que el rival termine de colocar...");
        componentes.ocultarPanelColocacion();
        componentes.mostrarLabelEsperaRival(panelPrincipal);
        componentes.mostrarBotonComprobarRival(panelPrincipal, e -> comprobarRivalListo());
    }

    /**
     * Comprueba si el rival ha terminado de colocar sus barcos.
     * Consulta al servidor sobre el estado de preparación del rival.
     */
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

    /**
     * Procesa la respuesta del servidor tras realizar un ataque.
     * Actualiza el tablero rival según el resultado del ataque.
     * 
     * @param fila Fila del ataque realizado
     * @param columna Columna del ataque realizado
     * @param respuesta Respuesta del servidor con el resultado
     */
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

    /**
     * Procesa los ataques recibidos del rival.
     * Actualiza el tablero propio según el resultado del ataque enemigo.
     * 
     * @param mensaje Mensaje del servidor con los detalles del ataque
     */
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
                System.out.println("Error procesando ataque recibido.");
        }

        tablero.habilitarAtaqueRival((f, c) -> enviarAtaque(f, c));
    }
}