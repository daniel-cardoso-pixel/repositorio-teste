package dao;

import model.Usuario;
import util.Conexao;
import util.Seguranca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    // Nomes das colunas de permissao na tabela usuarios
    private static final String[] PERMISSOES = {
        "Cadastro_Funcionario", "Cadastro_Usuario", "Cadastro_Paciente",
        "Cadastro_Especialidade", "Cadastro_Medico", "Cadastro_Convenio",
        "Agendamento_Consulta", "Cancelamento_Consulta",
        "Modulo_Administrativo", "Modulo_Agendamento", "Modulo_Atendimento"
    };

    /** Retorna o usuario autenticado ou null se login/senha estiverem errados. */
    public Usuario autenticar(String identificacao, String senha) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE Identificacao_Usuario = ? AND Senha_Acesso = ?";
        try (Connection c = Conexao.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identificacao);
            ps.setString(2, Seguranca.sha256(senha));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Usuario u = new Usuario(rs.getInt("Registro_Usuario"),
                                        rs.getString("Identificacao_Usuario"));
                for (String p : PERMISSOES) {
                    if ("S".equals(rs.getString(p))) u.adicionarPermissao(p);
                }
                return u;
            }
        }
    }
}