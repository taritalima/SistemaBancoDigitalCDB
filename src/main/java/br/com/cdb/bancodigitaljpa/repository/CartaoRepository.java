package br.com.cdb.bancodigitaljpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.cdb.bancodigitaljpa.entity.Cartoes;

@Repository
public interface CartaoRepository extends JpaRepository<Cartoes, Long> {
}