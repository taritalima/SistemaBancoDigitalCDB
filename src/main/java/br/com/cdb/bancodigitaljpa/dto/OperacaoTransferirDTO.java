package br.com.cdb.bancodigitaljpa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperacaoTransferirDTO {
	private int numeroContaDestino;
	private int numeroContaOrigem;
	private double valor;
}
