package view;

import dao.FuncionarioDAO;
import dao.UsuarioCadastroDAO;
import model.Funcionario;
import model.UsuarioCadastro;

import java.util.ArrayList;
import java.util.List;

public class UsuariosPanel extends CadastroPanel {

    // Posição dos campos no formulário
    private static final int FUNCIONARIO = 0;
    private static final int IDENTIFICACAO = 1;
    private static final int SENHA = 2;
    private static final int PRIMEIRA_PERMISSAO = 3;

    private final UsuarioCadastroDAO dao = new UsuarioCadastroDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    public UsuariosPanel() {
        super("Cadastro de Usuários",
            Campo.listaBanco("Funcionário"),
            Campo.texto("Identificação").max(20),
            Campo.senha("Senha").naoObrigatorio().dica(
                "Ao editar um usuário, deixe em branco para manter a senha atual."),
            // A ordem das permissões segue UsuarioCadastro.COLUNAS_PERMISSAO
            Campo.marcar("Cadastro de funcionários"),
            Campo.marcar("Cadastro de usuários"),
            Campo.marcar("Cadastro de pacientes"),
            Campo.marcar("Cadastro de especialidades"),
            Campo.marcar("Cadastro de médicos"),
            Campo.marcar("Cadastro de convênios"),
            Campo.marcar("Agendamento de consulta"),
            Campo.marcar("Cancelamento de consulta"),
            Campo.marcar("Módulo administrativo"),
            Campo.marcar("Módulo agendamento"),
            Campo.marcar("Módulo atendimento"));

        // A identificação (login) não pode se repetir
        definirColunaUnica(IDENTIFICACAO);
    }

    @Override
    protected void carregarOpcoes() throws Exception {
        List<Opcao> opcoes = new ArrayList<>();

        for (Funcionario f : funcionarioDAO.listar()) {
            opcoes.add(new Opcao(f.id, f.nome));
        }

        definirOpcoes(FUNCIONARIO, opcoes);
    }

    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();

        for (UsuarioCadastro u : dao.listar()) {
            Object[] linha = new Object[PRIMEIRA_PERMISSAO + u.permissoes.length + 1];

            linha[0] = u.id;
            linha[FUNCIONARIO + 1] = new Opcao(u.codigoFuncionario, u.nomeFuncionario);
            linha[IDENTIFICACAO + 1] = u.identificacao;
            linha[SENHA + 1] = "";

            for (int i = 0; i < u.permissoes.length; i++) {
                linha[PRIMEIRA_PERMISSAO + i + 1] = u.permissoes[i] ? "S" : "N";
            }

            linhas.add(linha);
        }

        return linhas;
    }

    @Override
    protected int gravarNovo() throws Exception {
        UsuarioCadastro u = montar(0);

        if (u.senha.isEmpty()) {
            throw new IllegalArgumentException("Informe a senha do novo usuário.");
        }

        return dao.inserir(u);
    }

    @Override
    protected void gravarAlteracao(int id) throws Exception {
        dao.atualizar(montar(id));
    }

    @Override
    protected void excluirRegistro(int id) throws Exception {
        dao.excluir(id);
    }

    private UsuarioCadastro montar(int id) {
        UsuarioCadastro u = new UsuarioCadastro();

        u.id = id;
        u.codigoFuncionario = idOpcao(FUNCIONARIO);
        u.identificacao = texto(IDENTIFICACAO);
        u.senha = texto(SENHA);

        for (int i = 0; i < u.permissoes.length; i++) {
            u.permissoes[i] = marcado(PRIMEIRA_PERMISSAO + i);
        }

        return u;
    }
}