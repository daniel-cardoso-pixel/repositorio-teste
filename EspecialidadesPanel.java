package view;

import dao.EspecialidadeDAO;
import model.Especialidade;

import java.util.ArrayList;
import java.util.List;

public class EspecialidadesPanel extends CadastroPanel {

    private static final int DESCRICAO = 0;

    private final EspecialidadeDAO dao = new EspecialidadeDAO();

    public EspecialidadesPanel() {
        super("Cadastro de Especialidades",
            Campo.texto("Descrição").max(40));

        // A descrição não pode se repetir
        definirColunaUnica(DESCRICAO);
    }

    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();

        for (Especialidade e : dao.listar()) {
            linhas.add(new Object[]{e.id, e.descricao});
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

    private Especialidade montar(int id) {
        Especialidade e = new Especialidade();

        e.id = id;
        e.descricao = texto(DESCRICAO);

        return e;
    }
}