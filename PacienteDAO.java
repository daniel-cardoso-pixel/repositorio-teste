package dao;

import model.Paciente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public List<Paciente> listar() throws SQLException {

        String sql = "SELECT p.Codigo_Paciente, p.Nome, p.Numero_CPF, p.Numero_RG, "
            + "p.Orgao_Emissor, p.Data_Nascimento, p.Endereco, p.Numero, p.Complemento, "
            + "p.Bairro, p.Cidade, p.Estado, p.Telefone, p.Celular, "
            + "p.Codigo_Convenio, cv.Empresa_Convenio "
            + "FROM pacientes p "
            + "LEFT JOIN convenios cv ON cv.Codigo_Convenio = p.Codigo_Convenio "
            + "ORDER BY p.Nome";

        List<Paciente> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Paciente p = new Paciente();
                p.id = rs.getInt(1);
                p.nome = rs.getString(2);
                p.cpf = rs.getString(3);
                p.rg = rs.getString(4);
                p.orgaoEmissor = rs.getString(5);

                Date nasc = rs.getDate(6);
                p.nascimento = (nasc == null) ? null : nasc.toLocalDate();

                p.endereco = rs.getString(7);
                p.numero = rs.getString(8);
                p.complemento = rs.getString(9);
                p.bairro = rs.getString(10);
                p.cidade = rs.getString(11);
                p.estado = rs.getString(12);
                p.telefone = rs.getString(13);
                p.celular = rs.getString(14);

                // getInt devolve 0 quando o convênio é NULL (= "Particular")
                p.codigoConvenio = rs.getInt(15);
                p.nomeConvenio = rs.getString(16);

                lista.add(p);
            }
        }

        return lista;
    }

    public int inserir(Paciente p) throws SQLException {

        String sql = "INSERT INTO pacientes (Nome, Numero_CPF, Numero_RG, Orgao_Emissor, "
            + "Data_Nascimento, Endereco, Numero, Complemento, Bairro, Cidade, Estado, "
            + "Telefone, Celular, Tem_Convenio, Codigo_Convenio) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencher(ps, p);
            ps.executeUpdate();

            return Banco.chaveGerada(ps);
        }
    }

    public void atualizar(Paciente p) throws SQLException {

        String sql = "UPDATE pacientes SET Nome=?, Numero_CPF=?, Numero_RG=?, Orgao_Emissor=?, "
            + "Data_Nascimento=?, Endereco=?, Numero=?, Complemento=?, Bairro=?, Cidade=?, "
            + "Estado=?, Telefone=?, Celular=?, Tem_Convenio=?, Codigo_Convenio=? "
            + "WHERE Codigo_Paciente=?";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql)) {

            preencher(ps, p);
            ps.setInt(16, p.id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM pacientes WHERE Codigo_Paciente=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Preenche os 15 primeiros parâmetros (comuns ao INSERT e ao UPDATE). */
    private void preencher(PreparedStatement ps, Paciente p) throws SQLException {
        ps.setString(1, p.nome);
        ps.setString(2, Banco.nulo(p.cpf));
        ps.setString(3, Banco.nulo(p.rg));
        ps.setString(4, Banco.nulo(p.orgaoEmissor));

        if (p.nascimento == null) {
            ps.setNull(5, Types.DATE);
        } else {
            ps.setDate(5, Date.valueOf(p.nascimento));
        }

        ps.setString(6, Banco.nulo(p.endereco));
        ps.setString(7, Banco.nulo(p.numero));
        ps.setString(8, Banco.nulo(p.complemento));
        ps.setString(9, Banco.nulo(p.bairro));
        ps.setString(10, Banco.nulo(p.cidade));
        ps.setString(11, Banco.nulo(p.estado));
        ps.setString(12, Banco.nulo(p.telefone));
        ps.setString(13, Banco.nulo(p.celular));

        // 0 ("Particular") ou -1 (nada escolhido) significam sem convênio
        boolean temConvenio = p.codigoConvenio > 0;
        ps.setString(14, temConvenio ? "S" : "N");

        if (temConvenio) {
            ps.setInt(15, p.codigoConvenio);
        } else {
            ps.setNull(15, Types.INTEGER);
        }
    }
}
