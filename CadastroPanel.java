package view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tela base de cadastro: formulário à esquerda, tabela com busca à direita
 * e botões Adicionar / Atualizar / Remover / Limpar.
 *
 * Cada tela informa seus campos e implementa os métodos que falam com o
 * banco (carregar, gravarNovo, gravarAlteracao, excluirRegistro).
 * Os dados são carregados do banco sempre que a tela é exibida.
 */
public abstract class CadastroPanel extends JPanel {

    /** Item de uma lista que guarda o código do banco. */
    public static class Opcao {
        public final int id;
        public final String texto;

        public Opcao(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto == null ? "" : texto;
        }
    }

    /** Descrição de um campo do formulário. */
    public static class Campo {
        private static final int TEXTO = 0;
        private static final int SENHA = 1;
        private static final int LISTA = 2;
        private static final int LISTA_BANCO = 3;
        private static final int MARCAR = 4;

        private final String nome;
        private final int tipo;
        private final String[] opcoes;
        private boolean obrigatorio = true;
        private boolean naTabela = true;
        private int max = 0;
        private String dica = null;

        private Campo(String nome, int tipo, String[] opcoes) {
            this.nome = nome;
            this.tipo = tipo;
            this.opcoes = opcoes;

            if (tipo == SENHA || tipo == MARCAR) {
                naTabela = false;
            }

            if (tipo == MARCAR) {
                obrigatorio = false;
            }
        }

        /** Campo de texto (obrigatório por padrão). */
        public static Campo texto(String nome) {
            return new Campo(nome, TEXTO, null);
        }

        /** Campo de senha (nunca aparece na tabela). */
        public static Campo senha(String nome) {
            return new Campo(nome, SENHA, null);
        }

        /** Lista com opções fixas. */
        public static Campo lista(String nome, String... opcoes) {
            return new Campo(nome, LISTA, opcoes);
        }

        /** Lista preenchida com dados do banco (veja definirOpcoes). */
        public static Campo listaBanco(String nome) {
            return new Campo(nome, LISTA_BANCO, null);
        }

        /** Caixa de marcar: vale "S" quando marcada e "N" quando não. */
        public static Campo marcar(String nome) {
            return new Campo(nome, MARCAR, null);
        }

        public Campo naoObrigatorio() {
            this.obrigatorio = false;
            return this;
        }

        public Campo semTabela() {
            this.naTabela = false;
            return this;
        }

        public Campo max(int tamanho) {
            this.max = tamanho;
            return this;
        }

        public Campo dica(String texto) {
            this.dica = texto;
            return this;
        }
    }

    private final List<Campo> campos = new ArrayList<>();
    private final List<JComponent> componentes = new ArrayList<>();
    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtBusca = new JTextField(18);

    private int colunaUnica = -1;
    private boolean carregando = false;

    protected CadastroPanel(String titulo, Campo... camposDoFormulario) {

        // A coluna 0 do modelo guarda o código do banco (fica escondida)
        String[] nomes = new String[camposDoFormulario.length + 1];
        nomes[0] = "Código";

        for (int i = 0; i < camposDoFormulario.length; i++) {
            campos.add(camposDoFormulario[i]);
            nomes[i + 1] = camposDoFormulario[i].nome;
        }

        modelo = new DefaultTableModel(nomes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(modelo);
        tabela.setRowSorter(sorter);

        // Esconde o código e as colunas marcadas com semTabela()
        // (de trás para frente, para os índices não mudarem)
        for (int i = campos.size(); i >= 0; i--) {
            boolean esconder = (i == 0) || !campos.get(i - 1).naTabela;

            if (esconder) {
                tabela.removeColumn(tabela.getColumnModel().getColumn(i));
            }
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(criarTopo(titulo), BorderLayout.NORTH);
        add(criarLadoEsquerdo(), BorderLayout.WEST);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Ao clicar numa linha, os dados vão para o formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !carregando) {
                carregarSelecionado();
            }
        });

