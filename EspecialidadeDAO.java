package dao;

import model.Especialidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadeDAO {

    public List<Especialidade> listar() throws SQLException {

        String sql = "SELECT Codigo_Especialidade, Descricao_Especialidade "
            + "FROM especialidades ORDER BY Descricao_Especialidade";

        List<Especialidade> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Especialidade e = new Especialidade();
                e.id = rs.getInt(1);
                e.descricao = rs.getString(2);
                lista.add(e);
            }
        }

        return lista;
    }

    public int inserir(Especialidade e) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO especialidades (Descricao_Especialidade) VALUES (?)",
                 Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.descricao);
            ps.executeUpdate();

            return Banco.chaveGerada(ps);
        }
    }

    public void atualizar(Especialidade e) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE especialidades SET Descricao_Especialidade=? "
                 + "WHERE Codigo_Especialidade=?")) {

            ps.setString(1, e.descricao);
            ps.setInt(2, e.id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM especialidades WHERE Codigo_Especialidade=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
