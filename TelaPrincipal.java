package view;

import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class TelaPrincipal extends JFrame {

    // Senha exigida para abrir qualquer tela do menu Administrativo
    private static final String SENHA_ADMIN = "123123";

    private final Usuario usuario;
    private final CardLayout cardLayout;
    private final JPanel painelConteudo;
    private final Set<String> telasRegistradas = new HashSet<>();

    public TelaPrincipal(Usuario usuario) {
        super("Clínica Médica");

        this.usuario = usuario;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Área onde as telas serão exibidas
        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);

        // Tela inicial
        JPanel inicio = new JPanel(new BorderLayout());
        JLabel boasVindas = new JLabel(
            "Bem-vindo, " + usuario.getIdentificacao(),
            SwingConstants.CENTER
        );

        boasVindas.setFont(new Font("Arial", Font.BOLD, 22));
        inicio.add(boasVindas, BorderLayout.CENTER);

        registrarTela("inicio", inicio);

        // Telas do menu Administrativo
        registrarTela("funcionarios", new FuncionariosPanel());
        registrarTela("usuarios", new UsuariosPanel());
        registrarTela("especialidades", new EspecialidadesPanel());
        registrarTela("medicos", new MedicosPanel());
        registrarTela("convenios", new ConveniosPanel());

        // Telas do menu Agendamento e Atendimento
        registrarTela("pacientes", new PacientesPanel());
        registrarTela("agendamento", new AgendamentosPanel(usuario.getRegistro()));
        registrarTela("cancelamento", new CancelamentosPanel());
        registrarTela("atendimento", new AtendimentoPanel());

        // MENU ADMINISTRATIVO (todos os itens pedem senha)
        JMenuBar barra = new JMenuBar();

        JMenu mAdm = new JMenu("Administrativo");
        mAdm.setEnabled(usuario.temPermissao("Modulo_Administrativo"));

        mAdm.add(item("Funcionários", "Cadastro_Funcionario", "funcionarios", true));
        mAdm.add(item("Usuários", "Cadastro_Usuario", "usuarios", true));
        mAdm.add(item("Especialidades", "Cadastro_Especialidade", "especialidades", true));
        mAdm.add(item("Médicos", "Cadastro_Medico", "medicos", true));
        mAdm.add(item("Convênios", "Cadastro_Convenio", "convenios", true));

        // MENU AGENDAMENTO
        JMenu mAgd = new JMenu("Agendamento");
        mAgd.setEnabled(usuario.temPermissao("Modulo_Agendamento"));

        mAgd.add(item("Pacientes", "Cadastro_Paciente", "pacientes", false));
        mAgd.add(item("Agendar consulta / retorno",
            "Agendamento_Consulta", "agendamento", false));
        mAgd.add(item("Cancelar consulta",
            "Cancelamento_Consulta", "cancelamento", false));

        // MENU ATENDIMENTO
        JMenu mAtd = new JMenu("Atendimento");
        mAtd.setEnabled(usuario.temPermissao("Modulo_Atendimento"));

        mAtd.add(item("Prontuário / atendimento",
            "Modulo_Atendimento", "atendimento", false));

        // MENU SISTEMA
        JMenu mSistema = new JMenu("Sistema");

        JMenuItem sair = new JMenuItem("Sair");

        sair.addActionListener(e -> {
            new TelaLogin().setVisible(true);
            dispose();
        });

        mSistema.add(sair);

        // Adicionando menus à barra
        barra.add(mAdm);
        barra.add(mAgd);
        barra.add(mAtd);
        barra.add(mSistema);

        setJMenuBar(barra);

        // Adicionando área central
        add(painelConteudo, BorderLayout.CENTER);

        cardLayout.show(painelConteudo, "inicio");
    }

    // Registra uma tela no CardLayout e guarda o nome para consulta
    private void registrarTela(String nome, JPanel painel) {
        painelConteudo.add(painel, nome);
        telasRegistradas.add(nome);
    }

    // Criar itens do menu
    private JMenuItem item(String titulo, String permissao, String tela,
                           boolean exigeSenha) {

        JMenuItem it = new JMenuItem(titulo);

        it.setEnabled(usuario.temPermissao(permissao));

        it.addActionListener(e -> {
            // Pede a senha antes de qualquer acesso ao menu administrativo
            if (exigeSenha && !pedirSenha()) {
                return;
            }

            if (telaExiste(tela)) {
                cardLayout.show(painelConteudo, tela);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Esta tela ainda será desenvolvida.");
            }
        });

        return it;
    }

    // Abre a janela de senha e devolve true se a senha estiver correta
    private boolean pedirSenha() {

        JPasswordField campo = new JPasswordField(15);

        int opcao = JOptionPane.showConfirmDialog(
            this,
            campo,
            "Digite a senha administrativa",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (opcao != JOptionPane.OK_OPTION) {
            return false;
        }

        String digitada = new String(campo.getPassword());

        if (digitada.equals(SENHA_ADMIN)) {
            return true;
        }

        JOptionPane.showMessageDialog(this,
            "Senha incorreta.",
            "Acesso negado",
            JOptionPane.ERROR_MESSAGE);

        return false;
    }

    // Verificar se a tela já foi criada
    private boolean telaExiste(String nome) {
        return telasRegistradas.contains(nome);
    }
}