package GUI;

import javax.swing.*;

public class SelectorPartidas {
    
    private final JFrame parent;
    private final String partidasStr;
    
    public SelectorPartidas(JFrame parent, String partidasStr) {
        this.parent = parent;
        this.partidasStr = partidasStr;
    }
    
    public String mostrarDialogo() {
        String[] partidas = partidasStr.split("\\|");
        
        if (partidas.length == 0) {
            JOptionPane.showMessageDialog(parent, "No hay partidas disponibles", "Info", JOptionPane.INFORMATION_MESSAGE);
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