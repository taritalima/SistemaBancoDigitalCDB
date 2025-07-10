package br.com.cdb.bancodigitaljpa.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("DEBITO")
@Getter
@Setter
public class CartaoDebito extends Cartoes {

	private double limiteDiario;
	@Column(name = "data_ultimo_uso_limite")
	private LocalDate dataUltimoUsoLimite;
	
	@Override
	public void ajustarLimite(Double novoLimite) {
		this.limiteDiario = novoLimite;
	}
	@Override
	public void ajustarStatus(Boolean novoStatus) {
	    if (novoStatus != null) {
	        this.setAtivo(novoStatus);
	    }
	}
}
