package br.com.cdb.bancodigitaljpa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagamentoCartaoDTO {
	private String numeroCartao;
	private int senha;
	private Double valor;
	private Double limite;
    private String descricao; 
	private Double limiteDiario;
}
