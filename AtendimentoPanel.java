package view;

import dao.AgendamentoDAO;
import dao.AtendimentoDAO;
import model.Agendamento;
import model.Atendimento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.format.DateTimeFormatter;

public class AtendimentoPanel extends JPanel {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private final JComboBox<CadastroPanel.Opcao> cbAgendamento = new JComboBox<>();
    private final JTextArea txtSintomas = new JTextArea(5, 30);
    private final JTextArea txtDiagnostico = new JTextArea(5, 30);
    private final JTextArea txtReceita = new JTextArea(5, 30);
    private final AtendimentoDAO dao = new AtendimentoDAO();
    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

    public AtendimentoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Módulo de Atendimento / Prontuário");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Consulta agendada:"), gbc);
        gbc.gridx = 1; form.add(cbAgendamento, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Sintomas / Queixa:"), gbc);
        gbc.gridx = 1; form.add(new JScrollPane(txtSintomas), gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Diagnóstico:"), gbc);
        gbc.gridx = 1; form.add(new JScrollPane(txtDiagnostico), gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Prescrição / Receita:"), gbc);
        gbc.gridx = 1; form.add(new JScrollPane(txtReceita), gbc);

        add(form, BorderLayout.CENTER);

        JButton btnSalvar = new JButton("Finalizar Atendimento");
        btnSalvar.addActionListener(e -> salvar());

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(btnSalvar);
        add(rodape, BorderLayout.SOUTH);

        // Sempre que a tela for exibida, lista as consultas que ainda aguardam atendimento
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                carregarAgendamentos();
            }
        });
    }

    /** Preenche a lista só com consultas ativas (nem canceladas, nem já atendidas). */
    private void carregarAgendamentos() {
        try {
            cbAgendamento.removeAllItems();

            for (Agendamento a : agendamentoDAO.listarAtivos()) {
                String texto = a.dataHora.format(DATA_HORA) + " - "
                    + a.nomePaciente + " / " + a.nomeMedico + " (" + a.tipo + ")";
                cbAgendamento.addItem(new CadastroPanel.Opcao(a.id, texto));
            }

            cbAgendamento.setSelectedIndex(-1);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Não foi possível carregar os agendamentos:\n" + e.getMessage());
        }
    }

    private void salvar() {
        CadastroPanel.Opcao escolhida = (CadastroPanel.Opcao) cbAgendamento.getSelectedItem();

        if (escolhida == null) {
            JOptionPane.showMessageDialog(this, "Selecione a consulta que está sendo atendida.");
            return;
        }

        if (txtSintomas.getText().trim().isEmpty()
                && txtDiagnostico.getText().trim().isEmpty()
                && txtReceita.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha ao menos um campo do prontuário.");
            return;
        }

        try {
            Atendimento a = new Atendimento();
            a.codigoAgendamento = escolhida.id;
            a.sintomas = txtSintomas.getText().trim();
            a.diagnostico = txtDiagnostico.getText().trim();
            a.receita = txtReceita.getText().trim();

            dao.salvar(a);
            JOptionPane.showMessageDialog(this, "Atendimento registrado e prontuário salvo!");
            limpar();
            carregarAgendamentos();   // a consulta atendida sai da lista
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            carregarAgendamentos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar atendimento: " + e.getMessage());
        }
    }

    private void limpar() {
        cbAgendamento.setSelectedIndex(-1);
        txtSintomas.setText("");
        txtDiagnostico.setText("");
        txtReceita.setText("");
    }
}