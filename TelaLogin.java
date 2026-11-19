package view;

import dao.UsuarioDAO;
import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class TelaLogin extends JFrame {
    private final JTextField txtUsuario = new JTextField(15);
    private final JPasswordField txtSenha = new JPasswordField(15);

    public TelaLogin() {
        super("Clínica Médica - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Usuário:"), g);
        g.gridx = 1; form.add(txtUsuario, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Senha:"), g);
        g.gridx = 1; form.add(txtSenha, g);

        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.addActionListener(e -> entrar());
        getRootPane().setDefaultButton(btnEntrar);   // Enter aciona o botão

        JPanel rodape = new JPanel();
        rodape.add(btnEntrar);

        add(form, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void entrar() {
        String id = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        if (id.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe usuário e senha.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Usuario u = new UsuarioDAO().autenticar(id, senha);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                txtSenha.setText("");
                return;
            }
            new TelaPrincipal(u).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao acessar o banco: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
