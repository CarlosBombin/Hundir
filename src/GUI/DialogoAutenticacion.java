package GUI;

import javax.swing.*;
import java.awt.*;
import Cliente.Usuario;

/**
 * Diálogo modal para la autenticación de usuarios en el juego.
 * Permite tanto el registro de nuevos usuarios como el login de usuarios existentes.
 * Proporciona validación de campos y manejo de eventos para una experiencia
 * de usuario fluida.
 * 
 * @author Sistema Hundir la Flota
 * @version 1.0
 */
public class DialogoAutenticacion extends JDialog {
    
    /** Campo de texto para el nombre de usuario */
    private JTextField txtUsuario;
    /** Campo de contraseña para mayor seguridad */
    private JPasswordField txtPassword;
    /** Radio button para seleccionar registro de nuevo usuario */
    private JRadioButton rbRegistro;
    /** Radio button para seleccionar login de usuario existente */
    private JRadioButton rbLogin;
    /** Botón para confirmar la autenticación */
    private JButton btnAceptar;
    /** Botón para cancelar el proceso */
    private JButton btnCancelar;
    
    /** Usuario resultado de la autenticación */
    private Usuario usuarioResultado;
    /** Indica si el usuario aceptó el diálogo */
    private boolean aceptado;
    
    /**
     * Constructor que inicializa el diálogo de autenticación.
     * Configura la ventana como modal y establece su apariencia básica.
     */
    public DialogoAutenticacion() {
        super((Frame) null, "Autenticación", true);
        inicializarComponentes();
        configurarLayout();
        configurarEventos();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * Inicializa todos los componentes visuales del diálogo.
     * Crea los campos de texto, botones y radio buttons con sus configuraciones básicas.
     */
    private void inicializarComponentes() {
        txtUsuario = new JTextField(20);
        txtPassword = new JPasswordField(20);
        
        rbRegistro = new JRadioButton("Registro (nuevo usuario)", true);
        rbLogin = new JRadioButton("Login (usuario existente)");
        
        // Agrupar radio buttons para selección mutuamente excluyente
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbRegistro);
        grupo.add(rbLogin);
        
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");
        
        // Permitir confirmar con Enter en el campo de contraseña
        txtPassword.addActionListener(e -> btnAceptar.doClick());
    }
    
    /**
     * Configura el layout del diálogo usando GridBagLayout para mejor control.
     * Organiza los componentes en una disposición clara y centrada.
     */
    private void configurarLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con los campos de entrada
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campo de usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panelCampos.add(new JLabel("Usuario:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panelCampos.add(txtUsuario, gbc);
        
        // Campo de contraseña
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panelCampos.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panelCampos.add(txtPassword, gbc);
        
        // Radio buttons para tipo de operación
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panelCampos.add(rbRegistro, gbc);
        
        gbc.gridy = 3;
        panelCampos.add(rbLogin, gbc);
        
        add(panelCampos, BorderLayout.CENTER);
        
        // Panel de botones en la parte inferior
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        
        add(panelBotones, BorderLayout.SOUTH);
        
        // Título en la parte superior
        JLabel lblTitulo = new JLabel("Hundir la Flota - Autenticación", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);
    }
    
    /**
     * Configura los event listeners para botones y campos.
     * Maneja la validación, creación del usuario y cierre del diálogo.
     */
    private void configurarEventos() {
        btnAceptar.addActionListener(e -> {
            if (validarCampos()) {
                String nombre = txtUsuario.getText().trim();
                String password = new String(txtPassword.getPassword());
                boolean esNuevo = rbRegistro.isSelected();
                
                usuarioResultado = new Usuario(nombre, password);
                usuarioResultado.setNuevo(esNuevo);
                aceptado = true;
                dispose();
            }
        });
        
        btnCancelar.addActionListener(e -> {
            aceptado = false;
            dispose();
        });
    }
    
    /**
     * Valida que los campos de entrada cumplan con los requisitos mínimos.
     * Verifica que no estén vacíos y tengan la longitud mínima requerida.
     * 
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validar campo de usuario
        if (usuario.isEmpty()) {
            mostrarError("El nombre de usuario no puede estar vacío", txtUsuario);
            return false;
        }
        
        if (usuario.length() < 3) {
            mostrarError("El nombre de usuario debe tener al menos 3 caracteres", txtUsuario);
            return false;
        }
        
        // Validar campo de contraseña
        if (password.isEmpty()) {
            mostrarError("La contraseña no puede estar vacía", txtPassword);
            return false;
        }
        
        if (password.length() < 3) {
            mostrarError("La contraseña debe tener al menos 3 caracteres", txtPassword);
            return false;
        }
        
        return true;
    }
    
    /**
     * Muestra un mensaje de error y enfoca el campo problemático.
     * 
     * @param mensaje Mensaje de error a mostrar
     * @param campo Campo que debe recibir el foco tras el error
     */
    private void mostrarError(String mensaje, JComponent campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        campo.requestFocus();
    }
    
    /**
     * Muestra el diálogo de forma modal y retorna el usuario autenticado.
     * Bloquea la ejecución hasta que el usuario acepte o cancele.
     * 
     * @return Usuario creado si se aceptó el diálogo, null si se canceló
     */
    public Usuario mostrarDialogo() {
        setVisible(true);
        return aceptado ? usuarioResultado : null;
    }
}