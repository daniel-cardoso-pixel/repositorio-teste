package view;

import dao.FuncionarioDAO;
import model.Funcionario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class FuncionariosPanel extends CadastroPanel {

    private static final DateTimeFormatter DATA =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] ESTADOS = {
        "", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
        "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC",
        "SP", "SE", "TO"
    };

    private static final int NOME = 0;
    private static final int RG = 1;
    private static final int ORGAO = 2;
    private static final int CPF = 3;
    private static final int ENDERECO = 4;
    private static final int NUMERO = 5;
    private static final int COMPLEMENTO = 6;
    private static final int BAIRRO = 7;
    private static final int CIDADE = 8;
    private static final int ESTADO = 9;
    private static final int TELEFONE = 10;
    private static final int CELULAR = 11;
    private static final int CTPS = 12;
    private static final int PIS = 13;
    private static final int NASCIMENTO = 14;

    private final FuncionarioDAO dao = new FuncionarioDAO();

    public FuncionariosPanel() {
        super("Cadastro de Funcionários",
            Campo.texto("Nome completo").max(50),
            Campo.texto("RG").naoObrigatorio().max(12).semTabela(),
            Campo.texto("Órgão emissor").naoObrigatorio().max(6).semTabela(),
            Campo.texto("CPF").naoObrigatorio().max(14),
            Campo.texto("Endereço").naoObrigatorio().max(50).semTabela(),
            Campo.texto("Número").naoObrigatorio().max(10).semTabela(),
            Campo.texto("Complemento").naoObrigatorio().max(30).semTabela(),
            Campo.texto("Bairro").naoObrigatorio().max(40).semTabela(),
            Campo.texto("Cidade").naoObrigatorio().max(40),
            Campo.lista("Estado", ESTADOS).naoObrigatorio().semTabela(),
            Campo.texto("Telefone").naoObrigatorio().max(20),
            Campo.texto("Celular").naoObrigatorio().max(20).semTabela(),
            Campo.texto("CTPS").naoObrigatorio().max(20).semTabela(),
            Campo.texto("PIS").naoObrigatorio().max(20).semTabela(),
            Campo.texto("Data de nascimento (dd/mm/aaaa)").naoObrigatorio().max(10));

        definirColunaUnica(CPF);
    }

    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();
        for (Funcionario f : dao.listar()) {
            linhas.add(new Object[]{
                f.id,
                t(f.nome),
                t(f.rg),
                t(f.orgaoEmissor),
                t(f.cpf),
                t(f.endereco),
                t(f.numero),
                t(f.complemento),
                t(f.bairro),
                t(f.cidade),
                t(f.estado),
                t(f.telefone),
                t(f.celular),
                t(f.ctps),
                t(f.pis),
                f.nascimento == null ? "" : f.nascimento.format(DATA)
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

    private Funcionario montar(int id) {
        Funcionario f = new Funcionario();
        f.id = id;
        f.nome = texto(NOME);
        f.rg = texto(RG);
        f.orgaoEmissor = texto(ORGAO);
        f.cpf = texto(CPF);
        f.endereco = texto(ENDERECO);
        f.numero = texto(NUMERO);
        f.complemento = texto(COMPLEMENTO);
        f.bairro = texto(BAIRRO);
        f.cidade = texto(CIDADE);
        f.estado = texto(ESTADO);
        f.telefone = texto(TELEFONE);
        f.celular = texto(CELULAR);
        f.ctps = texto(CTPS);
        f.pis = texto(PIS);
        f.nascimento = lerData(texto(NASCIMENTO));
        return f;
    }

    private LocalDate lerData(String texto) {
        if (texto.isEmpty()) return null;
        try {
            return LocalDate.parse(texto, DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de nascimento inválida. Use o formato dd/mm/aaaa.");
        }
    }

    private static String t(String s) {
        return s == null ? "" : s;
    }
}