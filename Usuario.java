package model;

import java.util.HashSet;
import java.util.Set;

public class Usuario {
    private final int registro;
    private final String identificacao;
    private final Set<String> permissoes = new HashSet<>();

    public Usuario(int registro, String identificacao) {
        this.registro = registro;
        this.identificacao = identificacao;
    }

    public int getRegistro() { return registro; }
    public String getIdentificacao() { return identificacao; }
    public void adicionarPermissao(String p) { permissoes.add(p); }
    public boolean temPermissao(String p) { return permissoes.contains(p); }
}
