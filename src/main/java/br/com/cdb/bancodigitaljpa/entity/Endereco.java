package br.com.cdb.bancodigitaljpa.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Endereco {
	
	private String logradouro;
	private int numero;
	private String complemento;
	private String localidade;
	private String uf;
	private String cep;
	private String bairro;
}
