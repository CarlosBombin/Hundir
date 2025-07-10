package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ManejadorTablero {
    
    private static final Color COLOR_CASILLA_VACIA = Color.LIGHT_GRAY;
    private static final Color COLOR_CASILLA_SELECCIONABLE = Color.GRAY;
    private static final Font FONT_CASILLA = new Font("Arial", Font.BOLD, 10);
    private static final Dimension CASILLA_SIZE = new Dimension(45, 45);
    
    private final int tamaño;
    private JButton[][] botonesTablero;
    private JPanel tableroPanel;
    
    private List<Point> seleccionActual = new ArrayList<>();
    private List<Point> casillasOcupadas = new ArrayList<>();
    private int[] barcosPendientes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1}; 
    private int barcoActual = 0;
    private JLabel labelEstado;
    private Consumer<ColocacionBarco> barcoColocadoListener;
    
    public ManejadorTablero(int tamaño) {
        this.tamaño = tamaño;
    }
    
    public void setDependencias(JLabel labelEstado, Consumer<ColocacionBarco> barcoColocadoListener) {
        this.labelEstado = labelEstado;
        this.barcoColocadoListener = barcoColocadoListener;
        if (labelEstado != null && barcoActual < barcosPendientes.length) {
            labelEstado.setText("Coloque barco de " + barcosPendientes[barcoActual] + " casillas");
        }
    }
    
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
    
    public void marcarBarco(ColocacionBarco colocacion) {
        try {
            int fila = colocacion.getFila();
            int columna = colocacion.getColumna();
            String tipoBarco = colocacion.getTipoBarco();
            String orientacion = colocacion.getOrientacion();
            
            int longitud;
            Color colorBarco;
            
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
            
            if (orientacion.equalsIgnoreCase("HORIZONTAL")) {
                for (int c = columna; c < columna + longitud && c < tamaño; c++) {
                    System.out.println("  - Marcando casilla [" + fila + "," + c + "]");
                    botonesTablero[fila][c].setBackground(colorBarco);
                    botonesTablero[fila][c].setText(tipoBarco.substring(0, 1));
                    botonesTablero[fila][c].setOpaque(true);
                    botonesTablero[fila][c].setBorderPainted(true);
                    botonesTablero[fila][c].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                    botonesTablero[fila][c].repaint();
                }
            } else {
                for (int f = fila; f < fila + longitud && f < tamaño; f++) {
                    System.out.println("  - Marcando casilla [" + f + "," + columna + "]");
                    botonesTablero[f][columna].setBackground(colorBarco);
                    botonesTablero[f][columna].setText(tipoBarco.substring(0, 1));
                    botonesTablero[f][columna].setOpaque(true);
                    botonesTablero[f][columna].setBorderPainted(true);
                    botonesTablero[f][columna].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                    botonesTablero[f][columna].repaint();
                }
            }
            
            SwingUtilities.invokeLater(() -> {
                if (tableroPanel != null) {
                    tableroPanel.repaint();
                    tableroPanel.revalidate();
                    
                    if (tableroPanel.getParent() != null) {
                        tableroPanel.getParent().repaint();
                        tableroPanel.getParent().revalidate();
                    }
                }
                
                System.out.println("[TABLERO] Actualización visual completada");
            });
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error marcando barco: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
    
    public void deshabilitarSeleccion() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tamaño; i++) {
                for (int j = 0; j < tamaño; j++) {
                    botonesTablero[i][j].setEnabled(false);
                }
            }
        });
    }
    
    public boolean esCasillaOcupada(int fila, int columna) {
        if (!esPosicionValida(fila, columna)) {
            return true;
        }
        
        JButton casilla = botonesTablero[fila][columna];
        String texto = casilla.getText();
        return texto != null && !texto.isEmpty();
    }
    
    private boolean esCasillaVacia(int fila, int columna) {
        return !esCasillaOcupada(fila, columna);
    }
    
    private boolean esPosicionValida(int fila, int columna) {
        return fila >= 0 && fila < tamaño && columna >= 0 && columna < tamaño;
    }
    
    public JPanel getTableroPanel() {
        return tableroPanel;
    }
    
    public int getTamaño() {
        return tamaño;
    }
    
    private void verificarVariables() {
        System.out.println("==== VERIFICACIÓN VARIABLES TABLERO ====");
        System.out.println("botonesTablero existe: " + (botonesTablero != null ? "SÍ" : "NO"));
        System.out.println("tableroPanel existe: " + (tableroPanel != null ? "SÍ" : "NO"));
        System.out.println("tamaño tablero: " + tamaño);
        System.out.println("=======================================");
    }
    
    public void manejarClick(int fila, int columna) {
        Point punto = new Point(fila, columna);
        
        if (casillasOcupadas.contains(punto) || seleccionActual.contains(punto)) {
            return;
        }
        
        seleccionActual.add(punto);
        botonesTablero[fila][columna].setBackground(new Color(144, 238, 144));
        
        int tamBarcoActual = barcosPendientes[barcoActual];
        
        if (seleccionActual.size() == 2) {
            if (!estanAlineadas(seleccionActual.get(0), seleccionActual.get(1))) {
                mostrarError("Las casillas deben estar alineadas horizontal o verticalmente");
                return;
            }
        }
        
        if (seleccionActual.size() > 2) {
            if (!sigueLineaRecta(seleccionActual)) {
                mostrarError("Las casillas deben estar en línea recta");
                return;
            }
        }
        
        if (seleccionActual.size() == tamBarcoActual) {
            if (!espacioValido(seleccionActual)) {
                mostrarError("No se puede colocar barco adyacente a otro barco");
                return;
            }
            
            String tipoBarco;
            switch (tamBarcoActual) {
                case 4: tipoBarco = "PORTAVIONES"; break;
                case 3: tipoBarco = "SUBMARINO"; break;
                case 2: tipoBarco = "DESTRUCTOR"; break;
                case 1: tipoBarco = "FRAGATA"; break;
                default: tipoBarco = "DESCONOCIDO"; break;
            }
            
            String orientacion = "HORIZONTAL";
            if (seleccionActual.size() > 1) {
                int primeraFila = seleccionActual.get(0).x;
                boolean esHorizontal = true;
                
                for (int i = 1; i < seleccionActual.size(); i++) {
                    if (seleccionActual.get(i).x != primeraFila) {
                        esHorizontal = false;
                        break;
                    }
                }
                
                orientacion = esHorizontal ? "HORIZONTAL" : "VERTICAL";
            }
            
            Color colorBarco = obtenerColorBarco(tipoBarco);
            for (Point p : seleccionActual) {
                botonesTablero[p.x][p.y].setBackground(colorBarco);
                botonesTablero[p.x][p.y].setText(tipoBarco.substring(0, 1));
                botonesTablero[p.x][p.y].setForeground(Color.WHITE);
                botonesTablero[p.x][p.y].setOpaque(true);
                botonesTablero[p.x][p.y].setBorderPainted(true);
                botonesTablero[p.x][p.y].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
            
            casillasOcupadas.addAll(seleccionActual);
            
            Point primeraCasilla = seleccionActual.get(0);
            ColocacionBarco colocacion = new ColocacionBarco(
                tipoBarco,
                primeraCasilla.x,
                primeraCasilla.y,
                orientacion
            );
            
            if (barcoColocadoListener != null) {
                barcoColocadoListener.accept(colocacion);
            }
            
            seleccionActual.clear();
            
            barcoActual++;
            
            if (barcoActual < barcosPendientes.length) {
                if (labelEstado != null) {
                    labelEstado.setText("Coloque barco de " + barcosPendientes[barcoActual] + " casillas");
                }
            } else {
                if (labelEstado != null) {
                    labelEstado.setText("¡Todos los barcos colocados! Presione Finalizar.");
                }
            }
        }
    }
    
    private boolean estanAlineadas(Point p1, Point p2) {
        return p1.x == p2.x || p1.y == p2.y;
    }

    private boolean sigueLineaRecta(List<Point> puntos) {
        if (puntos.size() <= 2) return true;
        
        boolean mismaFila = true;
        int fila = puntos.get(0).x;
        for (int i = 1; i < puntos.size(); i++) {
            if (puntos.get(i).x != fila) {
                mismaFila = false;
                break;
            }
        }
        
        boolean mismaColumna = true;
        int columna = puntos.get(0).y;
        for (int i = 1; i < puntos.size(); i++) {
            if (puntos.get(i).y != columna) {
                mismaColumna = false;
                break;
            }
        }
        
        if (mismaFila) {
            List<Point> ordenadas = new ArrayList<>(puntos);
            ordenadas.sort(Comparator.comparing(p -> p.y));
            
            for (int i = 1; i < ordenadas.size(); i++) {
                if (ordenadas.get(i).y != ordenadas.get(i-1).y + 1) {
                    return false;
                }
            }
        } else if (mismaColumna) {
            List<Point> ordenadas = new ArrayList<>(puntos);
            ordenadas.sort(Comparator.comparing(p -> p.x));
            
            for (int i = 1; i < ordenadas.size(); i++) {
                if (ordenadas.get(i).x != ordenadas.get(i-1).x + 1) {
                    return false;
                }
            }
        } else {
            return false;
        }
        
        return true;
    }

    private boolean espacioValido(List<Point> puntos) {
        for (Point p : puntos) {
            for (int i = p.x - 1; i <= p.x + 1; i++) {
                for (int j = p.y - 1; j <= p.y + 1; j++) {
                    if (i >= 0 && i < tamaño && j >= 0 && j < tamaño) {
                        Point adyacente = new Point(i, j);
                        if (casillasOcupadas.contains(adyacente) && !puntos.contains(adyacente)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        
        for (Point p : seleccionActual) {
            botonesTablero[p.x][p.y].setBackground(Color.BLUE);
            botonesTablero[p.x][p.y].setText("");
        }
        seleccionActual.clear();
    }

    private Color obtenerColorBarco(String tipoBarco) {
        switch (tipoBarco.toUpperCase()) {
            case "PORTAVIONES": return new Color(255, 0, 0); 
            case "SUBMARINO": return new Color(0, 255, 255);   
            case "DESTRUCTOR": return new Color(0, 128, 0); 
            case "FRAGATA": return new Color(255, 165, 0); 
            default: return Color.GRAY;
        }
    }

    public JPanel getTableroPanelPropio() {
        return tableroPanel;
    }

    private JPanel tableroPanelRival;

    public JPanel getTableroPanelRival() {
        if (tableroPanelRival == null) {
            tableroPanelRival = crearTableroRival();
        }
        return tableroPanelRival;
    }

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
                    if (btn.getText().isEmpty()) {
                        for (ActionListener al : btn.getActionListeners()) {
                            btn.removeActionListener(al);
                        }
                        btn.setEnabled(true);
                        final int fila = i;
                        final int columna = j;
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

    public void marcarAguaEnPropio(int fila, int columna) {
        if (botonesTablero == null) return;
        JButton btn = botonesTablero[fila][columna];
        btn.setBackground(Color.CYAN);
        btn.setText("~");
    }

    public void marcarTocadoEnPropio(int fila, int columna) {
        if (botonesTablero == null) return;
        JButton btn = botonesTablero[fila][columna];
        btn.setBackground(Color.RED);
        btn.setText("X");
    }

    public void marcarHundidoEnPropio(int fila, int columna) {
        if (botonesTablero == null) return;
        JButton btn = botonesTablero[fila][columna];
        btn.setBackground(Color.BLACK);
        btn.setForeground(Color.WHITE);
        btn.setText("☠");
    }

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