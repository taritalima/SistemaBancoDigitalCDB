package br.com.cdb.bancodigitaljpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.cdb.bancodigitaljpa.entity.ContaPoupanca;

@Repository
public interface ContaPoupancaRepository extends JpaRepository<ContaPoupanca, Long> {
    
    ContaPoupanca findByNumeroConta(int numeroConta);
}
