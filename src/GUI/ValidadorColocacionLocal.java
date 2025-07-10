package GUI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Validador local para la colocación de barcos en el tablero.
 * Realiza verificaciones del lado cliente antes de enviar la colocación al servidor,
 * mejorando la experiencia del usuario al proporcionar feedback inmediato.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class ValidadorColocacionLocal {
    
    /**
     * Valida si una colocación de barco es válida según las reglas del juego.
     * Realiza todas las verificaciones necesarias antes de enviar al servidor.
     * 
     * @param colocacion Datos de la colocación a validar
     * @param tablero Tablero donde se quiere colocar el barco
     * @return true si la colocación es válida, false en caso contrario
     */
    public boolean esValidaColocacion(ColocacionBarco colocacion, ManejadorTablero tablero) {
        return validarParametros(colocacion) &&
               validarLimites(colocacion, tablero) &&
               validarOcupacion(colocacion, tablero);
    }
    
    /**
     * Valida que los parámetros básicos de la colocación sean correctos.
     * Verifica que el tipo de barco y orientación sean válidos.
     * 
     * @param colocacion Colocación a validar
     * @return true si los parámetros son válidos, false en caso contrario
     */
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
    
    /**
     * Valida que el barco no se salga de los límites del tablero.
     * Verifica que todas las casillas del barco estén dentro del tablero.
     * 
     * @param colocacion Colocación a validar
     * @param tablero Tablero donde se coloca el barco
     * @return true si el barco cabe en el tablero, false en caso contrario
     */
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
    
    /**
     * Valida que las casillas donde se quiere colocar el barco estén libres.
     * Verifica que no haya otros barcos en las posiciones objetivo.
     * 
     * @param colocacion Colocación a validar
     * @param tablero Tablero donde se coloca el barco
     * @return true si las casillas están libres, false en caso contrario
     */
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
    
    /**
     * Muestra un mensaje de error al usuario de forma thread-safe.
     * Utiliza SwingUtilities.invokeLater para ejecutar en el EDT.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    private void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, mensaje, "Error de Colocación", 
                                        JOptionPane.ERROR_MESSAGE);
        });
    }
}