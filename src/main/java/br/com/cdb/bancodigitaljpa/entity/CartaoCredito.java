package br.com.cdb.bancodigitaljpa.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("CREDITO")
@Getter
@Setter
public class CartaoCredito extends Cartoes {

	private Double limiteCredito;
	private Double limiteOriginal;

	@Override
	public void ajustarLimite(Double novoLimite) {
		this.setLimiteCredito(novoLimite);
	}

	@Override
	public void ajustarStatus(Boolean novoStatus) {
		if (novoStatus != null) {
			this.setAtivo(novoStatus);
		}
	}


}
