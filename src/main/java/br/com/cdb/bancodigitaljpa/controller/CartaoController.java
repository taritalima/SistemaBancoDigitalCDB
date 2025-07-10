package br.com.cdb.bancodigitaljpa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cdb.bancodigitaljpa.dto.MessageResponseDTO;
import br.com.cdb.bancodigitaljpa.dto.PagamentoCartaoDTO;
import br.com.cdb.bancodigitaljpa.dto.TipoCartaoDTO;
import br.com.cdb.bancodigitaljpa.entity.CartaoCredito;
import br.com.cdb.bancodigitaljpa.entity.CartaoDebito;
import br.com.cdb.bancodigitaljpa.entity.Cartoes;
import br.com.cdb.bancodigitaljpa.entity.Conta;
import br.com.cdb.bancodigitaljpa.entity.Transacao;
import br.com.cdb.bancodigitaljpa.repository.CartaoCreditoRepository;
import br.com.cdb.bancodigitaljpa.repository.CartaoRepository;
import br.com.cdb.bancodigitaljpa.repository.ContaRepository;
import br.com.cdb.bancodigitaljpa.service.CartaoCreditoService;
import br.com.cdb.bancodigitaljpa.service.CartaoDebitoService;
import br.com.cdb.bancodigitaljpa.service.CartaoService;
import jakarta.validation.Valid;

import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/cartao")
public class CartaoController {

	@Autowired
	ContaRepository contaRepository;

	@Autowired
	CartaoService cartaoService;

	@Autowired
	CartaoRepository cartaoRepository;

	@Autowired
	CartaoDebitoService cartaoDebitoService;

	@Autowired
	CartaoCreditoService cartaoCreditoService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	CartaoCreditoRepository cartaoCreditoRepository;

	@PostMapping("/{contaId}")
	public ResponseEntity<MessageResponseDTO> criarCartao(@PathVariable Long contaId,
			@Valid @RequestBody TipoCartaoDTO tipoCartao) {
		Optional<Conta> contaOptional = contaRepository.findById(contaId);

		if (contaOptional.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Conta não encontrada.", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

		}

		try {
			cartaoService.criarCartao(contaOptional.get(), tipoCartao);
			MessageResponseDTO response = new MessageResponseDTO("Cartão criado com sucesso.",
					HttpStatus.CREATED.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());

			return ResponseEntity.badRequest().body(response);
		}
	}

