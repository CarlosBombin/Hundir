package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Clase que gestiona la visualización y funcionalidad de los tableros del juego.
 * Maneja tanto el tablero propio (donde se colocan los barcos) como el tablero
 * rival (donde se realizan los ataques). Proporciona funcionalidades para
 * marcar barcos, gestionar ataques y validar colocaciones.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ManejadorTablero {
    
    /** Color de las casillas vacías del tablero */
    private static final Color COLOR_CASILLA_VACIA = Color.LIGHT_GRAY;
    /** Color de las casillas que se pueden seleccionar */
    private static final Color COLOR_CASILLA_SELECCIONABLE = Color.GRAY;
    /** Fuente para el texto de las casillas */
    private static final Font FONT_CASILLA = new Font("Arial", Font.BOLD, 10);
    /** Tamaño estándar de cada casilla del tablero */
    private static final Dimension CASILLA_SIZE = new Dimension(45, 45);
    
    /** Tamaño del tablero (filas y columnas) */
    private final int tamaño;
    /** Matriz de botones que representan las casillas del tablero propio */
    private JButton[][] botonesTablero;
    /** Panel principal que contiene el tablero propio */
    private JPanel tableroPanel;
    /** Panel que contiene el tablero del rival */
    private JPanel tableroPanelRival;
    
    /** Lista de casillas actualmente seleccionadas para colocar un barco */
    private List<Point> seleccionActual = new ArrayList<>();
    /** Lista de todas las casillas que ya están ocupadas por barcos */
    private List<Point> casillasOcupadas = new ArrayList<>();
    /** Array con los tamaños de los barcos pendientes de colocar */
    private int[] barcosPendientes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1}; 
    /** Índice del barco que se está colocando actualmente */
    private int barcoActual = 0;
    /** Label para mostrar el estado de la colocación */
    private JLabel labelEstado;
    /** Listener que se ejecuta cuando se coloca un barco */
    private Consumer<ColocacionBarco> barcoColocadoListener;
    
    /**
     * Constructor que inicializa el manejador de tablero con el tamaño especificado.
     * 
     * @param tamaño Número de filas y columnas del tablero (típicamente 8)
     */
    public ManejadorTablero(int tamaño) {
        this.tamaño = tamaño;
    }
    
    /**
     * Establece las dependencias necesarias para el funcionamiento del tablero.
     * 
     * @param labelEstado Label donde mostrar el estado de la colocación
     * @param barcoColocadoListener Listener que se ejecuta al colocar un barco
     */
    public void setDependencias(JLabel labelEstado, Consumer<ColocacionBarco> barcoColocadoListener) {
        this.labelEstado = labelEstado;
        this.barcoColocadoListener = barcoColocadoListener;
        if (labelEstado != null && barcoActual < barcosPendientes.length) {
            labelEstado.setText("Coloque barco de " + barcosPendientes[barcoActual] + " casillas");
        }
    }
    
    /**
     * Crea la representación visual del tablero con botones interactivos.
     * Inicializa la matriz de botones y configura el panel contenedor.
     * 
     * @param onCasillaClick Callback que se ejecuta cuando se hace click en una casilla
     */
    public void crearTableroVisual(BiConsumer<Integer, Integer> onCasillaClick) {
        tableroPanel = new JPanel(new GridLayout(tamaño, tamaño, 2, 2));
        tableroPanel.setBorder(BorderFactory.createTitledBorder("Mi Tablero"));
        tableroPanel.setPreferredSize(new Dimension(400, 400));
        tableroPanel.setBackground(Color.WHITE);
        
        botonesTablero = new JButton[tamaño][tamaño];
        
        for (int i = 0; i < tamaño; i++) {
            for (int j = 0; j < tamaño; j++) {
                botonesTablero[i][j] = crearBotonCasilla(i, j, onCasillaClick);
                tableroPanel.add(botonesTablero[i][j]);
            }
        }
        
        verificarVariables();
    }
    
    /**
     * Crea un botón individual que representa una casilla del tablero.
     * Configura su apariencia y comportamiento de click.
     * 
     * @param fila Fila de la casilla en el tablero
     * @param columna Columna de la casilla en el tablero
     * @param onCasillaClick Callback para el evento de click
     * @return JButton configurado para la casilla
     */
    private JButton crearBotonCasilla(int fila, int columna, BiConsumer<Integer, Integer> onCasillaClick) {
        JButton boton = new JButton();
        boton.setPreferredSize(CASILLA_SIZE);
        boton.setFont(FONT_CASILLA);
        boton.setBackground(COLOR_CASILLA_VACIA);
        boton.setEnabled(false);
        boton.setFocusPainted(false);
        
        boton.addActionListener(e -> onCasillaClick.accept(fila, columna));
        
        return boton;
    }
    
    /**
     * Marca visualmente un barco en el tablero después de ser colocado.
     * Cambia el color y texto de las casillas según el tipo de barco.
     * 
     * @param colocacion Objeto con la información de colocación del barco
     */
    public void marcarBarco(ColocacionBarco colocacion) {
        try {
            int fila = colocacion.getFila();
            int columna = colocacion.getColumna();
            String tipoBarco = colocacion.getTipoBarco();
            String orientacion = colocacion.getOrientacion();
            
            int longitud;
            Color colorBarco;
            
            // Determinar longitud y color según el tipo de barco
            switch (tipoBarco.toUpperCase()) {
                case "PORTAVIONES":
                    longitud = 4;
                    colorBarco = new Color(255, 0, 0);
                    break;
                case "SUBMARINO":
                    longitud = 3;
                    colorBarco = new Color(0, 0, 255);
                    break;
                case "DESTRUCTOR":
                    longitud = 2;
                    colorBarco = new Color(0, 128, 0); 
                    break;
                case "FRAGATA":
                    longitud = 1;
                    colorBarco = new Color(255, 165, 0);
                    break;
                default:
                    longitud = 1;
                    colorBarco = Color.GRAY;
                    break;
            }
            
            // Marcar las casillas según la orientación
            if (orientacion.equalsIgnoreCase("HORIZONTAL")) {
                for (int c = columna; c < columna + longitud && c < tamaño; c++) {
                    marcarCasillaConBarco(fila, c, colorBarco, tipoBarco);
                }
            } else {
                for (int f = fila; f < fila + longitud && f < tamaño; f++) {
                    marcarCasillaConBarco(f, columna, colorBarco, tipoBarco);
                }
            }
            
            actualizarVisualizacion();
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error marcando barco: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Marca una casilla individual con la representación visual de un barco.
     * 
     * @param fila Fila de la casilla
     * @param columna Columna de la casilla
     * @param color Color del barco
     * @param tipoBarco Tipo del barco para el texto
     */
    private void marcarCasillaConBarco(int fila, int columna, Color color, String tipoBarco) {
        JButton casilla = botonesTablero[fila][columna];
        casilla.setBackground(color);
        casilla.setText(tipoBarco.substring(0, 1));
        casilla.setOpaque(true);
        casilla.setBorderPainted(true);
        casilla.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        casilla.repaint();
    }
    
    /**
     * Actualiza la visualización del tablero para reflejar los cambios.
     * Fuerza el repintado del panel y sus componentes padre.
     */
    private void actualizarVisualizacion() {
        SwingUtilities.invokeLater(() -> {
            if (tableroPanel != null) {
                tableroPanel.repaint();
                tableroPanel.revalidate();
                
                if (tableroPanel.getParent() != null) {
                    tableroPanel.getParent().repaint();
                    tableroPanel.getParent().revalidate();
                }
            }
        });
    }
    
    /**
     * Habilita la selección de casillas para colocar barcos.
     * Solo las casillas vacías se vuelven seleccionables.
     */
    public void habilitarSeleccion() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tamaño; i++) {
                for (int j = 0; j < tamaño; j++) {
                    if (esCasillaVacia(i, j)) {
                        botonesTablero[i][j].setEnabled(true);
                        botonesTablero[i][j].setBackground(COLOR_CASILLA_SELECCIONABLE);
                    }
                }
            }
        });
    }
    
    /**
     * Deshabilita la selección de todas las casillas del tablero.
     * Utilizado cuando no se está en fase de colocación.
     */
    public void deshabilitarSeleccion() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tamaño; i++) {
                for (int j = 0; j < tamaño; j++) {
                    botonesTablero[i][j].setEnabled(false);
                }
            }
        });
    }
    
    /**
     * Verifica si una casilla está ocupada por un barco.
     * Una casilla se considera ocupada si tiene texto (representando un barco).
     * 
     * @param fila Fila de la casilla
     * @param columna Columna de la casilla
     * @return true si la casilla está ocupada, false en caso contrario
     */
    public boolean esCasillaOcupada(int fila, int columna) {
        if (!esPosicionValida(fila, columna)) {
            return true;
        }
        
        JButton casilla = botonesTablero[fila][columna];
        String texto = casilla.getText();
        return texto != null && !texto.isEmpty();
    }
    
    /**
     * Verifica si una casilla está vacía y disponible para colocar barcos.
     * 
     * @param fila Fila de la casilla
     * @param columna Columna de la casilla
     * @return true si la casilla está vacía, false en caso contrario
     */
    private boolean esCasillaVacia(int fila, int columna) {
        return !esCasillaOcupada(fila, columna);
    }
    
    /**
     * Valida si una posición está dentro de los límites del tablero.
     * 
     * @param fila Fila a validar
     * @param columna Columna a validar
     * @return true si la posición es válida, false en caso contrario
     */
    private boolean esPosicionValida(int fila, int columna) {
        return fila >= 0 && fila < tamaño && columna >= 0 && columna < tamaño;
    }
    
    /**
     * Obtiene el panel principal del tablero propio.
     * 
     * @return JPanel que contiene el tablero
     */
    public JPanel getTableroPanel() {
        return tableroPanel;
    }
    
    /**
     * Obtiene el tamaño del tablero.
     * 
     * @return Número de filas y columnas del tablero
     */
    public int getTamaño() {
        return tamaño;
    }
    
    /**
     * Método de depuración que verifica la inicialización de variables clave.
     * Imprime en consola el estado de los componentes principales.
     */
    private void verificarVariables() {
        System.out.println("==== VERIFICACIÓN VARIABLES TABLERO ====");
        System.out.println("botonesTablero existe: " + (botonesTablero != null ? "SÍ" : "NO"));
        System.out.println("tableroPanel existe: " + (tableroPanel != null ? "SÍ" : "NO"));
        System.out.println("tamaño tablero: " + tamaño);
        System.out.println("=======================================");
    }
    
    /**
     * Maneja el click en una casilla durante la fase de colocación de barcos.
     * Gestiona la selección múltiple de casillas y valida la colocación.
     * 
     * @param fila Fila de la casilla clickeada
     * @param columna Columna de la casilla clickeada
     */
    public void manejarClick(int fila, int columna) {
        Point punto = new Point(fila, columna);
        
        // Ignorar casillas ya ocupadas o ya seleccionadas
        if (casillasOcupadas.contains(punto) || seleccionActual.contains(punto)) {
            return;
        }
        
        // Agregar casilla a la selección actual
        seleccionActual.add(punto);
        botonesTablero[fila][columna].setBackground(new Color(144, 238, 144));
        
        int tamBarcoActual = barcosPendientes[barcoActual];
        
        // Validar alineación para 2 casillas
        if (seleccionActual.size() == 2) {
            if (!estanAlineadas(seleccionActual.get(0), seleccionActual.get(1))) {
                mostrarError("Las casillas deben estar alineadas horizontal o verticalmente");
                return;
            }
        }
        
        // Validar línea recta para más de 2 casillas
        if (seleccionActual.size() > 2) {
            if (!sigueLineaRecta(seleccionActual)) {
                mostrarError("Las casillas deben estar en línea recta");
                return;
            }
        }
        
        // Colocar barco cuando se alcanza el tamaño requerido
        if (seleccionActual.size() == tamBarcoActual) {
            if (!espacioValido(seleccionActual)) {
                mostrarError("No se puede colocar barco adyacente a otro barco");
                return;
            }
            
            ejecutarColocacionBarco(tamBarcoActual);
        }
    }
    
    /**
     * Ejecuta la colocación final del barco una vez validada la selección.
     * 
     * @param tamBarcoActual Tamaño del barco que se está colocando
     */
    private void ejecutarColocacionBarco(int tamBarcoActual) {
        String tipoBarco = obtenerTipoBarco(tamBarcoActual);
        String orientacion = determinarOrientacion();
        Color colorBarco = obtenerColorBarco(tipoBarco);
        
        // Marcar casillas visualmente
        for (Point p : seleccionActual) {
            marcarCasillaSeleccionada(p, colorBarco, tipoBarco);
        }
        
        // Registrar casillas como ocupadas
        casillasOcupadas.addAll(seleccionActual);
        
        // Crear objeto de colocación y notificar
        Point primeraCasilla = seleccionActual.get(0);
        ColocacionBarco colocacion = new ColocacionBarco(
            tipoBarco, primeraCasilla.x, primeraCasilla.y, orientacion);
        
        if (barcoColocadoListener != null) {
            barcoColocadoListener.accept(colocacion);
        }
        
        // Limpiar selección y avanzar al siguiente barco
        seleccionActual.clear();
        barcoActual++;
        
        actualizarEstadoColocacion();
    }
    
    /**
     * Obtiene el tipo de barco basado en su tamaño.
     * 
     * @param tamaño Tamaño del barco en casillas
     * @return Nombre del tipo de barco
     */
    private String obtenerTipoBarco(int tamaño) {
        switch (tamaño) {
            case 4: return "PORTAVIONES";
            case 3: return "SUBMARINO";
            case 2: return "DESTRUCTOR";
            case 1: return "FRAGATA";
            default: return "DESCONOCIDO";
        }
    }
    
    /**
     * Determina la orientación del barco basándose en las casillas seleccionadas.
     * 
     * @return "HORIZONTAL" o "VERTICAL"
     */
    private String determinarOrientacion() {
        if (seleccionActual.size() <= 1) {
            return "HORIZONTAL";
        }
        
        int primeraFila = seleccionActual.get(0).x;
        boolean esHorizontal = true;
        
        for (int i = 1; i < seleccionActual.size(); i++) {
            if (seleccionActual.get(i).x != primeraFila) {
                esHorizontal = false;
                break;
            }
        }
        
        return esHorizontal ? "HORIZONTAL" : "VERTICAL";
    }
    
    /**
     * Marca visualmente una casilla seleccionada con el barco correspondiente.
     * 
     * @param punto Posición de la casilla
     * @param color Color del barco
     * @param tipoBarco Tipo del barco
     */
    private void marcarCasillaSeleccionada(Point punto, Color color, String tipoBarco) {
        JButton casilla = botonesTablero[punto.x][punto.y];
        casilla.setBackground(color);
        casilla.setText(tipoBarco.substring(0, 1));
        casilla.setForeground(Color.WHITE);
        casilla.setOpaque(true);
        casilla.setBorderPainted(true);
        casilla.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }
    
    /**
     * Actualiza el estado mostrado en el label tras colocar un barco.
     */
    private void actualizarEstadoColocacion() {
        if (labelEstado != null) {
            if (barcoActual < barcosPendientes.length) {
                labelEstado.setText("Coloque barco de " + barcosPendientes[barcoActual] + " casillas");
            } else {
                labelEstado.setText("¡Todos los barcos colocados! Presione Finalizar.");
            }
        }
    }
    
    /**
     * Verifica si dos puntos están alineados horizontal o verticalmente.
     * 
     * @param p1 Primer punto
     * @param p2 Segundo punto
     * @return true si están alineados, false en caso contrario
     */
    private boolean estanAlineadas(Point p1, Point p2) {
        return p1.x == p2.x || p1.y == p2.y;
    }

    /**
     * Verifica si una lista de puntos forma una línea recta continua.
     * Los puntos deben estar consecutivos en horizontal o vertical.
     * 
     * @param puntos Lista de puntos a verificar
     * @return true si forman línea recta, false en caso contrario
     */
    private boolean sigueLineaRecta(List<Point> puntos) {
        if (puntos.size() <= 2) return true;
        
        // Verificar si todos están en la misma fila
        boolean mismaFila = puntos.stream().allMatch(p -> p.x == puntos.get(0).x);
        
        // Verificar si todos están en la misma columna
        boolean mismaColumna = puntos.stream().allMatch(p -> p.y == puntos.get(0).y);
        
        if (mismaFila) {
            return verificarContinuidadHorizontal(puntos);
        } else if (mismaColumna) {
            return verificarContinuidadVertical(puntos);
        }
        
        return false;
    }
    
    /**
     * Verifica que los puntos sean consecutivos horizontalmente.
     * 
     * @param puntos Lista de puntos en la misma fila
     * @return true si son consecutivos, false en caso contrario
     */
    private boolean verificarContinuidadHorizontal(List<Point> puntos) {
        List<Point> ordenadas = new ArrayList<>(puntos);
        ordenadas.sort(Comparator.comparing(p -> p.y));
        
        for (int i = 1; i < ordenadas.size(); i++) {
            if (ordenadas.get(i).y != ordenadas.get(i-1).y + 1) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica que los puntos sean consecutivos verticalmente.
     * 
     * @param puntos Lista de puntos en la misma columna
     * @return true si son consecutivos, false en caso contrario
     */
    private boolean verificarContinuidadVertical(List<Point> puntos) {
        List<Point> ordenadas = new ArrayList<>(puntos);
        ordenadas.sort(Comparator.comparing(p -> p.x));
        
        for (int i = 1; i < ordenadas.size(); i++) {
            if (ordenadas.get(i).x != ordenadas.get(i-1).x + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valida que haya espacio suficiente alrededor de las casillas seleccionadas.
     * Los barcos no pueden colocarse adyacentes a otros barcos.
     * 
     * @param puntos Lista de puntos donde se quiere colocar el barco
     * @return true si el espacio es válido, false en caso contrario
     */
    private boolean espacioValido(List<Point> puntos) {
        for (Point p : puntos) {
            // Verificar todas las casillas adyacentes (incluyendo diagonales)
            for (int i = p.x - 1; i <= p.x + 1; i++) {
                for (int j = p.y - 1; j <= p.y + 1; j++) {
                    if (esPosicionValida(i, j)) {
                        Point adyacente = new Point(i, j);
                        // Si hay una casilla ocupada que no es parte del barco actual
                        if (casillasOcupadas.contains(adyacente) && !puntos.contains(adyacente)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Muestra un mensaje de error y resetea la selección actual.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        
        // Limpiar selección visual
        for (Point p : seleccionActual) {
            botonesTablero[p.x][p.y].setBackground(COLOR_CASILLA_SELECCIONABLE);
            botonesTablero[p.x][p.y].setText("");
        }
        seleccionActual.clear();
    }

    /**
     * Obtiene el color asociado a un tipo de barco específico.
     * 
     * @param tipoBarco Tipo del barco
     * @return Color correspondiente al tipo de barco
     */
    private Color obtenerColorBarco(String tipoBarco) {
        switch (tipoBarco.toUpperCase()) {
            case "PORTAVIONES": return new Color(255, 0, 0);   // Rojo
            case "SUBMARINO": return new Color(0, 255, 255);   // Cian
            case "DESTRUCTOR": return new Color(0, 128, 0);    // Verde
            case "FRAGATA": return new Color(255, 165, 0);     // Naranja
            default: return Color.GRAY;
        }
    }

    /**
     * Obtiene el panel del tablero propio.
     * Alias para getTableroPanel() para mayor claridad.
     * 
     * @return JPanel del tablero propio
     */
    public JPanel getTableroPanelPropio() {
        return tableroPanel;
    }

    /**
     * Obtiene el panel del tablero rival, creándolo si no existe.
     * 
     * @return JPanel del tablero rival
     */
    public JPanel getTableroPanelRival() {
        if (tableroPanelRival == null) {
            tableroPanelRival = crearTableroRival();
        }
        return tableroPanelRival;
    }

    /**
     * Crea el tablero visual para atacar al rival.
     * Similar al tablero propio pero optimizado para mostrar ataques.
     * 
     * @return JPanel configurado como tablero rival
     */
    private JPanel crearTableroRival() {
        JPanel panel = new JPanel(new GridLayout(tamaño, tamaño, 2, 2));
        panel.setBorder(BorderFactory.createTitledBorder("Tablero Rival"));
        panel.setPreferredSize(new Dimension(400, 400));
        panel.setBackground(Color.WHITE);

        for (int i = 0; i < tamaño; i++) {
            for (int j = 0; j < tamaño; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(CASILLA_SIZE);
                btn.setFont(FONT_CASILLA);
                btn.setBackground(COLOR_CASILLA_VACIA);
                btn.setEnabled(true);
                panel.add(btn);
            }
        }
        return panel;
    }

    /**
     * Habilita el tablero rival para realizar ataques.
     * Solo las casillas no atacadas previamente son clickeables.
     * 
     * @param callback Función que se ejecuta al hacer click en una casilla
     */
    public void habilitarAtaqueRival(BiConsumer<Integer, Integer> callback) {
        if (tableroPanelRival == null) {
            tableroPanelRival = crearTableroRival();
        }
        
        Component[] componentes = tableroPanelRival.getComponents();
        for (int i = 0; i < tamaño; i++) {
            for (int j = 0; j < tamaño; j++) {
                int idx = i * tamaño + j;
                if (idx < componentes.length && componentes[idx] instanceof JButton) {
                    JButton btn = (JButton) componentes[idx];
                    
                    // Solo habilitar casillas no atacadas (sin texto)
                    if (btn.getText().isEmpty()) {
                        // Limpiar listeners previos
                        for (ActionListener al : btn.getActionListeners()) {
                            btn.removeActionListener(al);
                        }
                        
                        btn.setEnabled(true);
                        final int fila = i;
                        final int columna = j;
                        
                        // Agregar nuevo listener que deshabilita todo tras el ataque
                        btn.addActionListener(e -> {
                            for (Component c : componentes) {
                                if (c instanceof JButton) {
                                    c.setEnabled(false);
                                }
                            }
                            callback.accept(fila, columna);
                        });
                    } else {
                        btn.setEnabled(false);
                    }
                }
            }
        }
    }

    /**
     * Marca una casilla del tablero rival como agua (ataque fallido).
     * 
     * @param fila Fila de la casilla atacada
     * @param columna Columna de la casilla atacada
     */
    public void marcarAguaEnRival(int fila, int columna) {
        if (tableroPanelRival == null) return;
        
        int idx = fila * tamaño + columna;
        Component[] componentes = tableroPanelRival.getComponents();
        
        if (idx < componentes.length && componentes[idx] instanceof JButton) {
            JButton btn = (JButton) componentes[idx];
            btn.setBackground(Color.CYAN);
            btn.setText("~");
            btn.setEnabled(false);
        }
    }

    /**
     * Marca una casilla del tablero rival como tocado (ataque exitoso).
     * 
     * @param fila Fila de la casilla atacada
     * @param columna Columna de la casilla atacada
     */
    public void marcarTocadoEnRival(int fila, int columna) {
        if (tableroPanelRival == null) return;
        
        int idx = fila * tamaño + columna;
        Component[] componentes = tableroPanelRival.getComponents();
        
        if (idx < componentes.length && componentes[idx] instanceof JButton) {
            JButton btn = (JButton) componentes[idx];
            btn.setBackground(Color.RED);
            btn.setText("X");
            btn.setEnabled(false);
        }
    }

    /**
     * Marca una casilla del tablero rival como hundido (barco completamente destruido).
     * 
     * @param fila Fila de la casilla atacada
     * @param columna Columna de la casilla atacada
     */
    public void marcarHundidoEnRival(int fila, int columna) {
        if (tableroPanelRival == null) return;
        
        int idx = fila * tamaño + columna;
        Component[] componentes = tableroPanelRival.getComponents();
        
        if (idx < componentes.length && componentes[idx] instanceof JButton) {
            JButton btn = (JButton) componentes[idx];
            btn.setBackground(Color.BLACK);
            btn.setForeground(Color.WHITE);
            btn.setText("☠");
            btn.setEnabled(false);
        }
    }

    /**
     * Marca una casilla del tablero propio como agua tras recibir un ataque fallido.
     * 
     * @param fila Fila de la casilla atacada
     * @param columna Columna de la casilla atacada
     */
    public void marcarAguaEnPropio(int fila, int columna) {
        if (botonesTablero == null) return;
        
        JButton btn = botonesTablero[fila][columna];
        btn.setBackground(Color.CYAN);
        btn.setText("~");
    }

    /**
     * Marca una casilla del tablero propio como tocado tras recibir un ataque exitoso.
     * 
     * @param fila Fila de la casilla atacada
     * @param columna Columna de la casilla atacada
     */
    public void marcarTocadoEnPropio(int fila, int columna) {
        if (botonesTablero == null) return;
        
        JButton btn = botonesTablero[fila][columna];
        btn.setBackground(Color.RED);
        btn.setText("X");
    }

    /**
     * Marca una casilla del tablero propio como hundido tras destruir completamente un barco.
     * 
     * @param fila Fila de la casilla atacada
     * @param columna Columna de la casilla atacada
     */
    public void marcarHundidoEnPropio(int fila, int columna) {
        if (botonesTablero == null) return;
        
        JButton btn = botonesTablero[fila][columna];
        btn.setBackground(Color.BLACK);
        btn.setForeground(Color.WHITE);
        btn.setText("☠");
    }

    /**
     * Deshabilita todos los botones del tablero rival.
     * Utilizado cuando no es el turno del jugador para atacar.
     */
    public void deshabilitarAtaqueRival() {
        if (tableroPanelRival == null) return;
        
        Component[] componentes = tableroPanelRival.getComponents();
        for (Component c : componentes) {
            if (c instanceof JButton) {
                c.setEnabled(false);
            }
        }
    }
}