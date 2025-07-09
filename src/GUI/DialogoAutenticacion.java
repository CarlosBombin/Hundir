package GUI;

import javax.swing.*;
import java.awt.*;
import Cliente.Usuario;

public class DialogoAutenticacion extends JDialog {
    
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JRadioButton rbRegistro;
    private JRadioButton rbLogin;
    private JButton btnAceptar;
    private JButton btnCancelar;
    
    private Usuario usuarioResultado;
    private boolean aceptado;
    
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
    
    private void inicializarComponentes() {
        txtUsuario = new JTextField(20);
        txtPassword = new JPasswordField(20);
        
        rbRegistro = new JRadioButton("Registro (nuevo usuario)", true);
        rbLogin = new JRadioButton("Login (usuario existente)");
        
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbRegistro);
        grupo.add(rbLogin);
        
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");
        
        // Enter en password = clic en aceptar
        txtPassword.addActionListener(e -> btnAceptar.doClick());
    }
    
    private void configurarLayout() {
        setLayout(new BorderLayout());
        
        // Panel central con campos
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panelCampos.add(new JLabel("Usuario:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panelCampos.add(txtUsuario, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panelCampos.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panelCampos.add(txtPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panelCampos.add(rbRegistro, gbc);
        
        gbc.gridy = 3;
        panelCampos.add(rbLogin, gbc);
        
        add(panelCampos, BorderLayout.CENTER);
        
        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        
        add(panelBotones, BorderLayout.SOUTH);
        
        // Título
        JLabel lblTitulo = new JLabel("Hundir la Flota - Autenticación", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);
    }
    
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
    
    private boolean validarCampos() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
            txtUsuario.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña no puede estar vacía", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return false;
        }
        
        if (usuario.length() < 3) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario debe tener al menos 3 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            txtUsuario.requestFocus();
            return false;
        }
        
        if (password.length() < 3) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 3 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public Usuario mostrarDialogo() {
        setVisible(true);
        return aceptado ? usuarioResultado : null;
    }
}