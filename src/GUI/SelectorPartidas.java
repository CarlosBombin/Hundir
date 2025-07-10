package GUI;

import javax.swing.*;

/**
 * Diálogo para seleccionar una partida disponible de una lista.
 * Permite al usuario elegir entre las partidas disponibles en el servidor
 * mediante un diálogo de selección intuitivo.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class SelectorPartidas {
    
    /** Ventana padre para el diálogo modal */
    private final JFrame parent;
    /** String con las partidas disponibles separadas por delimitadores */
    private final String partidasStr;
    
    /**
     * Constructor que inicializa el selector de partidas.
     * 
     * @param parent Ventana padre para el diálogo modal
     * @param partidasStr String con las partidas disponibles
     */
    public SelectorPartidas(JFrame parent, String partidasStr) {
        this.parent = parent;
        this.partidasStr = partidasStr;
    }
    
    /**
     * Muestra el diálogo de selección de partidas.
     * Parsea la lista de partidas y permite al usuario elegir una.
     * 
     * @return Partida seleccionada por el usuario o null si canceló
     */
    public String mostrarDialogo() {
        String[] partidas = partidasStr.split("\\|");
        
        if (partidas.length == 0) {
            JOptionPane.showMessageDialog(parent, "No hay partidas disponibles", 
                                        "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        
        return (String) JOptionPane.showInputDialog(
            parent,
            "Seleccione una partida:",
            "Partidas Disponibles",
            JOptionPane.QUESTION_MESSAGE,
            null,
            partidas,
            partidas[0]
        );
    }
}