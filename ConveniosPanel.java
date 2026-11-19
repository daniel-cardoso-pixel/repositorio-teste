package view;

import dao.ConvenioDAO;
import model.Convenio;

import java.util.ArrayList;
import java.util.List;

public class ConveniosPanel extends CadastroPanel {

    private static final int EMPRESA = 0;
    private static final int CNPJ = 1;
    private static final int TELEFONE = 2;

    private final ConvenioDAO dao = new ConvenioDAO();

    public ConveniosPanel() {
        super("Cadastro de Convênios",
            Campo.texto("Empresa").max(40),
            Campo.texto("CNPJ").naoObrigatorio().max(18),
            Campo.texto("Telefone").naoObrigatorio().max(20));

        // O nome da empresa não pode se repetir
        definirColunaUnica(EMPRESA);
    }

    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();

        for (Convenio c : dao.listar()) {
            linhas.add(new Object[]{
                c.id,
                c.empresa,
                c.cnpj == null ? "" : c.cnpj,
                c.telefone == null ? "" : c.telefone
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

    private Convenio montar(int id) {
        Convenio c = new Convenio();

        c.id = id;
        c.empresa = texto(EMPRESA);
        c.cnpj = texto(CNPJ);
        c.telefone = texto(TELEFONE);

        return c;
    }
}