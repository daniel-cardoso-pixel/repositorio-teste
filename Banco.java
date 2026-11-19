package dao;

import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ponto único de acesso ao banco para os DAOs de cadastro.
 */
public final class Banco {

    private Banco() {
    }

    /**
     * Abre uma conexão com o banco.
     * Se o método da sua classe util.Conexao tiver outro nome,
     * troque SOMENTE esta linha.
     */
    public static Connection abrir() throws SQLException {
        return Conexao.getConexao();
    }

    /** Texto vazio vira null (para gravar NULL no banco). */
    public static String nulo(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return s.trim();
    }

    /** Código gerado pelo AUTO_INCREMENT no último INSERT. */
    public static int chaveGerada(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        throw new SQLException("Não foi possível obter o código gerado.");
    }
}
