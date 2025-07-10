package br.com.cdb.bancodigitaljpa.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.cdb.bancodigitaljpa.dto.PagamentoCartaoDTO;
import br.com.cdb.bancodigitaljpa.dto.TipoCartaoDTO;
import br.com.cdb.bancodigitaljpa.entity.CartaoCredito;
import br.com.cdb.bancodigitaljpa.entity.CartaoDebito;
import br.com.cdb.bancodigitaljpa.entity.Cartoes;
import br.com.cdb.bancodigitaljpa.entity.Conta;
import br.com.cdb.bancodigitaljpa.entity.Transacao;
import br.com.cdb.bancodigitaljpa.repository.CartaoCreditoRepository;
import br.com.cdb.bancodigitaljpa.repository.CartaoDebitoRepository;
import br.com.cdb.bancodigitaljpa.repository.CartaoRepository;
import br.com.cdb.bancodigitaljpa.repository.TransacaoRepository;

@Service
public class CartaoService {

	@Autowired
	private CartaoCreditoRepository cartaoCreditoRepository;

	@Autowired
	private CartaoDebitoRepository cartaoDebitoRepository;

	@Autowired
	private CartaoRepository cartaoRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TransacaoRepository transacaoRepository;

	public String gerarNumeroCartao() {
		Random random = new Random();
		String numeroCartao;

		do {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 16; i++) {
				sb.append(random.nextInt(10));
			}
			numeroCartao = sb.toString();
		} while (cartaoCreditoRepository.findByNumeroCartao(numeroCartao) != null
				|| cartaoDebitoRepository.findByNumeroCartao(numeroCartao) != null);

