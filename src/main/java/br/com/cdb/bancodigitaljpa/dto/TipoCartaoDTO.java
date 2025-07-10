package br.com.cdb.bancodigitaljpa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoCartaoDTO {
	private String tipoCartao;
	private String numeroCartao;
	private String senha;
	private Double limite;
	private Double limiteDiario;
	private Boolean ativo;
}
