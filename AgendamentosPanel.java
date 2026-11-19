package view;

import dao.AgendamentoDAO;
import dao.MedicoDAO;
import dao.PacienteDAO;
import model.Agendamento;
import model.Medico;
import model.Paciente;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class AgendamentosPanel extends CadastroPanel {

    // STRICT: recusa datas que não existem (ex.: 31/02/2026), em vez de "corrigi-las"
    private static final DateTimeFormatter DATA_HORA =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private static final int PACIENTE = 0;
    private static final int MEDICO = 1;
    private static final int DATA_HORA_CAMPO = 2;
    private static final int TIPO = 3;
    private static final int OBSERVACAO = 4;

    private final AgendamentoDAO dao = new AgendamentoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();

    /** Registro do usuário logado: o banco exige quem fez o agendamento. */
    private final int codigoUsuario;

    public AgendamentosPanel(int codigoUsuario) {
        super("Agendamento de Consultas / Retornos",
            Campo.listaBanco("Paciente"),
            Campo.listaBanco("Médico"),
            Campo.texto("Data e Hora (dd/mm/aaaa hh:mm)").dica("Ex: 15/11/2026 14:30"),
            Campo.lista("Tipo", "Consulta", "Retorno"),
            Campo.texto("Observação").naoObrigatorio().max(100));

        this.codigoUsuario = codigoUsuario;
    }

    @Override
    protected void carregarOpcoes() throws Exception {
        List<Opcao> opPacientes = new ArrayList<>();
        for (Paciente p : pacienteDAO.listar()) {
            opPacientes.add(new Opcao(p.id, p.nome));
        }
        definirOpcoes(PACIENTE, opPacientes);

        List<Opcao> opMedicos = new ArrayList<>();
        for (Medico m : medicoDAO.listar()) {
            opMedicos.add(new Opcao(m.id, m.nome + " (" + m.especialidade + ")"));
        }
        definirOpcoes(MEDICO, opMedicos);
    }

    /** Mostra só as consultas ainda agendadas; canceladas e atendidas ficam como histórico. */
    @Override
    protected List<Object[]> carregar() throws Exception {
        List<Object[]> linhas = new ArrayList<>();
        for (Agendamento a : dao.listarAtivos()) {
            linhas.add(new Object[]{
                a.id,
                new Opcao(a.codigoPaciente, a.nomePaciente),
                new Opcao(a.codigoMedico, a.nomeMedico),
                a.dataHora.format(DATA_HORA),
                a.tipo,
                a.observacao == null ? "" : a.observacao
            });
        }
        return linhas;
    }

    @Override
    protected int gravarNovo() throws Exception {
        Agendamento a = montar(0);

        if (a.dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data e hora informadas já passaram.");
        }

        return dao.inserir(a);
    }

    @Override
    protected void gravarAlteracao(int id) throws Exception {
        dao.atualizar(montar(id));
    }

    @Override
    protected void excluirRegistro(int id) throws Exception {
        dao.excluir(id);
    }

    private Agendamento montar(int id) {
        Agendamento a = new Agendamento();
        a.id = id;
        a.codigoUsuario = codigoUsuario;
        a.codigoPaciente = idOpcao(PACIENTE);
        a.codigoMedico = idOpcao(MEDICO);
        try {
            a.dataHora = LocalDateTime.parse(texto(DATA_HORA_CAMPO), DATA_HORA);
        } catch (Exception e) {
            throw new IllegalArgumentException("Data/Hora inválida. Utilize o formato dd/mm/aaaa hh:mm.");
        }
        a.tipo = texto(TIPO);
        a.observacao = texto(OBSERVACAO);
        a.status = "Agendado";
        return a;
    }
}