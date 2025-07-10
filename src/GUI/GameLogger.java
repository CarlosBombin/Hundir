package GUI;

import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase que gestiona el sistema de logging del juego.
 * Proporciona diferentes niveles de log (info, error, success, warning)
 * y los muestra en un área de texto con timestamps automáticos.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class GameLogger {
    
    /** Formato para mostrar la hora en los logs */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /** Área de texto donde se muestran los logs */
    private final JTextArea areaLog;
    
    /**
     * Constructor que inicializa el logger con el área de texto especificada.
     * 
     * @param areaLog Área de texto donde se mostrarán los mensajes de log
     */
    public GameLogger(JTextArea areaLog) {
        this.areaLog = areaLog;
    }
    
    /**
     * Registra un mensaje con nivel INFO.
     * Es el método principal para logging general.
     * 
     * @param mensaje Mensaje a registrar en el log
     */
    public void log(String mensaje) {
        log(mensaje, LogLevel.INFO);
    }
    
    /**
     * Registra un mensaje con el nivel de log especificado.
     * Añade timestamp automáticamente y actualiza la interfaz en el EDT.
     * 
     * @param mensaje Mensaje a registrar
     * @param level Nivel de importancia del mensaje
     */
    public void log(String mensaje, LogLevel level) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(TIME_FORMATTER);
            String prefijo = level.getPrefix();
            areaLog.append(String.format("[%s] %s%s%n", timestamp, prefijo, mensaje));
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }
    
    /**
     * Registra un mensaje de error con prefijo especial.
     * Utilizado para errores críticos y problemas de funcionamiento.
     * 
     * @param mensaje Mensaje de error a registrar
     */
    public void logError(String mensaje) {
        log(mensaje, LogLevel.ERROR);
    }
    
    /**
     * Registra un mensaje de éxito con prefijo especial.
     * Utilizado para confirmar operaciones completadas correctamente.
     * 
     * @param mensaje Mensaje de éxito a registrar
     */
    public void logSuccess(String mensaje) {
        log(mensaje, LogLevel.SUCCESS);
    }
    
    /**
     * Registra un mensaje de advertencia con prefijo especial.
     * Utilizado para situaciones que requieren atención pero no son errores.
     * 
     * @param mensaje Mensaje de advertencia a registrar
     */
    public void logWarning(String mensaje) {
        log(mensaje, LogLevel.WARNING);
    }
    
    /**
     * Enumeración que define los diferentes niveles de log disponibles.
     * Cada nivel tiene un prefijo específico para identificación visual.
     */
    public enum LogLevel {
        /** Información general sin prefijo especial */
        INFO(""),
        /** Errores críticos con prefijo [ERROR] */
        ERROR("[ERROR] "),
        /** Operaciones exitosas con prefijo [OK] */
        SUCCESS("[OK] "),
        /** Advertencias con prefijo [WARN] */
        WARNING("[WARN] ");
        
        /** Prefijo que se muestra antes del mensaje */
        private final String prefix;
        
        /**
         * Constructor del nivel de log.
         * 
         * @param prefix Prefijo a mostrar antes del mensaje
         */
        LogLevel(String prefix) {
            this.prefix = prefix;
        }
        
        /**
         * Obtiene el prefijo asociado al nivel de log.
         * 
         * @return Prefijo como String
         */
        public String getPrefix() {
            return prefix;
        }
    }
}