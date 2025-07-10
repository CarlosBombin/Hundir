package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Clase que gestiona todos los componentes de la interfaz de usuario del cliente.
 * Centraliza la creación, configuración y manejo de elementos visuales como
 * botones, paneles, campos de entrada y áreas de log.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ComponentesUI {
    
    /** Formato para mostrar la hora en los logs */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    /** Fuente para el área de log */
    private static final Font LOG_FONT = new Font("Monospaced", Font.PLAIN, 12);
    /** Fuente para el label de estado */
    private static final Font ESTADO_FONT = new Font("Arial", Font.BOLD, 14);
    /** Tamaño estándar para spinners */
    private static final Dimension SPINNER_SIZE = new Dimension(150, 25);
    /** Tamaño estándar para combo boxes */
    private static final Dimension COMBO_SIZE = new Dimension(150, 25);
    /** Tamaño estándar para botones */
    private static final Dimension BUTTON_SIZE = new Dimension(200, 30);
    
    // Componentes de estado y log
    /** Label que muestra el estado actual del juego */
    private JLabel labelEstado;
    /** Área de texto para mostrar mensajes y logs */
    private JTextArea areaLog;
    /** Panel con scroll para el área de log */
    private JScrollPane scrollLog;
    
    // Botones del menú principal
    /** Botón para crear una nueva partida */
    private JButton btnCrearPartida;
    /** Botón para unirse a una partida existente */
    private JButton btnUnirsePartida;
    /** Botón para ver el estado del servidor */
    private JButton btnEstadoServidor;
    /** Botón para desconectarse del servidor */
    private JButton btnDesconectar;
    /** Botón para comprobar si el rival está listo */
    private JButton botonComprobarRival;
    
    // Componentes de colocación de barcos
    /** Panel que contiene todos los controles de colocación */
    private JPanel panelColocacion;
    /** Selector del tipo de barco a colocar */
    private JComboBox<String> comboTipoBarco;
    /** Selector de la fila donde colocar el barco */
    private JSpinner spinnerFila;
    /** Selector de la columna donde colocar el barco */
    private JSpinner spinnerColumna;
    /** Selector de la orientación del barco */
    private JComboBox<String> comboOrientacion;
    /** Botón para ejecutar la colocación del barco */
    private JButton btnColocarBarco;
    /** Botón para finalizar la fase de colocación */
    private JButton btnFinalizarColocacion;
    
    // Otros paneles
    /** Panel que contiene los botones del menú */
    private JPanel panelBotones;
    /** Label que se muestra mientras se espera al rival */
    private JLabel labelEsperaRival;
    
    /**
     * Inicializa todos los componentes de la interfaz de usuario.
     * Debe llamarse antes de usar cualquier componente.
     */
    public void inicializar() {
        crearComponentesEstado();
        crearComponentesLog();
        crearComponentesMenu();
        crearComponentesColocacion();
    }
    
    /**
     * Crea y configura los componentes relacionados con el estado del juego.
     */
    private void crearComponentesEstado() {
        labelEstado = new JLabel("Conectado - Seleccione una opción", JLabel.CENTER);
        labelEstado.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        labelEstado.setFont(ESTADO_FONT);
        labelEstado.setBackground(Color.LIGHT_GRAY);
        labelEstado.setOpaque(true);
    }
    
    /**
     * Crea y configura el área de log y su panel con scroll.
     */
    private void crearComponentesLog() {
        areaLog = new JTextArea(10, 30);
        areaLog.setEditable(false);
        areaLog.setFont(LOG_FONT);
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(Color.GREEN);
        
        scrollLog = new JScrollPane(areaLog);
        scrollLog.setPreferredSize(new Dimension(0, 150));
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    /**
     * Crea y configura los botones del menú principal.
     */
    private void crearComponentesMenu() {
        btnCrearPartida = new JButton("Crear Partida");
        btnUnirsePartida = new JButton("Unirse a Partida");
        btnEstadoServidor = new JButton("Estado Servidor");
        btnDesconectar = new JButton("Desconectar");
        
        panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnCrearPartida);
        panelBotones.add(btnUnirsePartida);
        panelBotones.add(btnEstadoServidor);
        panelBotones.add(btnDesconectar);
    }
    
    /**
     * Crea y configura el panel de colocación de barcos con todos sus controles.
     */
    private void crearComponentesColocacion() {
        panelColocacion = new JPanel(new GridBagLayout());
        panelColocacion.setBorder(BorderFactory.createTitledBorder("Colocación de Barcos"));
        panelColocacion.setPreferredSize(new Dimension(250, 300));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        agregarComponenteColocacion(gbc, 0, "Tipo:", 
            comboTipoBarco = crearComboTipoBarco());
        
        agregarComponenteColocacion(gbc, 1, "Fila:", 
            spinnerFila = crearSpinnerCoordenada());
        
        agregarComponenteColocacion(gbc, 2, "Columna:", 
            spinnerColumna = crearSpinnerCoordenada());
        
        agregarComponenteColocacion(gbc, 3, "Orientación:", 
            comboOrientacion = crearComboOrientacion());
        
        agregarBotonesColocacion(gbc);
        
        panelColocacion.setVisible(false);
    }
    
    /**
     * Ayuda a agregar un componente con su etiqueta al panel de colocación.
     * 
     * @param gbc Constraints para el layout
     * @param fila Fila donde colocar el componente
     * @param etiqueta Texto de la etiqueta
     * @param componente Componente a agregar
     */
    private void agregarComponenteColocacion(GridBagConstraints gbc, int fila, 
                                           String etiqueta, JComponent componente) {
        gbc.gridx = 0; 
        gbc.gridy = fila;
        panelColocacion.add(new JLabel(etiqueta), gbc);
        
        gbc.gridx = 1;
        panelColocacion.add(componente, gbc);
    }
    
    /**
     * Agrega los botones de acción al panel de colocación.
     * 
     * @param gbc Constraints para el layout
     */
    private void agregarBotonesColocacion(GridBagConstraints gbc) {
        gbc.gridx = 0; 
        gbc.gridy = 4; 
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        btnColocarBarco = new JButton("Colocar Barco");
        btnColocarBarco.setPreferredSize(BUTTON_SIZE);
        panelColocacion.add(btnColocarBarco, gbc);
        
        gbc.gridy = 5;
        btnFinalizarColocacion = new JButton("Finalizar Colocación");
        btnFinalizarColocacion.setPreferredSize(BUTTON_SIZE);
        btnFinalizarColocacion.setEnabled(false);
        panelColocacion.add(btnFinalizarColocacion, gbc);
    }
    
    /**
     * Crea el combo box para seleccionar el tipo de barco.
     * @return JComboBox configurado con los tipos de barco
     */
    private JComboBox<String> crearComboTipoBarco() {
        JComboBox<String> combo = new JComboBox<>(
            new String[]{"PORTAVIONES", "SUBMARINO", "DESTRUCTOR", "FRAGATA"});
        combo.setPreferredSize(COMBO_SIZE);
        return combo;
    }
    
    /**
     * Crea un spinner para seleccionar coordenadas (0-7).
     * @return JSpinner configurado para coordenadas del tablero
     */
    private JSpinner crearSpinnerCoordenada() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 7, 1));
        spinner.setPreferredSize(SPINNER_SIZE);
        return spinner;
    }
    
    /**
     * Crea el combo box para seleccionar la orientación del barco.
     * @return JComboBox con opciones HORIZONTAL y VERTICAL
     */
    private JComboBox<String> crearComboOrientacion() {
        JComboBox<String> combo = new JComboBox<>(
            new String[]{"HORIZONTAL", "VERTICAL"});
        combo.setPreferredSize(COMBO_SIZE);
        return combo;
    }
    
    /**
     * Configura los listeners de acción para todos los botones.
     * 
     * @param crearPartida Listener para crear partida
     * @param unirsePartida Listener para unirse a partida
     * @param estadoServidor Listener para ver estado del servidor
     * @param desconectar Listener para desconectarse
     * @param colocarBarco Listener para colocar barco
     * @param finalizarColocacion Listener para finalizar colocación
     */
    public void setActionListeners(ActionListener crearPartida, 
                                 ActionListener unirsePartida,
                                 ActionListener estadoServidor, 
                                 ActionListener desconectar,
                                 ActionListener colocarBarco, 
                                 ActionListener finalizarColocacion) {
        btnCrearPartida.addActionListener(crearPartida);
        btnUnirsePartida.addActionListener(unirsePartida);
        btnEstadoServidor.addActionListener(estadoServidor);
        btnDesconectar.addActionListener(desconectar);
        btnColocarBarco.addActionListener(colocarBarco);
        btnFinalizarColocacion.addActionListener(finalizarColocacion);
    }
    
    /**
     * Configura el listener especial para el combo de tipo de barco.
     * Deshabilita la orientación cuando se selecciona FRAGATA.
     */
    public void configurarTipoBarcoListener() {
        comboTipoBarco.addActionListener(e -> {
            String tipo = (String) comboTipoBarco.getSelectedItem();
            if ("FRAGATA".equals(tipo)) {
                comboOrientacion.setSelectedItem("HORIZONTAL");
                comboOrientacion.setEnabled(false);
            } else {
                comboOrientacion.setEnabled(true);
            }
        });
    }
    
    /**
     * Escribe un mensaje en el área de log con timestamp.
     * 
     * @param mensaje Mensaje a escribir en el log
     */
    public void escribirLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMATTER);
            areaLog.append("[" + timestamp + "] " + mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }
    
    /**
     * Actualiza el texto del label de estado del juego.
     * 
     * @param estado Nuevo estado a mostrar
     */
    public void actualizarEstado(String estado) {
        SwingUtilities.invokeLater(() -> labelEstado.setText(estado));
    }
    
    /**
     * Muestra el panel de colocación de barcos.
     */
    public void mostrarPanelColocacion() {
        SwingUtilities.invokeLater(() -> {
            panelColocacion.setVisible(true);
            panelColocacion.revalidate();
            panelColocacion.repaint();
        });
    }
    
    /**
     * Oculta el panel de colocación de barcos.
     */
    public void ocultarPanelColocacion() {
        SwingUtilities.invokeLater(() -> {
            panelColocacion.setVisible(false);
            panelColocacion.revalidate();
            panelColocacion.repaint();
        });
    }
    
    /**
     * Habilita el botón de colocar barco.
     */
    public void habilitarBotonColocar() {
        SwingUtilities.invokeLater(() -> btnColocarBarco.setEnabled(true));
    }
    
    /**
     * Deshabilita el botón de colocar barco.
     */
    public void deshabilitarBotonColocar() {
        SwingUtilities.invokeLater(() -> btnColocarBarco.setEnabled(false));
    }
    
    /**
     * Habilita el botón de finalizar colocación.
     */
    public void habilitarBotonFinalizar() {
        SwingUtilities.invokeLater(() -> btnFinalizarColocacion.setEnabled(true));
    }
    
    /**
     * Deshabilita el botón de finalizar colocación.
     */
    public void deshabilitarBotonFinalizar() {
        SwingUtilities.invokeLater(() -> btnFinalizarColocacion.setEnabled(false));
    }
    
    /**
     * Deshabilita todos los botones del menú principal.
     */
    public void deshabilitarBotonesMenu() {
        SwingUtilities.invokeLater(() -> {
            btnCrearPartida.setEnabled(false);
            btnUnirsePartida.setEnabled(false);
            btnEstadoServidor.setEnabled(false);
        });
    }
    
    /**
     * Habilita todos los botones del menú principal.
     */
    public void habilitarBotonesMenu() {
        SwingUtilities.invokeLater(() -> {
            btnCrearPartida.setEnabled(true);
            btnUnirsePartida.setEnabled(true);
            btnEstadoServidor.setEnabled(true);
        });
    }
    
    /**
     * Selecciona automáticamente una casilla en los spinners de coordenadas.
     * 
     * @param fila Fila a seleccionar
     * @param columna Columna a seleccionar
     */
    public void seleccionarCasilla(int fila, int columna) {
        SwingUtilities.invokeLater(() -> {
            spinnerFila.setValue(fila);
            spinnerColumna.setValue(columna);
        });
    }
    
    /**
     * Obtiene el tipo de barco actualmente seleccionado.
     * @return Tipo de barco seleccionado
     */
    public String getTipoBarcoSeleccionado() {
        return (String) comboTipoBarco.getSelectedItem();
    }
    
    /**
     * Obtiene la fila actualmente seleccionada.
     * @return Fila seleccionada (0-7)
     */
    public int getFilaSeleccionada() {
        return (Integer) spinnerFila.getValue();
    }
    
    /**
     * Obtiene la columna actualmente seleccionada.
     * @return Columna seleccionada (0-7)
     */
    public int getColumnaSeleccionada() {
        return (Integer) spinnerColumna.getValue();
    }
    
    /**
     * Obtiene la orientación actualmente seleccionada.
     * @return Orientación seleccionada (HORIZONTAL o VERTICAL)
     */
    public String getOrientacionSeleccionada() {
        return (String) comboOrientacion.getSelectedItem();
    }
    
    // Getters para acceder a los componentes desde otras clases
    
    /**
     * Obtiene el label de estado del juego.
     * @return JLabel del estado
     */
    public JLabel getLabelEstado() {
        return labelEstado;
    }
    
    /**
     * Obtiene el panel con scroll del área de log.
     * @return JScrollPane del log
     */
    public JScrollPane getScrollLog() { 
        return scrollLog; 
    }
    
    /**
     * Obtiene el panel de botones del menú.
     * @return JPanel con los botones del menú
     */
    public JPanel getPanelBotones() { 
        return panelBotones; 
    }
    
    /**
     * Obtiene el panel de colocación de barcos.
     * @return JPanel de colocación
     */
    public JPanel getPanelColocacion() { 
        return panelColocacion; 
    }
    
    /**
     * Obtiene el área de texto del log.
     * @return JTextArea del log
     */
    public JTextArea getAreaLog() {
        return areaLog;
    }
    
    /**
     * Muestra un diálogo de error al usuario.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un diálogo informativo al usuario.
     * 
     * @param mensaje Mensaje informativo a mostrar
     */
    public void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Limpia la selección de todos los controles de colocación.
     * Restaura los valores por defecto en spinners y combos.
     */
    public void limpiarSeleccion() {
        try {
            if (spinnerFila != null) spinnerFila.setValue(0);
            if (spinnerColumna != null) spinnerColumna.setValue(0);
            if (comboOrientacion != null) comboOrientacion.setSelectedIndex(0);
            if (comboTipoBarco != null) comboTipoBarco.setSelectedIndex(0);
        } catch (Exception e) {
            // Ignorar errores de UI
        }
    }

    /**
     * Actualiza el selector de tipos de barco con los barcos disponibles.
     * Solo muestra los tipos que aún tienen unidades disponibles para colocar.
     * 
     * @param barcosRestantes Mapa con el tipo de barco y cantidad restante
     */
    public void actualizarSelectorBarcos(Map<String, Integer> barcosRestantes) {
        DefaultComboBoxModel<String> modelo = (DefaultComboBoxModel<String>) comboTipoBarco.getModel();
        modelo.removeAllElements();
        for (Map.Entry<String, Integer> entry : barcosRestantes.entrySet()) {
            if (entry.getValue() > 0) {
                modelo.addElement(entry.getKey().toUpperCase());
            }
        }
        if (modelo.getSize() > 0) {
            comboTipoBarco.setSelectedIndex(0);
        }
    }
    
    /**
     * Crea el label de espera del rival si no existe.
     */
    public void crearLabelEsperaRival() {
        labelEsperaRival = new JLabel("Esperando a que el rival termine de colocar...", SwingConstants.CENTER);
        labelEsperaRival.setFont(new Font("Arial", Font.BOLD, 22));
        labelEsperaRival.setForeground(Color.BLUE);
        labelEsperaRival.setVisible(false);
    }
    
    /**
     * Muestra el label de espera del rival en el panel principal.
     * 
     * @param panelPrincipal Panel donde mostrar el label
     */
    public void mostrarLabelEsperaRival(JPanel panelPrincipal) {
        if (labelEsperaRival == null) crearLabelEsperaRival();
        labelEsperaRival.setVisible(true);
        panelPrincipal.add(labelEsperaRival, BorderLayout.CENTER);
        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }
    
    /**
     * Oculta el label de espera del rival del panel principal.
     * 
     * @param panelPrincipal Panel del que ocultar el label
     */
    public void ocultarLabelEsperaRival(JPanel panelPrincipal) {
        if (labelEsperaRival != null) {
            labelEsperaRival.setVisible(false);
            panelPrincipal.remove(labelEsperaRival);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
        }
    }
    
    /**
     * Crea el botón para comprobar el estado del rival si no existe.
     */
    private void crearBotonComprobarRival() {
        botonComprobarRival = new JButton("Comprobar rival");
        botonComprobarRival.setFont(new Font("Arial", Font.BOLD, 16));
        botonComprobarRival.setVisible(false);
    }

    /**
     * Muestra el botón para comprobar el rival con el listener especificado.
     * 
     * @param panelPrincipal Panel donde mostrar el botón
     * @param listener Listener para el evento click del botón
     */
    public void mostrarBotonComprobarRival(JPanel panelPrincipal, ActionListener listener) {
        if (botonComprobarRival == null) crearBotonComprobarRival();
        for (ActionListener al : botonComprobarRival.getActionListeners()) {
            botonComprobarRival.removeActionListener(al);
        }
        botonComprobarRival.addActionListener(listener);
        botonComprobarRival.setVisible(true);
        panelBotones.add(botonComprobarRival);
        panelBotones.revalidate();
        panelBotones.repaint();
    }

    /**
     * Oculta el botón para comprobar el rival del panel principal.
     * 
     * @param panelPrincipal Panel del que ocultar el botón
     */
    public void ocultarBotonComprobarRival(JPanel panelPrincipal) {
        if (botonComprobarRival != null) {
            botonComprobarRival.setVisible(false);
            panelBotones.remove(botonComprobarRival);
            panelBotones.revalidate();
            panelBotones.repaint();
        }
    }
}