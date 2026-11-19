package view;

import dao.AgendamentoDAO;
import model.Agendamento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CancelamentosPanel extends JPanel {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final AgendamentoDAO dao = new AgendamentoDAO();
    private final DefaultTableModel modelo;
    private final JTable tabela;

    public CancelamentosPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Cancelamento de Consultas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(lblTitulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Paciente", "Médico", "Data/Hora", "Tipo", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btnCancelar = new JButton("Cancelar Consulta Selecionada");
        btnCancelar.addActionListener(e -> cancelarConsulta());
        
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(btnCancelar);
        add(rodape, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                carregarAgendamentos();
            }
        });
    }

    private void carregarAgendamentos() {
        try {
            modelo.setRowCount(0);
            List<Agendamento> lista = dao.listarAtivos();
            for (Agendamento a : lista) {
                modelo.addRow(new Object[]{
                    a.id,
                    a.nomePaciente,
                    a.nomeMedico,
                    a.dataHora.format(DATA_HORA),
                    a.tipo,
                    a.status
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar agendamentos: " + e.getMessage());
        }
    }

    private void cancelarConsulta() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma consulta para cancelar.");
            return;
        }

        int id = (Integer) modelo.getValueAt(linha, 0);
        String motivo = JOptionPane.showInputDialog(this, "Informe o motivo do cancelamento:");

        if (motivo == null) {
            return;   // o usuário fechou a janela / clicou em Cancelar
        }

        if (motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O motivo do cancelamento é obrigatório.");
            return;
        }

        try {
            dao.cancelar(id, motivo.trim());
            JOptionPane.showMessageDialog(this, "Consulta cancelada com sucesso!");
            carregarAgendamentos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao cancelar consulta: " + e.getMessage());
            carregarAgendamentos();   // a consulta pode ter sido alterada por outra pessoa
        }
    }
}
