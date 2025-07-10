package GUI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ValidadorColocacionLocal {
    
    public boolean esValidaColocacion(ColocacionBarco colocacion, ManejadorTablero tablero) {
        return validarParametros(colocacion) &&
               validarLimites(colocacion, tablero) &&
               validarOcupacion(colocacion, tablero);
    }
    
    private boolean validarParametros(ColocacionBarco colocacion) {
        if (colocacion.getTipoBarco() == null || colocacion.getOrientacion() == null) {
            mostrarError("Datos de colocación inválidos");
            return false;
        }
        
        if (!BarcoFactory.esTipoValido(colocacion.getTipoBarco())) {
            mostrarError("Tipo de barco inválido: " + colocacion.getTipoBarco());
            return false;
        }
        
        if (!"HORIZONTAL".equals(colocacion.getOrientacion()) && 
            !"VERTICAL".equals(colocacion.getOrientacion())) {
            mostrarError("Orientación inválida: " + colocacion.getOrientacion());
            return false;
        }
        
        return true;
    }
    
    private boolean validarLimites(ColocacionBarco colocacion, ManejadorTablero tablero) {
        BarcoFactory.BarcoInfo info = BarcoFactory.obtenerInfo(colocacion.getTipoBarco());
        int tamaño = info.getTamaño();
        
        for (int i = 0; i < tamaño; i++) {
            int targetFila = colocacion.getFila();
            int targetColumna = colocacion.getColumna();
            
            if ("HORIZONTAL".equals(colocacion.getOrientacion())) {
                targetColumna += i;
            } else {
                targetFila += i;
            }
            
            if (targetFila < 0 || targetFila >= tablero.getTamaño() ||
                targetColumna < 0 || targetColumna >= tablero.getTamaño()) {
                mostrarError("El barco se sale del tablero");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean validarOcupacion(ColocacionBarco colocacion, ManejadorTablero tablero) {
        BarcoFactory.BarcoInfo info = BarcoFactory.obtenerInfo(colocacion.getTipoBarco());
        int tamaño = info.getTamaño();
        
        for (int i = 0; i < tamaño; i++) {
            int targetFila = colocacion.getFila();
            int targetColumna = colocacion.getColumna();
            
            if ("HORIZONTAL".equals(colocacion.getOrientacion())) {
                targetColumna += i;
            } else {
                targetFila += i;
            }
            
            if (tablero.esCasillaOcupada(targetFila, targetColumna)) {
                mostrarError("Ya hay un barco en esa posición");
                return false;
            }
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, mensaje, "Error de Colocación", 
                                        JOptionPane.ERROR_MESSAGE);
        });
    }
}