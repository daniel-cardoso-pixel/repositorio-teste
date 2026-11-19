package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    // allowPublicKeyRetrieval evita o erro "Public Key Retrieval is not allowed"
    // no MySQL 8 quando o SSL está desligado.
    private static final String URL =
        "jdbc:mysql://localhost:3306/clinica"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String SENHA = "senha";   // Ajuste para a sua senha do MySQL

    public static Connection getConexao() throws SQLException {
        try {
            // Força o Java a carregar o driver moderno do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("O driver do MySQL não foi encontrado nas bibliotecas do projeto "
                + "(coloque o .jar do MySQL Connector/J na pasta lib).", e);
        }

        // Se existir a variável de ambiente CLINICA_DB_SENHA, ela tem prioridade
        // (assim a senha não precisa ficar escrita no código).
        String senha = System.getenv("CLINICA_DB_SENHA");

        return DriverManager.getConnection(URL, USUARIO, senha != null ? senha : SENHA);
    }
}
