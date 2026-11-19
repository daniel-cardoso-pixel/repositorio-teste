package model;

import java.time.LocalDateTime;

public class Agendamento {
    public int id;
    public int codigoUsuario;      // usuário que fez o agendamento (obrigatório no banco)
    public int codigoPaciente;
    public String nomePaciente;
    public int codigoMedico;
    public String nomeMedico;
    public LocalDateTime dataHora;
    public String tipo;            // "Consulta" ou "Retorno"
    public String status;          // "Agendado", "Cancelado" ou "Atendido"
    public String observacao;
}