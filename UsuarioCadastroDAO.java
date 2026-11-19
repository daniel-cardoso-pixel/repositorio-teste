package dao;

import model.UsuarioCadastro;
import util.Seguranca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Cadastro de usuarios do sistema (tabela usuarios).
 * A senha nunca e gravada pura: o DAO converte para SHA-256.
 */
public class UsuarioCadastroDAO {

    private static final String MSG_ULTIMO_ADMIN =
        "Operacao recusada: o sistema precisa manter pelo menos um usuario com "
        + "\"Modulo administrativo\" e \"Cadastro de usuarios\".";

    private static final int POS_ADMIN = indice("Modulo_Administrativo");
    private static final int POS_CAD_USUARIO = indice("Cadastro_Usuario");

    public List<UsuarioCadastro> listar() throws SQLException {

        String sql = "SELECT u.Registro_Usuario, u.Codigo_Funcionario, f.Nome_Completo, "
            + "u.Identificacao_Usuario, " + colunasPermissao("u") + " "
            + "FROM usuarios u "
            + "JOIN funcionarios f ON f.Codigo_Funcionario = u.Codigo_Funcionario "
            + "ORDER BY u.Identificacao_Usuario";

        List<UsuarioCadastro> lista = new ArrayList<>();

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioCadastro u = new UsuarioCadastro();
                u.id = rs.getInt(1);
                u.codigoFuncionario = rs.getInt(2);
                u.nomeFuncionario = rs.getString(3);
                u.identificacao = rs.getString(4);

                for (int i = 0; i < u.permissoes.length; i++) {
                    u.permissoes[i] = "S".equals(rs.getString(5 + i));
                }

                lista.add(u);
            }
        }

        return lista;
    }

    public int inserir(UsuarioCadastro u) throws SQLException {

        if (u.senha == null || u.senha.isEmpty()) {
            throw new IllegalArgumentException("Informe a senha do novo usuário.");
        }

        String sql = "INSERT INTO usuarios (Codigo_Funcionario, Identificacao_Usuario, "
            + "Senha_Acesso, " + colunasPermissao(null) + ") VALUES (?, ?, ?, "
            + interrogacoes(UsuarioCadastro.COLUNAS_PERMISSAO.length) + ")";

        try (Connection c = Banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, u.codigoFuncionario);
            ps.setString(2, u.identificacao);
            ps.setString(3, Seguranca.sha256(u.senha));

            for (int i = 0; i < u.permissoes.length; i++) {
                ps.setString(4 + i, u.permissoes[i] ? "S" : "N");
            }

            ps.executeUpdate();

            return Banco.chaveGerada(ps);
        }
    }

    /** Senha em branco = mantém a senha atual. */
    public void atualizar(UsuarioCadastro u) throws SQLException {

        boolean trocaSenha = u.senha != null && !u.senha.isEmpty();

        StringBuilder sql = new StringBuilder(
            "UPDATE usuarios SET Codigo_Funcionario=?, Identificacao_Usuario=?");

        for (String coluna : UsuarioCadastro.COLUNAS_PERMISSAO) {
            sql.append(", ").append(coluna).append("=?");
        }

        if (trocaSenha) {
            sql.append(", Senha_Acesso=?");
        }

        sql.append(" WHERE Registro_Usuario=?");

        try (Connection c = Banco.abrir()) {

            // Não deixa o sistema sem nenhum administrador
            if (!(u.permissoes[POS_ADMIN] && u.permissoes[POS_CAD_USUARIO])) {
                exigirOutroAdministrador(c, u.id);
            }

            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                int n = 1;

                ps.setInt(n++, u.codigoFuncionario);
                ps.setString(n++, u.identificacao);

                for (boolean permitido : u.permissoes) {
                    ps.setString(n++, permitido ? "S" : "N");
                }

                if (trocaSenha) {
                    ps.setString(n++, Seguranca.sha256(u.senha));
                }

                ps.setInt(n, u.id);
                ps.executeUpdate();
            }
        }
    }

    public void excluir(int id) throws SQLException {

        try (Connection c = Banco.abrir()) {

            exigirOutroAdministrador(c, id);

            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM usuarios WHERE Registro_Usuario=?")) {

                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    /** Garante que existe outro usuário (diferente de "id") que é administrador. */
    private void exigirOutroAdministrador(Connection c, int id) throws SQLException {

        String sql = "SELECT COUNT(*) FROM usuarios "
            + "WHERE Modulo_Administrativo='S' AND Cadastro_Usuario='S' "
            + "AND Registro_Usuario<>?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new IllegalArgumentException(MSG_ULTIMO_ADMIN);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares (os nomes de coluna vêm de uma constante, nunca do usuário)
    // ------------------------------------------------------------------

    private static String colunasPermissao(String alias) {
        StringBuilder sb = new StringBuilder();

        for (String coluna : UsuarioCadastro.COLUNAS_PERMISSAO) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            if (alias != null) {
                sb.append(alias).append('.');
            }
            sb.append(coluna);
        }

        return sb.toString();
    }

    private static String interrogacoes(int quantidade) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < quantidade; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('?');
        }

        return sb.toString();
    }

    private static int indice(String coluna) {
        for (int i = 0; i < UsuarioCadastro.COLUNAS_PERMISSAO.length; i++) {
            if (UsuarioCadastro.COLUNAS_PERMISSAO[i].equals(coluna)) {
                return i;
            }
        }
        throw new IllegalStateException("Coluna de permissão desconhecida: " + coluna);
    }
}
