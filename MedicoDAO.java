package dao;

import model.Medico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAO {

    public List<Medico> listar() throws SQLException {

        String sql = "SELECT m.Codigo_Medico, m.Nome_Medico, m.CRM, "
            + "m.Codigo_Especialidade, e.Descricao_Especialidade "
            + "FROM medicos m "
            + "JOIN especialidades e ON e.Codigo_Especialidade = m.Codigo_Especialidade "
            + "ORDER BY m.Nome_Medico";

        List<Medico> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medico m = new Medico();
                m.id = rs.getInt(1);
                m.nome = rs.getString(2);
                m.crm = rs.getString(3);
                m.codigoEspecialidade = rs.getInt(4);
                m.especialidade = rs.getString(5);
                lista.add(m);
            }
        }

        return lista;
    }

    public int inserir(Medico m) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO medicos (Nome_Medico, Codigo_Especialidade, CRM) "
                 + "VALUES (?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.nome);
            ps.setInt(2, m.codigoEspecialidade);
            ps.setString(3, m.crm);
            ps.executeUpdate();

            return Banco.chaveGerada(ps);
        }
    }

    public void atualizar(Medico m) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE medicos SET Nome_Medico=?, Codigo_Especialidade=?, CRM=? "
                 + "WHERE Codigo_Medico=?")) {

            ps.setString(1, m.nome);
            ps.setInt(2, m.codigoEspecialidade);
            ps.setString(3, m.crm);
            ps.setInt(4, m.id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM medicos WHERE Codigo_Medico=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
