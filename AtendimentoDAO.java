package dao;

import model.Atendimento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Grava o prontuário (tabela prontuario_paciente) e marca a consulta como
 * atendida, tudo na mesma transação.
 *
 * Mapeamento: sintomas -> Historico, diagnostico -> Diagnostico, receita -> Receituario.
 */
public class AtendimentoDAO {

    public void salvar(Atendimento a) throws SQLException {
        try (Connection c = Banco.abrir()) {
            c.setAutoCommit(false);

            try {
                validarAgendamento(c, a.codigoAgendamento);

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO prontuario_paciente "
                        + "(Registro_Agenda, Historico, Diagnostico, Receituario) "
                        + "VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, a.codigoAgendamento);
                    ps.setString(2, Banco.nulo(a.sintomas));
                    ps.setString(3, Banco.nulo(a.diagnostico));
                    ps.setString(4, Banco.nulo(a.receita));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE agenda_consulta SET Atendido='S' WHERE Registro_Agenda=?")) {
                    ps.setInt(1, a.codigoAgendamento);
                    ps.executeUpdate();
                }

                c.commit();

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** A consulta precisa existir e estar ativa (nem cancelada, nem já atendida). */
    private void validarAgendamento(Connection c, int codigo) throws SQLException {
        String sql = "SELECT Cancelado, Atendido FROM agenda_consulta "
            + "WHERE Registro_Agenda=? FOR UPDATE";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Agendamento não encontrado.");
                }
                if ("S".equals(rs.getString(1))) {
                    throw new IllegalArgumentException("Esta consulta foi cancelada.");
                }
                if ("S".equals(rs.getString(2))) {
                    throw new IllegalArgumentException("Esta consulta já foi atendida.");
                }
            }
        }
    }
}