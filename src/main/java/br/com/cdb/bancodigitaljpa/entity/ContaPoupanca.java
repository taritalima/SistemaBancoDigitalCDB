package br.com.cdb.bancodigitaljpa.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cdb.bancodigitaljpa.enums.TipoCliente;
import jakarta.persistence.Entity;

@Entity
public class ContaPoupanca extends Conta {
	private Double taxaRendimento;

	public Double getTaxaRendimento() {
		return taxaRendimento;
	}

	public void setTaxaRendimento(Double taxaRendimento) {
		this.taxaRendimento = taxaRendimento;
	}

	public ContaPoupanca() {
		super();
	}

	public ContaPoupanca(TipoCliente tipoCliente) {
		super();
		switch (tipoCliente) {
		case COMUM:
			this.taxaRendimento = 0.005;
			break;
		case SUPER:
			this.taxaRendimento = 0.007;
			break;
		case PREMIUM:
			this.taxaRendimento = 0.009;
			break;
		default:
			this.taxaRendimento = 0.005;
		}
	}

	public void aplicarRendimento() {
		double taxaMensal = Math.pow(1 + taxaRendimento, 1.0 / 12.0) - 1;
		double novoSaldo = getSaldo() * (1 + taxaMensal);
		BigDecimal saldoArredondado = new BigDecimal(novoSaldo).setScale(2, RoundingMode.HALF_UP);
	    setSaldo(saldoArredondado.doubleValue());

	}

	@Override
	public String getTipoConta() {
		return "poupanca";

	}

}
