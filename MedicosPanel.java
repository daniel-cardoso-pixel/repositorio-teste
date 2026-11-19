package view;

import dao.EspecialidadeDAO;
import dao.MedicoDAO;
import model.Especialidade;
import model.Medico;

import java.util.ArrayList;
import java.util.List;

public class MedicosPanel extends CadastroPanel {

    private static final int NOME = 0;
    private static final int CRM = 1;
    private static final int ESPECIALIDADE = 2;

    private final MedicoDAO dao = new MedicoDAO();
    private final EspecialidadeDAO especialidadeDAO = new EspecialidadeDAO();

    public MedicosPanel() {
        super("Cadastro de Médicos",
            Campo.texto("Nome").max(50),
            Campo.texto("CRM").max(20),
            Campo.listaBanco("Especialidade"));

        // O CRM não pode se repetir
        definirColunaUnica(CRM);
    }

    /** As especialidades vêm do banco toda vez que a tela é exibida. */
    @Override
    protected void carregarOpcoes() throws Exception {
        List<Opcao> opcoes = new ArrayList<>();

        for (Especialidade e : especialidadeDAO.listar()) {
            opcoes.add(new Opcao(e.id, e.descricao));
        }

        definirOpcoes(ESPECIALIDADE, opcoes);
    }

    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();

        for (Medico m : dao.listar()) {
            linhas.add(new Object[]{
                m.id,
                m.nome,
                m.crm,
                new Opcao(m.codigoEspecialidade, m.especialidade)
            });
        }

        return linhas;
    }

    @Override
    protected int gravarNovo() throws Exception {
        return dao.inserir(montar(0));
    }

    @Override
    protected void gravarAlteracao(int id) throws Exception {
        dao.atualizar(montar(id));
    }

    @Override
    protected void excluirRegistro(int id) throws Exception {
        dao.excluir(id);
    }

    private Medico montar(int id) {
        Medico m = new Medico();

        m.id = id;
        m.nome = texto(NOME);
        m.crm = texto(CRM);
        m.codigoEspecialidade = idOpcao(ESPECIALIDADE);

        return m;
    }
}