package br.com.cdb.bancodigitaljpa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.cdb.bancodigitaljpa.entity.CartaoDebito;
import br.com.cdb.bancodigitaljpa.repository.CartaoDebitoRepository;

@Service
public class CartaoDebitoService {

	@Autowired
	private CartaoDebitoRepository cartaoDebitoRepository;

	public Optional<CartaoDebito> buscarPorId(Long id) {
		return cartaoDebitoRepository.findById(id);
	}
	
	public CartaoDebito salvar(CartaoDebito cartao) {
	    return cartaoDebitoRepository.save(cartao);
	}

}
