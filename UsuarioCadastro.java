package model;

/**
 * Usuário como aparece na tela de cadastro (não é o usuário logado).
 * O usuário logado continua sendo a classe model.Usuario.
 */
public class UsuarioCadastro {

    /** Nomes das colunas de permissão da tabela usuarios, na ordem da tela. */
    public static final String[] COLUNAS_PERMISSAO = {
        "Cadastro_Funcionario",
        "Cadastro_Usuario",
        "Cadastro_Paciente",
        "Cadastro_Especialidade",
        "Cadastro_Medico",
        "Cadastro_Convenio",
        "Agendamento_Consulta",
        "Cancelamento_Consulta",
        "Modulo_Administrativo",
        "Modulo_Agendamento",
        "Modulo_Atendimento"
    };

    public int id;
    public int codigoFuncionario;
    public String nomeFuncionario;
    public String identificacao;

    /** Senha em texto puro, só usada ao gravar (o DAO converte para hash). */
    public String senha;

    public boolean[] permissoes = new boolean[COLUNAS_PERMISSAO.length];
}