		return numeroCartao;
	}

	public Cartoes criarCartao(Conta conta, TipoCartaoDTO dto) {
		String tipo = dto.getTipoCartao();

		if (tipo.equalsIgnoreCase("credito")) {
			CartaoCredito cartaoCredito = new CartaoCredito();
			cartaoCredito.setConta(conta);
			cartaoCredito.setNumeroCartao(gerarNumeroCartao());
			cartaoCredito.setSenha(passwordEncoder.encode(dto.getSenha()));
			cartaoCredito.setAtivo(true);

			double limite;
			switch (conta.getCliente().getTipoCliente()) {
			case COMUM:
				limite = 1000.0;
				break;
			case SUPER:
				limite = 5000.0;
				break;
			case PREMIUM:
				limite = 10000.0;
				break;
			default:
				limite = 1000.0;
			}

			cartaoCredito.setLimiteCredito(limite);
			cartaoCredito.setLimiteOriginal(limite);
			return cartaoCreditoRepository.save(cartaoCredito);

		} else if (tipo.equalsIgnoreCase("debito")) {
			CartaoDebito cartaoDebito = new CartaoDebito();
			cartaoDebito.setConta(conta);
			cartaoDebito.setNumeroCartao(gerarNumeroCartao());
			cartaoDebito.setSenha(passwordEncoder.encode(dto.getSenha()));
			cartaoDebito.setAtivo(true);
//			cartaoDebito.ajustarLimite(dto.getLimite());
			return cartaoDebitoRepository.save(cartaoDebito);
		}

		throw new IllegalArgumentException("Tipo de cartão inválido.");
	}

	public Cartoes pagamento(Long id, PagamentoCartaoDTO dto) {

		Optional<CartaoCredito> opcCredito = cartaoCreditoRepository.findById(id);
		if (opcCredito.isPresent()) {
			CartaoCredito cartao = opcCredito.get();

			if (!cartao.isAtivo()) {
				throw new IllegalArgumentException("Cartão de crédito inativo.");
			}

			if (!cartao.getNumeroCartao().equals(dto.getNumeroCartao())
					|| !passwordEncoder.matches(String.valueOf(dto.getSenha()), cartao.getSenha())) {
				throw new IllegalArgumentException("Número do cartão ou senha inválidos.");
			}

			if (dto.getValor() == null || dto.getValor() <= 0) {
				throw new IllegalArgumentException("Valor inválido.");
			}

			if (cartao.getLimiteCredito() < dto.getValor()) {
				throw new IllegalArgumentException("Limite insuficiente.");
			}

			cartao.setLimiteCredito(cartao.getLimiteCredito() - dto.getValor());
			Cartoes cartaoSalvo = cartaoCreditoRepository.save(cartao);

			Transacao transacao = new Transacao();
			transacao.setCartao(cartaoSalvo);
			transacao.setData(LocalDateTime.now());
			transacao.setDescricao(dto.getDescricao());
			transacao.setValor(dto.getValor());
			transacaoRepository.save(transacao);

			return cartaoSalvo;
		}

		Optional<CartaoDebito> opcDebito = cartaoDebitoRepository.findById(id);
		if (opcDebito.isPresent()) {
			CartaoDebito cartao = opcDebito.get();

			if (cartao.getDataUltimoUsoLimite() == null || !cartao.getDataUltimoUsoLimite().isEqual(LocalDate.now())) {
				cartao.setDataUltimoUsoLimite(LocalDate.now());
			}

			if (!cartao.isAtivo()) {
				throw new IllegalArgumentException("Cartão de debito inativo.");
			}

			if (!passwordEncoder.matches(String.valueOf(dto.getSenha()), cartao.getSenha())) {
				throw new IllegalArgumentException("senha inválida.");
			}
			if (!cartao.getNumeroCartao().equals(dto.getNumeroCartao())) {
				throw new IllegalArgumentException("Número do cartão inválido.");
			}

			if (dto.getValor() == null || dto.getValor() <= 0) {
				throw new IllegalArgumentException("Valor inválido.");
			}

			if (cartao.getLimiteDiario() < dto.getValor()) {
				throw new IllegalArgumentException("Limite diário insuficiente.");
			}

			if (cartao.getConta().getSaldo() < dto.getValor()) {
				throw new IllegalArgumentException("Saldo insuficiente na conta.");
			}

			cartao.setLimiteDiario(cartao.getLimiteDiario() - dto.getValor());
			cartao.getConta().setSaldo(cartao.getConta().getSaldo() - dto.getValor());

			return cartaoDebitoRepository.save(cartao);
		}

		throw new IllegalArgumentException("Tipo de cartão inválido. Use 'credito' ou 'debito'.");

	}

	public void pagarFatura(Long id) {
		CartaoCredito cartao = cartaoCreditoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Cartão de crédito não encontrado."));

		Conta conta = cartao.getConta();

		List<Transacao> transacoes = transacaoRepository.findByCartaoId(id);

		if (transacoes.isEmpty()) {
			throw new IllegalArgumentException("Não há fatura pendente para este cartão.");
		}

		double totalFatura = transacoes.stream().filter(t -> !t.getDescricao().toLowerCase().contains("taxa"))
				.mapToDouble(Transacao::getValor).sum();

		double limiteOriginal = cartao.getLimiteCredito() + totalFatura;
		double taxa = calcularTaxa(totalFatura, limiteOriginal);
		if (taxa > 0) {
			totalFatura += taxa;
		}

		if (conta.getSaldo() < totalFatura) {
			throw new IllegalArgumentException("Saldo insuficiente para pagar a fatura.");
		}

		conta.setSaldo(conta.getSaldo() - totalFatura);
		cartao.setLimiteCredito(limiteOriginal);

		transacaoRepository.deleteAll(transacoes);

		cartaoCreditoRepository.save(cartao);
	}

	public double calcularTaxa(double total, double limiteOriginal) {
		return (total > limiteOriginal * 0.8) ? total * 0.05 : 0;
	}

	public List<Cartoes> listarTodosCartoes() {
		return cartaoRepository.findAll();
	}

	public Optional<Cartoes> buscarPorId(Long id) {
		return cartaoRepository.findById(id);
	}

	public Cartoes salvar(Cartoes cartao) {
		return cartaoRepository.save(cartao);
	}

	public List<Transacao> buscarTransacoesPorCartao(Long cartaoId) {
		return transacaoRepository.findByCartaoId(cartaoId);
	}
}