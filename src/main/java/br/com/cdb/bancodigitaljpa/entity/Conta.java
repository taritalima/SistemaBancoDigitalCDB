package br.com.cdb.bancodigitaljpa.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_conta")
public abstract class Conta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Double saldo;
	private int numeroConta;

	
	@OneToMany(mappedBy = "conta", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Cartoes> cartoes;
	
	@ManyToOne
	private Cliente cliente;

	@Column(name = "data_criacao", updatable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao;

	public Conta() {
		this.numeroConta = gerarNumeroConta();
		this.saldo = 0.0;
	}

	private int gerarNumeroConta() {
		Random random = new Random();
		return 100000 + random.nextInt(900000);
	}

	public Double getSaldo() {
		return saldo;
	}

	public void setSaldo(Double saldo) {
		this.saldo = saldo;
	    this.dataAtualizacao = LocalDateTime.now();

	}

	public int getNumeroConta() {
		return numeroConta;
	}

	public void setNumeroConta(int numeroConta) {
		this.numeroConta = numeroConta;
	}

	@JsonIgnore
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	@JsonProperty("clienteId")
	public Long getClienteId() {
		return cliente != null ? cliente.getId() : null;
	}

	public abstract String getTipoConta();

	@PrePersist
	public void prePersist() {
		this.dataCriacao = LocalDateTime.now();
	}

}