        // Busca em tempo real
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltro();
            }
        });

        // Sempre que a tela for exibida, busca os dados atuais no banco
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recarregarTudo();
            }
        });
    }

    // ------------------------------------------------------------------
    // O que cada tela precisa implementar
    // ------------------------------------------------------------------

    /**
     * Lê os registros do banco. Cada linha é: código, depois um valor por
     * campo, na ordem dos campos. Campos listaBanco devem vir como Opcao;
     * caixas de marcar como "S" ou "N"; senha como "".
     */
    protected abstract List<Object[]> carregar() throws Exception;

    /** Grava um registro novo com os valores do formulário e devolve o código. */
    protected abstract int gravarNovo() throws Exception;

    /** Grava as alterações do registro com este código. */
    protected abstract void gravarAlteracao(int id) throws Exception;

    /** Remove o registro com este código. */
    protected abstract void excluirRegistro(int id) throws Exception;

    /** Preenche as listas que vêm do banco (use definirOpcoes). */
    protected void carregarOpcoes() throws Exception {
    }

    // ------------------------------------------------------------------
    // Utilitários para as telas filhas
    // ------------------------------------------------------------------

    /** Valor de um campo do formulário, como texto. */
    protected String texto(int campo) {
        return lerValor(campo);
    }

    /** Se a caixa de marcar do campo está marcada. */
    protected boolean marcado(int campo) {
        return ((JCheckBox) componentes.get(campo)).isSelected();
    }

    /** Código da opção escolhida numa lista do banco (-1 se nada escolhido). */
    protected int idOpcao(int campo) {
        Object o = ((JComboBox<?>) componentes.get(campo)).getSelectedItem();

        if (o instanceof Opcao) {
            return ((Opcao) o).id;
        }

        return -1;
    }

    /** Define qual campo não pode se repetir (-1 = nenhum). */
    protected void definirColunaUnica(int campo) {
        this.colunaUnica = campo;
    }

    /** Troca as opções de uma lista do banco, mantendo a escolha atual. */
    @SuppressWarnings("unchecked")
    protected void definirOpcoes(int campo, List<Opcao> opcoes) {
        JComboBox<Object> combo = (JComboBox<Object>) componentes.get(campo);
        int idAnterior = idOpcao(campo);
        Opcao manter = null;

        combo.removeAllItems();

        for (Opcao o : opcoes) {
            combo.addItem(o);

            if (o.id == idAnterior) {
                manter = o;
            }
        }

        if (manter != null) {
            combo.setSelectedItem(manter);
        } else {
            combo.setSelectedIndex(-1);
        }
    }

    /** Recarrega as listas e a tabela a partir do banco. */
    public void recarregarTudo() {
        try {
            carregarOpcoes();
            List<Object[]> dados = carregar();

            carregando = true;
            modelo.setRowCount(0);

            for (Object[] linha : dados) {
                modelo.addRow(linha);
            }

            carregando = false;
            limparFormulario();

        } catch (Exception e) {
            carregando = false;
            mostrarErro("Não foi possível carregar os dados", e);
        }
    }

    // ------------------------------------------------------------------
    // Montagem da tela
    // ------------------------------------------------------------------

    private JPanel criarTopo(String titulo) {
        JPanel topo = new JPanel(new BorderLayout());

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        topo.add(lblTitulo, BorderLayout.WEST);

        JPanel busca = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        busca.add(new JLabel("Buscar:"));
        busca.add(txtBusca);
        topo.add(busca, BorderLayout.EAST);

        return topo;
    }

    private JPanel criarLadoEsquerdo() {

        JPanel form = criarFormulario();

        JScrollPane rolagem = new JScrollPane(form);
        rolagem.setBorder(null);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        rolagem.setPreferredSize(new Dimension(form.getPreferredSize().width + 25, 100));

        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnRemover = new JButton("Remover");
        JButton btnLimpar = new JButton("Limpar");

        btnAdicionar.addActionListener(e -> adicionar());
        btnAtualizar.addActionListener(e -> atualizar());
        btnRemover.addActionListener(e -> remover());
        btnLimpar.addActionListener(e -> limparFormulario());

        JPanel botoes = new JPanel(new GridLayout(2, 2, 5, 5));
        botoes.add(btnAdicionar);
        botoes.add(btnAtualizar);
        botoes.add(btnRemover);
        botoes.add(btnLimpar);

        JPanel esquerda = new JPanel(new BorderLayout(0, 8));
        esquerda.add(rolagem, BorderLayout.CENTER);
        esquerda.add(botoes, BorderLayout.SOUTH);

        return esquerda;
    }

    private JPanel criarFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;

        for (Campo campo : campos) {
            JComponent componente = criarComponente(campo);
            componentes.add(componente);

            if (campo.dica != null) {
                componente.setToolTipText(campo.dica);
            }

            gbc.gridy = linha;

            if (campo.tipo == Campo.MARCAR) {
                gbc.gridx = 0;
                gbc.gridwidth = 2;
                gbc.weightx = 1;
                form.add(componente, gbc);
            } else {
                gbc.gridx = 0;
                gbc.gridwidth = 1;
                gbc.weightx = 0;
                form.add(new JLabel(campo.nome + ":"), gbc);

                gbc.gridx = 1;
                gbc.weightx = 1;
                form.add(componente, gbc);
            }

            linha++;
        }

        // Empurra o formulário para o topo
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        form.add(Box.createVerticalGlue(), gbc);

        return form;
    }

    private JComponent criarComponente(Campo campo) {

        switch (campo.tipo) {
            case Campo.SENHA:
                return new JPasswordField(18);

            case Campo.LISTA:
                return new JComboBox<Object>(campo.opcoes);

            case Campo.LISTA_BANCO:
                JComboBox<Object> combo = new JComboBox<>();
                combo.setSelectedIndex(-1);
                return combo;

            case Campo.MARCAR:
                return new JCheckBox(campo.nome);

            default:
                return new JTextField(18);
        }
    }

    // ------------------------------------------------------------------
    // Ações dos botões
    // ------------------------------------------------------------------

    protected void adicionar() {
        if (!validar(-1)) {
            return;
        }

        try {
            int id = gravarNovo();

            modelo.addRow(linhaDoFormulario(id));
            limparFormulario();

        } catch (Exception e) {
            mostrarErro("Não foi possível gravar", e);
        }
    }

    protected void atualizar() {
        int linha = linhaSelecionadaNoModelo();

        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um registro na tabela para atualizar.");
            return;
        }

        if (!validar(linha)) {
            return;
        }

        try {
            int id = (Integer) modelo.getValueAt(linha, 0);

            gravarAlteracao(id);

            Object[] novos = linhaDoFormulario(id);

            for (int i = 0; i < novos.length; i++) {
                modelo.setValueAt(novos[i], linha, i);
            }

            limparFormulario();

        } catch (Exception e) {
            mostrarErro("Não foi possível atualizar", e);
        }
    }

    protected void remover() {
        int linha = linhaSelecionadaNoModelo();

        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um registro na tabela para remover.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(this,
            "Deseja realmente remover este registro?",
            "Confirmar remoção",
            JOptionPane.YES_NO_OPTION);

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = (Integer) modelo.getValueAt(linha, 0);

            excluirRegistro(id);

            modelo.removeRow(linha);
            limparFormulario();

        } catch (Exception e) {
            mostrarErro("Não foi possível remover", e);
        }
    }

    protected void limparFormulario() {
        carregando = true;
        tabela.clearSelection();

        for (int i = 0; i < componentes.size(); i++) {
            definirValor(i, null);
        }

        carregando = false;

        if (!componentes.isEmpty()) {
            componentes.get(0).requestFocus();
        }
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private void mostrarErro(String titulo, Exception e) {
        String mensagem;

        if (e instanceof IllegalArgumentException) {
            mensagem = e.getMessage();
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            mensagem = "Operação não permitida: o registro está em uso em outro "
                + "cadastro ou já existe um igual.";
        } else {
            e.printStackTrace();
            mensagem = titulo + ":\n" + e.getMessage();
        }

        JOptionPane.showMessageDialog(this, mensagem,
            "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void aplicarFiltro() {
        String texto = txtBusca.getText().trim();

        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        // Pesquisa só nas colunas que aparecem na tabela
        List<Integer> colunas = new ArrayList<>();

        for (int i = 0; i < campos.size(); i++) {
            if (campos.get(i).naTabela) {
                colunas.add(i + 1);
            }
        }

        int[] indices = new int[colunas.size()];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = colunas.get(i);
        }

        sorter.setRowFilter(RowFilter.regexFilter(
            "(?i)" + Pattern.quote(texto), indices));
    }

    private int linhaSelecionadaNoModelo() {
        int linha = tabela.getSelectedRow();

        if (linha < 0) {
            return -1;
        }

        return tabela.convertRowIndexToModel(linha);
    }

    private void carregarSelecionado() {
        int linha = linhaSelecionadaNoModelo();

        if (linha < 0) {
            return;
        }

        for (int i = 0; i < componentes.size(); i++) {
            definirValor(i, modelo.getValueAt(linha, i + 1));
        }
    }

    private boolean validar(int linhaIgnorada) {

        for (int i = 0; i < campos.size(); i++) {
            Campo campo = campos.get(i);
            String valor = lerValor(i);

            if (campo.obrigatorio && valor.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Preencha o campo \"" + campo.nome + "\".");
                componentes.get(i).requestFocus();
                return false;
            }

            if (campo.max > 0 && valor.length() > campo.max) {
                JOptionPane.showMessageDialog(this,
                    "O campo \"" + campo.nome + "\" aceita no máximo "
                    + campo.max + " caracteres.");
                componentes.get(i).requestFocus();
                return false;
            }
        }

        if (colunaUnica >= 0) {
            String novo = lerValor(colunaUnica);

            if (!novo.isEmpty()) {
                for (int r = 0; r < modelo.getRowCount(); r++) {
                    if (r == linhaIgnorada) {
                        continue;
                    }

                    String existente = String.valueOf(
                        modelo.getValueAt(r, colunaUnica + 1));

                    if (existente.equalsIgnoreCase(novo)) {
                        JOptionPane.showMessageDialog(this,
                            "Já existe um registro com este valor em \""
                            + campos.get(colunaUnica).nome + "\".");
                        componentes.get(colunaUnica).requestFocus();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /** Linha da tabela com os valores atuais do formulário. */
    private Object[] linhaDoFormulario(int id) {
        Object[] linha = new Object[campos.size() + 1];
        linha[0] = id;

        for (int i = 0; i < campos.size(); i++) {
            Campo campo = campos.get(i);

            if (campo.tipo == Campo.SENHA) {
                linha[i + 1] = "";
            } else if (campo.tipo == Campo.LISTA_BANCO) {
                linha[i + 1] = ((JComboBox<?>) componentes.get(i)).getSelectedItem();
            } else {
                linha[i + 1] = lerValor(i);
            }
        }

        return linha;
    }

    private String lerValor(int i) {
        JComponent c = componentes.get(i);

        if (c instanceof JPasswordField) {
            return new String(((JPasswordField) c).getPassword());
        }

        if (c instanceof JTextField) {
            return ((JTextField) c).getText().trim();
        }

        if (c instanceof JCheckBox) {
            return ((JCheckBox) c).isSelected() ? "S" : "N";
        }

        if (c instanceof JComboBox) {
            Object o = ((JComboBox<?>) c).getSelectedItem();
            return o == null ? "" : o.toString();
        }

        return "";
    }

    private void definirValor(int i, Object valor) {
        JComponent c = componentes.get(i);
        String texto = valor == null ? "" : valor.toString();

        if (c instanceof JCheckBox) {
            ((JCheckBox) c).setSelected("S".equals(texto));
            return;
        }

        if (c instanceof JTextField) {
            ((JTextField) c).setText(texto);
            return;
        }

        if (c instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) c;

            if (campos.get(i).tipo == Campo.LISTA_BANCO) {
                combo.setSelectedIndex(-1);

                if (valor instanceof Opcao) {
                    int id = ((Opcao) valor).id;

                    for (int k = 0; k < combo.getItemCount(); k++) {
                        Object item = combo.getItemAt(k);

                        if (item instanceof Opcao && ((Opcao) item).id == id) {
                            combo.setSelectedIndex(k);
                            break;
                        }
                    }
                }

            } else if (combo.getItemCount() > 0) {
                int escolhido = 0;

                for (int k = 0; k < combo.getItemCount(); k++) {
                    if (String.valueOf(combo.getItemAt(k)).equals(texto)) {
                        escolhido = k;
                        break;
                    }
                }

                combo.setSelectedIndex(escolhido);
            }
        }
    }
}