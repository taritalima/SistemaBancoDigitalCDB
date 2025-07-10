package br.com.cdb.bancodigitaljpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.cdb.bancodigitaljpa.entity.CartaoDebito;

@Repository
public interface CartaoDebitoRepository extends JpaRepository<CartaoDebito, Long> {
    
	CartaoDebito findByNumeroCartao(String numeroCartao);
}
