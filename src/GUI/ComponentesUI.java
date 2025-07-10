package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ComponentesUI {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Font LOG_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font ESTADO_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Dimension SPINNER_SIZE = new Dimension(150, 25);
    private static final Dimension COMBO_SIZE = new Dimension(150, 25);
    private static final Dimension BUTTON_SIZE = new Dimension(200, 30);
    
    private JLabel labelEstado;
    private JTextArea areaLog;
    private JScrollPane scrollLog;
    
    private JButton btnCrearPartida;
    private JButton btnUnirsePartida;
    private JButton btnEstadoServidor;
    private JButton btnDesconectar;
    private JButton botonComprobarRival;
    
    private JPanel panelColocacion;
    private JComboBox<String> comboTipoBarco;
    private JSpinner spinnerFila;
    private JSpinner spinnerColumna;
    private JComboBox<String> comboOrientacion;
    private JButton btnColocarBarco;
    private JButton btnFinalizarColocacion;
    
    private JPanel panelBotones;
    
    private JLabel labelEsperaRival;
    
    public void inicializar() {
        crearComponentesEstado();
        crearComponentesLog();
        crearComponentesMenu();
        crearComponentesColocacion();
    }
    
    private void crearComponentesEstado() {
        labelEstado = new JLabel("Conectado - Seleccione una opción", JLabel.CENTER);
        labelEstado.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        labelEstado.setFont(ESTADO_FONT);
        labelEstado.setBackground(Color.LIGHT_GRAY);
        labelEstado.setOpaque(true);
    }
    
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
    
    private void agregarComponenteColocacion(GridBagConstraints gbc, int fila, 
                                           String etiqueta, JComponent componente) {
        gbc.gridx = 0; 
        gbc.gridy = fila;
        panelColocacion.add(new JLabel(etiqueta), gbc);
        
        gbc.gridx = 1;
        panelColocacion.add(componente, gbc);
    }
    
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
    
    private JComboBox<String> crearComboTipoBarco() {
        JComboBox<String> combo = new JComboBox<>(
            new String[]{"PORTAVIONES", "SUBMARINO", "DESTRUCTOR", "FRAGATA"});
        combo.setPreferredSize(COMBO_SIZE);
        return combo;
    }
    
    private JSpinner crearSpinnerCoordenada() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 7, 1));
        spinner.setPreferredSize(SPINNER_SIZE);
        return spinner;
    }
    
    private JComboBox<String> crearComboOrientacion() {
        JComboBox<String> combo = new JComboBox<>(
            new String[]{"HORIZONTAL", "VERTICAL"});
        combo.setPreferredSize(COMBO_SIZE);
        return combo;
    }
    
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
    
    public void escribirLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMATTER);
            areaLog.append("[" + timestamp + "] " + mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }
    
    public void actualizarEstado(String estado) {
        SwingUtilities.invokeLater(() -> labelEstado.setText(estado));
    }
    
    public void mostrarPanelColocacion() {
        SwingUtilities.invokeLater(() -> {
            panelColocacion.setVisible(true);
            panelColocacion.revalidate();
            panelColocacion.repaint();
        });
    }
    
    public void ocultarPanelColocacion() {
        SwingUtilities.invokeLater(() -> {
            panelColocacion.setVisible(false);
            panelColocacion.revalidate();
            panelColocacion.repaint();
        });
    }
    
    public void habilitarBotonColocar() {
        SwingUtilities.invokeLater(() -> btnColocarBarco.setEnabled(true));
    }
    
    public void deshabilitarBotonColocar() {
        SwingUtilities.invokeLater(() -> btnColocarBarco.setEnabled(false));
    }
    
    public void habilitarBotonFinalizar() {
        SwingUtilities.invokeLater(() -> btnFinalizarColocacion.setEnabled(true));
    }
    
    public void deshabilitarBotonFinalizar() {
        SwingUtilities.invokeLater(() -> btnFinalizarColocacion.setEnabled(false));
    }
    
    public void deshabilitarBotonesMenu() {
        SwingUtilities.invokeLater(() -> {
            btnCrearPartida.setEnabled(false);
            btnUnirsePartida.setEnabled(false);
            btnEstadoServidor.setEnabled(false);
        });
    }
    
    public void habilitarBotonesMenu() {
        SwingUtilities.invokeLater(() -> {
            btnCrearPartida.setEnabled(true);
            btnUnirsePartida.setEnabled(true);
            btnEstadoServidor.setEnabled(true);
        });
    }
    
    public void seleccionarCasilla(int fila, int columna) {
        SwingUtilities.invokeLater(() -> {
            spinnerFila.setValue(fila);
            spinnerColumna.setValue(columna);
        });
    }
    
    public String getTipoBarcoSeleccionado() {
        return (String) comboTipoBarco.getSelectedItem();
    }
    
    public int getFilaSeleccionada() {
        return (Integer) spinnerFila.getValue();
    }
    
    public int getColumnaSeleccionada() {
        return (Integer) spinnerColumna.getValue();
    }
    
    public String getOrientacionSeleccionada() {
        return (String) comboOrientacion.getSelectedItem();
    }
    
    public JLabel getLabelEstado() {
        return labelEstado;
    }
    public JScrollPane getScrollLog() { return scrollLog; }
    public JPanel getPanelBotones() { return panelBotones; }
    public JPanel getPanelColocacion() { return panelColocacion; }
    public JTextArea getAreaLog() {
        return areaLog;
    }
    
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void limpiarSeleccion() {
        try {
            if (spinnerFila != null) spinnerFila.setValue(0);
            if (spinnerColumna != null) spinnerColumna.setValue(0);
            if (comboOrientacion != null) comboOrientacion.setSelectedIndex(0);
            if (comboTipoBarco != null) comboTipoBarco.setSelectedIndex(0);
        } catch (Exception e) {}
    }

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
    
    public void crearLabelEsperaRival() {
        labelEsperaRival = new JLabel("Esperando a que el rival termine de colocar...", SwingConstants.CENTER);
        labelEsperaRival.setFont(new Font("Arial", Font.BOLD, 22));
        labelEsperaRival.setForeground(Color.BLUE);
        labelEsperaRival.setVisible(false);
    }
    
    public void mostrarLabelEsperaRival(JPanel panelPrincipal) {
        if (labelEsperaRival == null) crearLabelEsperaRival();
        labelEsperaRival.setVisible(true);
        panelPrincipal.add(labelEsperaRival, BorderLayout.CENTER);
        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }
    
    public void ocultarLabelEsperaRival(JPanel panelPrincipal) {
        if (labelEsperaRival != null) {
            labelEsperaRival.setVisible(false);
            panelPrincipal.remove(labelEsperaRival);
            panelPrincipal.revalidate();
            panelPrincipal.repaint();
        }
    }
    
    private void crearBotonComprobarRival() {
        botonComprobarRival = new JButton("Comprobar rival");
        botonComprobarRival.setFont(new Font("Arial", Font.BOLD, 16));
        botonComprobarRival.setVisible(false);
    }

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

    public void ocultarBotonComprobarRival(JPanel panelPrincipal) {
        if (botonComprobarRival != null) {
            botonComprobarRival.setVisible(false);
            panelBotones.remove(botonComprobarRival);
            panelBotones.revalidate();
            panelBotones.repaint();
        }
    }
}