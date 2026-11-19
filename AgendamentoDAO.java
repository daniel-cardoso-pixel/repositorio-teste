package dao;

import model.Agendamento;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Agendamentos gravados na tabela agenda_consulta.
 *
 * O banco guarda Data (DATE) e Hora ("HH:mm") separadas, Retorno/Cancelado/Atendido
 * como 'S'/'N'. Este DAO converte tudo para o modelo Agendamento
 * (dataHora, tipo, status).
 */
public class AgendamentoDAO {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String MSG_INATIVA =
        "Esta consulta não está mais ativa (já foi cancelada, atendida ou removida).";

    private static final String SELECT =
        "SELECT a.Registro_Agenda, a.Codigo_Paciente, p.Nome AS Paciente, "
        + "a.Codigo_Medico, m.Nome_Medico AS Medico, a.Data, a.Hora, "
        + "a.Retorno, a.Observacao, a.Cancelado, a.Atendido "
        + "FROM agenda_consulta a "
        + "JOIN pacientes p ON p.Codigo_Paciente = a.Codigo_Paciente "
        + "JOIN medicos m ON m.Codigo_Medico = a.Codigo_Medico ";

    /** Todas as consultas (inclusive canceladas e atendidas), das mais recentes às mais antigas. */
    public List<Agendamento> listar() throws SQLException {
        return consultar(SELECT + "ORDER BY a.Data DESC, a.Hora DESC");
    }

    /** Somente consultas ainda agendadas (não canceladas e não atendidas). */
    public List<Agendamento> listarAtivos() throws SQLException {
        return consultar(SELECT
            + "WHERE a.Cancelado = 'N' AND a.Atendido = 'N' "
            + "ORDER BY a.Data, a.Hora");
    }

    public int inserir(Agendamento a) throws SQLException {
        String sql = "INSERT INTO agenda_consulta "
            + "(Codigo_Usuario, Codigo_Paciente, Codigo_Medico, Data, Hora, Retorno, Observacao) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Banco.abrir()) {
            verificarConflito(c, a);

            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, a.codigoUsuario);
                ps.setInt(2, a.codigoPaciente);
                ps.setInt(3, a.codigoMedico);
                ps.setDate(4, Date.valueOf(a.dataHora.toLocalDate()));
                ps.setString(5, a.dataHora.format(HORA));
                ps.setString(6, "Retorno".equals(a.tipo) ? "S" : "N");
                ps.setString(7, Banco.nulo(a.observacao));
                ps.executeUpdate();

                return Banco.chaveGerada(ps);
            }
        }
    }

    /** Só altera consultas ainda ativas. */
    public void atualizar(Agendamento a) throws SQLException {
        String sql = "UPDATE agenda_consulta SET Codigo_Paciente=?, Codigo_Medico=?, "
            + "Data=?, Hora=?, Retorno=?, Observacao=? "
            + "WHERE Registro_Agenda=? AND Cancelado='N' AND Atendido='N'";

        try (Connection c = Banco.abrir()) {
            verificarConflito(c, a);

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, a.codigoPaciente);
                ps.setInt(2, a.codigoMedico);
                ps.setDate(3, Date.valueOf(a.dataHora.toLocalDate()));
                ps.setString(4, a.dataHora.format(HORA));
                ps.setString(5, "Retorno".equals(a.tipo) ? "S" : "N");
                ps.setString(6, Banco.nulo(a.observacao));
                ps.setInt(7, a.id);

                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException(MSG_INATIVA);
                }
            }
        }
    }

    /** Marca como cancelada e guarda o motivo (a observação original é preservada). */
    public void cancelar(int id, String motivo) throws SQLException {
        String sql = "UPDATE agenda_consulta SET Cancelado='S', Motivo_Cancelamento=? "
            + "WHERE Registro_Agenda=? AND Cancelado='N' AND Atendido='N'";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, motivo);
            ps.setInt(2, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException(MSG_INATIVA);
            }
        }
    }

    /** Só remove consultas ativas; canceladas e atendidas ficam como histórico. */
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM agenda_consulta "
            + "WHERE Registro_Agenda=? AND Cancelado='N' AND Atendido='N'";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException(MSG_INATIVA);
            }
        }
    }

    /**
     * Impede dois agendamentos ativos no mesmo dia e horário para o mesmo
     * médico ou para o mesmo paciente (a própria consulta é ignorada ao editar).
     */
    private void verificarConflito(Connection c, Agendamento a) throws SQLException {
        String sql = "SELECT Codigo_Medico FROM agenda_consulta "
            + "WHERE Data=? AND Hora=? AND Cancelado='N' "
            + "AND (Codigo_Medico=? OR Codigo_Paciente=?) AND Registro_Agenda<>?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(a.dataHora.toLocalDate()));
            ps.setString(2, a.dataHora.format(HORA));
            ps.setInt(3, a.codigoMedico);
            ps.setInt(4, a.codigoPaciente);
            ps.setInt(5, a.id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) == a.codigoMedico) {
                        throw new IllegalArgumentException(
                            "O médico já tem uma consulta marcada neste dia e horário.");
                    }
                    throw new IllegalArgumentException(
                        "O paciente já tem uma consulta marcada neste dia e horário.");
                }
            }
        }
    }

    private List<Agendamento> consultar(String sql) throws SQLException {
        List<Agendamento> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    private Agendamento mapear(ResultSet rs) throws SQLException {
        Agendamento a = new Agendamento();

        a.id = rs.getInt("Registro_Agenda");
        a.codigoPaciente = rs.getInt("Codigo_Paciente");
        a.nomePaciente = rs.getString("Paciente");
        a.codigoMedico = rs.getInt("Codigo_Medico");
        a.nomeMedico = rs.getString("Medico");
        a.dataHora = LocalDateTime.of(
            rs.getDate("Data").toLocalDate(),
            LocalTime.parse(rs.getString("Hora")));
        a.tipo = "S".equals(rs.getString("Retorno")) ? "Retorno" : "Consulta";
        a.observacao = rs.getString("Observacao");

        if ("S".equals(rs.getString("Cancelado"))) {
            a.status = "Cancelado";
        } else if ("S".equals(rs.getString("Atendido"))) {
            a.status = "Atendido";
        } else {
            a.status = "Agendado";
        }

        return a;
    }
}