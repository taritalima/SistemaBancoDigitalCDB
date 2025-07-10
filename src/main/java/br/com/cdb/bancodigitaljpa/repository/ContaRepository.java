package br.com.cdb.bancodigitaljpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.cdb.bancodigitaljpa.entity.Conta;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {
    Conta findByNumeroConta(int numeroConta);
    
}
