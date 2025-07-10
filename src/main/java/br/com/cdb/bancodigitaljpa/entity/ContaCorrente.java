package br.com.cdb.bancodigitaljpa.entity;

import br.com.cdb.bancodigitaljpa.enums.TipoCliente;
import jakarta.persistence.Entity;

@Entity

public class ContaCorrente extends Conta {

	private Double taxaManutencaoMensal;

	public ContaCorrente() {
		super();
	}

	public ContaCorrente(TipoCliente tipoCliente) {
		super();
		switch (tipoCliente) {
		case COMUM:
			this.taxaManutencaoMensal = 12.0;
			break;
		case SUPER:
			this.taxaManutencaoMensal = 8.0;
			break;
		case PREMIUM:
			this.taxaManutencaoMensal = 0.0;
			break;
		default:
			this.taxaManutencaoMensal = 12.0;
		}
	}

	public Double getTaxaManutencaoMensal() {
		return taxaManutencaoMensal;
	}

	public void aplicarTaxaMensal() {
		if (taxaManutencaoMensal != null && taxaManutencaoMensal > 0) {
			double novoSaldo = getSaldo() - taxaManutencaoMensal;
			setSaldo(novoSaldo);
		}
	}

	@Override
	public String getTipoConta() {
		return "corrente";
	}

}