	@PostMapping("/{id}/pagamento")
	public ResponseEntity<MessageResponseDTO> realizarPagamento(@PathVariable Long id,
			@Valid @RequestBody PagamentoCartaoDTO dto) {
		try {
			cartaoService.pagamento(id, dto);
			MessageResponseDTO response = new MessageResponseDTO("Pagamento realizado com sucesso.",
					HttpStatus.OK.value(), Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PostMapping("/{id}/fatura/pagamento")
	public ResponseEntity<MessageResponseDTO> pagamentoFatura(@PathVariable Long id) {
		try {
			cartaoService.pagarFatura(id);
			MessageResponseDTO response = new MessageResponseDTO("Fatura paga com sucesso.", HttpStatus.OK.value(),
					Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			MessageResponseDTO response = new MessageResponseDTO("Erro ao pagar fatura: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/{id}/fatura")
	public ResponseEntity<?> fatura(@PathVariable Long id) {
		try {
			List<Transacao> transacoes = cartaoService.buscarTransacoesPorCartao(id);

			if (transacoes.isEmpty()) {
				MessageResponseDTO response = new MessageResponseDTO("Nenhuma transação encontrada para este cartão.",
						HttpStatus.NOT_FOUND.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			List<Map<String, Object>> itensFatura = transacoes.stream().map(t -> {
				Map<String, Object> item = new HashMap<>();
				item.put("data", t.getData());
				item.put("descricao", t.getDescricao());
				item.put("valor", t.getValor());
				return item;
			}).toList();

			Optional<CartaoCredito> optCartao = cartaoCreditoRepository.findById(id);
			if (optCartao.isEmpty()) {
				MessageResponseDTO response = new MessageResponseDTO("Cartão de crédito não encontrado.",
						HttpStatus.NOT_FOUND.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			Double limiteOriginal = optCartao.get().getLimiteOriginal();
			if (limiteOriginal == null) {
				MessageResponseDTO response = new MessageResponseDTO(
						"Erro: limite original do cartão não está definido.", HttpStatus.INTERNAL_SERVER_ERROR.value(),
						Instant.now());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

			double total = transacoes.stream().filter(t -> !t.getDescricao().toLowerCase().contains("taxa"))
					.mapToDouble(Transacao::getValor).sum();

			double taxa = cartaoService.calcularTaxa(total, limiteOriginal);

			Map<String, Object> resposta = new HashMap<>();
			resposta.put("transacoes", itensFatura);
			resposta.put("total", total);
			resposta.put("taxaAplicada", taxa);
			resposta.put("valorTotalComTaxa", total + taxa);

			return ResponseEntity.ok(resposta);

		} catch (Exception e) {
			MessageResponseDTO response = new MessageResponseDTO("Erro ao buscar fatura: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/all")
	public ResponseEntity<?> listarCartoes() {
		List<Cartoes> cartoes = cartaoService.listarTodosCartoes();
		if (cartoes.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Nenhum cartão encontrado.",
					HttpStatus.NOT_FOUND.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		return ResponseEntity.ok(cartoes);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarCartaoPorId(@PathVariable Long id) {
		Optional<Cartoes> cartoes = cartaoService.buscarPorId(id);
		if (cartoes.isPresent()) {
			return ResponseEntity.ok(cartoes.get());
		} else {
			MessageResponseDTO response = new MessageResponseDTO("Cartão não encontrado.", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@PutMapping("/{id}/limite")
	public ResponseEntity<MessageResponseDTO> atualizarLimite(@PathVariable Long id,
			@Valid @RequestBody TipoCartaoDTO tipoCartao) {
		Optional<CartaoCredito> cartaoOpt = cartaoCreditoService.buscarPorId(id);

		if (cartaoOpt.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Cartão não encontrado!", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		CartaoCredito cartao = cartaoOpt.get();
		cartao.ajustarLimite(tipoCartao.getLimite());

	    cartaoCreditoService.salvar(cartao);

		MessageResponseDTO response = new MessageResponseDTO("Limite atualizado com sucesso.", HttpStatus.OK.value(),
				Instant.now());
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/limiteDiario")
	public ResponseEntity<MessageResponseDTO> atualizarLimiteDiario(@PathVariable Long id,
			@Valid @RequestBody TipoCartaoDTO tipoCartao) {
		Optional<CartaoDebito> cartaoOpt = cartaoDebitoService.buscarPorId(id);

		if (cartaoOpt.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Cartão não encontrado!", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		CartaoDebito cartao = cartaoOpt.get();
		cartao.ajustarLimite(tipoCartao.getLimiteDiario());

	    cartaoDebitoService.salvar(cartao);

		MessageResponseDTO response = new MessageResponseDTO("Limite diário atualizado com sucesso.",
				HttpStatus.OK.value(), Instant.now());
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<MessageResponseDTO> atualizarStatus(@PathVariable Long id,
			@Valid @RequestBody TipoCartaoDTO tipoCartao) {
		Optional<Cartoes> cartaoOpt = cartaoService.buscarPorId(id);

		if (cartaoOpt.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Cartão não encontrado!", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Cartoes cartao = cartaoOpt.get();
		cartao.ajustarStatus(tipoCartao.getAtivo());
		cartaoService.salvar(cartao);

		MessageResponseDTO response = new MessageResponseDTO("Status do cartão atualizado com sucesso.",
				HttpStatus.OK.value(), Instant.now());
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/senha")
	public ResponseEntity<MessageResponseDTO> atualizarSenha(@PathVariable Long id,
			@Valid @RequestBody TipoCartaoDTO tipoCartao) {
		Optional<Cartoes> cartaoOpt = cartaoService.buscarPorId(id);

		if (cartaoOpt.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Cartão não encontrado!", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Cartoes cartao = cartaoOpt.get();
		cartao.atualizarSenha(tipoCartao.getSenha(), passwordEncoder);
		cartaoService.salvar(cartao);

		MessageResponseDTO response = new MessageResponseDTO("Senha atualizada com sucesso.", HttpStatus.OK.value(),
				Instant.now());
		return ResponseEntity.ok(response);
	}

}
