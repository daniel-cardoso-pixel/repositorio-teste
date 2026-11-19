package view;

import dao.ConvenioDAO;
import dao.PacienteDAO;
import model.Convenio;
import model.Paciente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class PacientesPanel extends CadastroPanel {

    private static final DateTimeFormatter DATA =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] ESTADOS = {
        "", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
        "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC",
        "SP", "SE", "TO"
    };

    private static final int NOME = 0;
    private static final int CPF = 1;
    private static final int RG = 2;
    private static final int ORGAO = 3;
    private static final int NASCIMENTO = 4;
    private static final int ENDERECO = 5;
    private static final int NUMERO = 6;
    private static final int COMPLEMENTO = 7;
    private static final int BAIRRO = 8;
    private static final int CIDADE = 9;
    private static final int ESTADO = 10;
    private static final int TELEFONE = 11;
    private static final int CELULAR = 12;
    private static final int CONVENIO = 13;

    private final PacienteDAO dao = new PacienteDAO();
    private final ConvenioDAO convenioDAO = new ConvenioDAO();

    public PacientesPanel() {
        super("Cadastro de Pacientes",
            Campo.texto("Nome completo").max(50),
            Campo.texto("CPF").naoObrigatorio().max(14),
            Campo.texto("RG").naoObrigatorio().max(12).semTabela(),
            Campo.texto("Órgão emissor").naoObrigatorio().max(6).semTabela(),
            Campo.texto("Data de nascimento (dd/mm/aaaa)").naoObrigatorio().max(10),
            Campo.texto("Endereço").naoObrigatorio().max(50).semTabela(),
            Campo.texto("Número").naoObrigatorio().max(10).semTabela(),
            Campo.texto("Complemento").naoObrigatorio().max(30).semTabela(),
            Campo.texto("Bairro").naoObrigatorio().max(40).semTabela(),
            Campo.texto("Cidade").naoObrigatorio().max(40),
            Campo.lista("Estado", ESTADOS).naoObrigatorio().semTabela(),
            Campo.texto("Telefone").naoObrigatorio().max(20),
            Campo.texto("Celular").naoObrigatorio().max(20).semTabela(),
            Campo.listaBanco("Convênio").naoObrigatorio());

        definirColunaUnica(CPF);
    }

    @Override
    protected void carregarOpcoes() throws Exception {
        List<Opcao> opcoes = new ArrayList<>();
        opcoes.add(new Opcao(0, "Particular / Nenhum"));
        for (Convenio c : convenioDAO.listar()) {
            opcoes.add(new Opcao(c.id, c.empresa));
        }
        definirOpcoes(CONVENIO, opcoes);
    }

    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();
        for (Paciente p : dao.listar()) {
            linhas.add(new Object[]{
                p.id,
                t(p.nome),
                t(p.cpf),
                t(p.rg),
                t(p.orgaoEmissor),
                p.nascimento == null ? "" : p.nascimento.format(DATA),
                t(p.endereco),
                t(p.numero),
                t(p.complemento),
                t(p.bairro),
                t(p.cidade),
                t(p.estado),
                t(p.telefone),
                t(p.celular),
                new Opcao(p.codigoConvenio, p.nomeConvenio != null ? p.nomeConvenio : "Particular")
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

    private Paciente montar(int id) {
        Paciente p = new Paciente();
        p.id = id;
        p.nome = texto(NOME);
        p.cpf = texto(CPF);
        p.rg = texto(RG);
        p.orgaoEmissor = texto(ORGAO);
        p.nascimento = lerData(texto(NASCIMENTO));
        p.endereco = texto(ENDERECO);
        p.numero = texto(NUMERO);
        p.complemento = texto(COMPLEMENTO);
        p.bairro = texto(BAIRRO);
        p.cidade = texto(CIDADE);
        p.estado = texto(ESTADO);
        p.telefone = texto(TELEFONE);
        p.celular = texto(CELULAR);
        p.codigoConvenio = idOpcao(CONVENIO);
        return p;
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