package br.com.cdb.bancodigitaljpa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperacaoDTO {
	private int numeroConta;
	private double valor;
}
