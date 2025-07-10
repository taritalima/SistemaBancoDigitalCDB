package br.com.cdb.bancodigitaljpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.cdb.bancodigitaljpa.entity.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> { 
    List<Transacao> findByCartaoId(Long cartaoId);
}