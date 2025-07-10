package br.com.cdb.bancodigitaljpa.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) 
@DiscriminatorColumn(name = "tipo_cartao")
@Getter
@Setter
public abstract class Cartoes {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String numeroCartao;
	
	@JsonIgnore
	private String senha;
	private boolean ativo;
	@ManyToOne
	private Conta conta;

	
	@JsonProperty("tipo_cartao")
    public String getTipoCartao() {
        if (this instanceof CartaoDebito) return "DEBITO";
        if (this instanceof CartaoCredito) return "CREDITO";
        return "DESCONHECIDO";
    }

	public abstract void ajustarLimite(Double novoLimite);
	
	public abstract void ajustarStatus(Boolean novoStatus);
	
	
	public void atualizarSenha(String novaSenha, PasswordEncoder passwordEncoder) {
	    this.senha = passwordEncoder.encode(novaSenha);
	}

}
