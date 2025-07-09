package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ManejadorTablero {
    
    private static final Color COLOR_CASILLA_VACIA = Color.LIGHT_GRAY;
    private static final Color COLOR_CASILLA_SELECCIONABLE = Color.BLUE;
    private static final Font FONT_CASILLA = new Font("Arial", Font.BOLD, 10);
    private static final Dimension CASILLA_SIZE = new Dimension(45, 45);
    
    private final int tamaño;
    private JButton[][] botonesTablero;
    private JPanel tableroPanel;
    
    // AGREGAR estos campos a la clase:
    private List<Point> seleccionActual = new ArrayList<>();
    private List<Point> casillasOcupadas = new ArrayList<>();
    private int[] barcosPendientes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1}; // Portaviones, Submarinos, Destructores, Fragatas
    private int barcoActual = 0;
    private JLabel labelEstado; // Para mostrar indicaciones
    private Consumer<ColocacionBarco> barcoColocadoListener;
    
    public ManejadorTablero(int tamaño) {
        this.tamaño = tamaño;
    }
    
    // MÉTODO para establecer dependencias
    public void setDependencias(JLabel labelEstado, Consumer<ColocacionBarco> barcoColocadoListener) {
        this.labelEstado = labelEstado;
        this.barcoColocadoListener = barcoColocadoListener;
        // Actualizar etiqueta inicial
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
        
        verificarVariables(); // Verificar variables después de crear el tablero
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
    
    /**
     * Marca visualmente un barco en el tablero
     */
    public void marcarBarco(ColocacionBarco colocacion) {
        try {
            int fila = colocacion.getFila();
            int columna = colocacion.getColumna();
            String tipoBarco = colocacion.getTipoBarco();
            String orientacion = colocacion.getOrientacion();
            
            System.out.println("[TABLERO] Marcando barco: " + tipoBarco + " en [" + fila + "," + columna + "] " + orientacion);
            
            // Determinar longitud y color según tipo
            int longitud;
            Color colorBarco;
            
            switch (tipoBarco.toUpperCase()) {
                case "PORTAVIONES":
                    longitud = 4;
                    colorBarco = new Color(255, 0, 0); // Rojo
                    break;
                case "SUBMARINO":
                    longitud = 3;
                    colorBarco = new Color(0, 0, 255); // Azul
                    break;
                case "DESTRUCTOR":
                    longitud = 2;
                    colorBarco = new Color(0, 128, 0); // Verde
                    break;
                case "FRAGATA":
                    longitud = 1;
                    colorBarco = new Color(255, 165, 0); // Naranja
                    break;
                default:
                    longitud = 1;
                    colorBarco = Color.GRAY;
                    break;
            }
            
            // CORRECCIÓN CRÍTICA: Usar botonesTablero y no hacer referencia a casillas
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
            } else { // VERTICAL
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
            
            // CORRECCIÓN CRÍTICA: Repintar el panel completo sin redundancias
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
    
    // REEMPLAZAR método para manejar clicks:
    public void manejarClick(int fila, int columna) {
        Point punto = new Point(fila, columna);
        
        // Ignorar click en casilla ya ocupada o seleccionada
        if (casillasOcupadas.contains(punto) || seleccionActual.contains(punto)) {
            return;
        }
        
        // Agregar a selección actual
        seleccionActual.add(punto);
        botonesTablero[fila][columna].setBackground(new Color(144, 238, 144)); // Verde claro
        
        // Obtener tamaño del barco actual
        int tamBarcoActual = barcosPendientes[barcoActual];
        
        // Si ya hay 2 casillas, verificar que estén alineadas
        if (seleccionActual.size() == 2) {
            if (!estanAlineadas(seleccionActual.get(0), seleccionActual.get(1))) {
                mostrarError("Las casillas deben estar alineadas horizontal o verticalmente");
                return;
            }
        }
        
        // Si hay más de 2 casillas, verificar que sigan la misma línea
        if (seleccionActual.size() > 2) {
            if (!sigueLineaRecta(seleccionActual)) {
                mostrarError("Las casillas deben estar en línea recta");
                return;
            }
        }
        
        // Si ya seleccionamos el número correcto de casillas para este barco
        if (seleccionActual.size() == tamBarcoActual) {
            // Verificar que no haya barcos adyacentes
            if (!espacioValido(seleccionActual)) {
                mostrarError("No se puede colocar barco adyacente a otro barco");
                return;
            }
            
            // Determinar tipo y orientación del barco según su tamaño
            String tipoBarco;
            switch (tamBarcoActual) {
                case 4: tipoBarco = "PORTAVIONES"; break;
                case 3: tipoBarco = "SUBMARINO"; break;
                case 2: tipoBarco = "DESTRUCTOR"; break;
                case 1: tipoBarco = "FRAGATA"; break;
                default: tipoBarco = "DESCONOCIDO"; break;
            }
            
            // Determinar orientación (por defecto horizontal)
            String orientacion = "HORIZONTAL";
            if (seleccionActual.size() > 1) {
                // Si la fila es la misma en todas las casillas, es horizontal
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
            
            // Marcar barco en el tablero
            Color colorBarco = obtenerColorBarco(tipoBarco);
            for (Point p : seleccionActual) {
                botonesTablero[p.x][p.y].setBackground(colorBarco);
                botonesTablero[p.x][p.y].setText(tipoBarco.substring(0, 1));
                botonesTablero[p.x][p.y].setForeground(Color.WHITE);
                botonesTablero[p.x][p.y].setOpaque(true);
                botonesTablero[p.x][p.y].setBorderPainted(true);
                botonesTablero[p.x][p.y].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
            
            // Agregar a casillas ocupadas
            casillasOcupadas.addAll(seleccionActual);
            
            // Crear objeto de colocación
            Point primeraCasilla = seleccionActual.get(0);
            ColocacionBarco colocacion = new ColocacionBarco(
                tipoBarco,
                primeraCasilla.x,
                primeraCasilla.y,
                orientacion
            );
            
            // Notificar colocación
            if (barcoColocadoListener != null) {
                barcoColocadoListener.accept(colocacion);
            }
            
            // Limpiar selección actual
            seleccionActual.clear();
            
            // Avanzar al siguiente barco
            barcoActual++;
            
            // Actualizar estado
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
    
    // AGREGAR métodos auxiliares:
    private boolean estanAlineadas(Point p1, Point p2) {
        // Las casillas están alineadas si comparten fila o columna
        return p1.x == p2.x || p1.y == p2.y;
    }

    private boolean sigueLineaRecta(List<Point> puntos) {
        if (puntos.size() <= 2) return true;
        
        // Verificar si todas comparten fila (horizontal)
        boolean mismaFila = true;
        int fila = puntos.get(0).x;
        for (int i = 1; i < puntos.size(); i++) {
            if (puntos.get(i).x != fila) {
                mismaFila = false;
                break;
            }
        }
        
        // Verificar si todas comparten columna (vertical)
        boolean mismaColumna = true;
        int columna = puntos.get(0).y;
        for (int i = 1; i < puntos.size(); i++) {
            if (puntos.get(i).y != columna) {
                mismaColumna = false;
                break;
            }
        }
        
        // Verificar que están contiguas
        if (mismaFila) {
            // Ordenar por columna
            List<Point> ordenadas = new ArrayList<>(puntos);
            ordenadas.sort(Comparator.comparing(p -> p.y));
            
            // Verificar que son consecutivas
            for (int i = 1; i < ordenadas.size(); i++) {
                if (ordenadas.get(i).y != ordenadas.get(i-1).y + 1) {
                    return false;
                }
            }
        } else if (mismaColumna) {
            // Ordenar por fila
            List<Point> ordenadas = new ArrayList<>(puntos);
            ordenadas.sort(Comparator.comparing(p -> p.x));
            
            // Verificar que son consecutivas
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
        // Verificar que ninguna casilla adyacente esté ocupada
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
        
        // Limpiar selección actual
        for (Point p : seleccionActual) {
            botonesTablero[p.x][p.y].setBackground(Color.BLUE);
            botonesTablero[p.x][p.y].setText("");
        }
        seleccionActual.clear();
    }

    private Color obtenerColorBarco(String tipoBarco) {
        switch (tipoBarco.toUpperCase()) {
            case "PORTAVIONES": return new Color(255, 0, 0);  // Rojo
            case "SUBMARINO": return new Color(0, 0, 255);    // Azul
            case "DESTRUCTOR": return new Color(0, 128, 0);   // Verde
            case "FRAGATA": return new Color(255, 165, 0);    // Naranja
            default: return Color.GRAY;
        }
    }
}