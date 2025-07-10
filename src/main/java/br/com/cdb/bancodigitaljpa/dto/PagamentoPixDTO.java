package br.com.cdb.bancodigitaljpa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagamentoPixDTO {
	private String chavePix; 
	private Double valor;
}
