package dao;

import model.Funcionario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    public List<Funcionario> listar() throws SQLException {

        String sql = "SELECT Codigo_Funcionario, Nome_Completo, Numero_RG, Orgao_Emissor, "
            + "Numero_CPF, Endereco, Numero, Complemento, Bairro, Cidade, Estado, "
            + "Telefone, Celular, Numero_CTPS, Numero_PIS, Data_Nascimento "
            + "FROM funcionarios ORDER BY Nome_Completo";

        List<Funcionario> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Funcionario f = new Funcionario();
                f.id = rs.getInt(1);
                f.nome = rs.getString(2);
                f.rg = rs.getString(3);
                f.orgaoEmissor = rs.getString(4);
                f.cpf = rs.getString(5);
                f.endereco = rs.getString(6);
                f.numero = rs.getString(7);
                f.complemento = rs.getString(8);
                f.bairro = rs.getString(9);
                f.cidade = rs.getString(10);
                f.estado = rs.getString(11);
                f.telefone = rs.getString(12);
                f.celular = rs.getString(13);
                f.ctps = rs.getString(14);
                f.pis = rs.getString(15);

                Date nasc = rs.getDate(16);
                f.nascimento = (nasc == null) ? null : nasc.toLocalDate();

                lista.add(f);
            }
        }

        return lista;
    }

    public int inserir(Funcionario f) throws SQLException {

        String sql = "INSERT INTO funcionarios (Nome_Completo, Numero_RG, Orgao_Emissor, "
            + "Numero_CPF, Endereco, Numero, Complemento, Bairro, Cidade, Estado, "
            + "Telefone, Celular, Numero_CTPS, Numero_PIS, Data_Nascimento) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencher(ps, f);
            ps.executeUpdate();

            return Banco.chaveGerada(ps);
        }
    }

    public void atualizar(Funcionario f) throws SQLException {

        String sql = "UPDATE funcionarios SET Nome_Completo=?, Numero_RG=?, Orgao_Emissor=?, "
            + "Numero_CPF=?, Endereco=?, Numero=?, Complemento=?, Bairro=?, Cidade=?, "
            + "Estado=?, Telefone=?, Celular=?, Numero_CTPS=?, Numero_PIS=?, "
            + "Data_Nascimento=? WHERE Codigo_Funcionario=?";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql)) {

            preencher(ps, f);
            ps.setInt(16, f.id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM funcionarios WHERE Codigo_Funcionario=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Preenche os 15 primeiros parâmetros (comuns ao INSERT e ao UPDATE). */
    private void preencher(PreparedStatement ps, Funcionario f) throws SQLException {
        ps.setString(1, f.nome);
        ps.setString(2, Banco.nulo(f.rg));
        ps.setString(3, Banco.nulo(f.orgaoEmissor));
        ps.setString(4, Banco.nulo(f.cpf));
        ps.setString(5, Banco.nulo(f.endereco));
        ps.setString(6, Banco.nulo(f.numero));
        ps.setString(7, Banco.nulo(f.complemento));
        ps.setString(8, Banco.nulo(f.bairro));
        ps.setString(9, Banco.nulo(f.cidade));
        ps.setString(10, Banco.nulo(f.estado));
        ps.setString(11, Banco.nulo(f.telefone));
        ps.setString(12, Banco.nulo(f.celular));
        ps.setString(13, Banco.nulo(f.ctps));
        ps.setString(14, Banco.nulo(f.pis));

        if (f.nascimento == null) {
            ps.setNull(15, Types.DATE);
        } else {
            ps.setDate(15, Date.valueOf(f.nascimento));
        }
    }
}
