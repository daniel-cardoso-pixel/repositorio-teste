package dao;

import model.Convenio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConvenioDAO {

    public List<Convenio> listar() throws SQLException {

        String sql = "SELECT Codigo_Convenio, Empresa_Convenio, CNPJ, Telefone "
            + "FROM convenios ORDER BY Empresa_Convenio";

        List<Convenio> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Convenio cv = new Convenio();
                cv.id = rs.getInt(1);
                cv.empresa = rs.getString(2);
                cv.cnpj = rs.getString(3);
                cv.telefone = rs.getString(4);
                lista.add(cv);
            }
        }

        return lista;
    }

    public int inserir(Convenio cv) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO convenios (Empresa_Convenio, CNPJ, Telefone) "
                 + "VALUES (?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cv.empresa);
            ps.setString(2, Banco.nulo(cv.cnpj));
            ps.setString(3, Banco.nulo(cv.telefone));
            ps.executeUpdate();

            return Banco.chaveGerada(ps);
        }
    }

    public void atualizar(Convenio cv) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE convenios SET Empresa_Convenio=?, CNPJ=?, Telefone=? "
                 + "WHERE Codigo_Convenio=?")) {

            ps.setString(1, cv.empresa);
            ps.setString(2, Banco.nulo(cv.cnpj));
            ps.setString(3, Banco.nulo(cv.telefone));
            ps.setInt(4, cv.id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM convenios WHERE Codigo_Convenio=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
