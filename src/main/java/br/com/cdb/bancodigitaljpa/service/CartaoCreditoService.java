package br.com.cdb.bancodigitaljpa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.cdb.bancodigitaljpa.entity.CartaoCredito;
import br.com.cdb.bancodigitaljpa.repository.CartaoCreditoRepository;

@Service
public class CartaoCreditoService {
	@Autowired
	private CartaoCreditoRepository cartaoCreditoRepository;

	public Optional<CartaoCredito> buscarPorId(Long id) {
		return cartaoCreditoRepository.findById(id);
	}
	
	public CartaoCredito salvar(CartaoCredito cartao) {
	    return cartaoCreditoRepository.save(cartao);
	}
}
