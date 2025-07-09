package GUI;

import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameLogger {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final JTextArea areaLog;
    
    public GameLogger(JTextArea areaLog) {
        this.areaLog = areaLog;
    }
    
    public void log(String mensaje) {
        log(mensaje, LogLevel.INFO);
    }
    
    public void log(String mensaje, LogLevel level) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMATTER);
            String prefijo = level.getPrefix();
            areaLog.append(String.format("[%s] %s%s%n", timestamp, prefijo, mensaje));
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }
    
    public void logError(String mensaje) {
        log(mensaje, LogLevel.ERROR);
    }
    
    public void logSuccess(String mensaje) {
        log(mensaje, LogLevel.SUCCESS);
    }
    
    public void logWarning(String mensaje) {
        log(mensaje, LogLevel.WARNING);
    }
    
    public enum LogLevel {
        INFO(""),
        ERROR("[ERROR] "),
        SUCCESS("[OK] "),
        WARNING("[WARN] ");
        
        private final String prefix;
        
        LogLevel(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
    }
}