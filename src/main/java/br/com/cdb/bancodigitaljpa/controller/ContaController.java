package br.com.cdb.bancodigitaljpa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cdb.bancodigitaljpa.entity.Cliente;
import br.com.cdb.bancodigitaljpa.entity.Conta;
import br.com.cdb.bancodigitaljpa.entity.ContaCorrente;
import br.com.cdb.bancodigitaljpa.entity.ContaPoupanca;
import br.com.cdb.bancodigitaljpa.repository.ClienteRepository;
import br.com.cdb.bancodigitaljpa.repository.ContaCorrenteRepository;
import br.com.cdb.bancodigitaljpa.repository.ContaPoupancaRepository;
import br.com.cdb.bancodigitaljpa.service.ContaService;
import br.com.cdb.bancodigitaljpa.dto.MessageResponseDTO;
import br.com.cdb.bancodigitaljpa.dto.OperacaoDTO;
import br.com.cdb.bancodigitaljpa.dto.OperacaoTransferirDTO;
import br.com.cdb.bancodigitaljpa.dto.PagamentoPixDTO;
import br.com.cdb.bancodigitaljpa.dto.SaldoDTO;
import br.com.cdb.bancodigitaljpa.dto.TipoContaDTO;
import java.time.Instant;

@RestController
@RequestMapping("/contas")
public class ContaController {
	@Autowired
	private ContaCorrenteRepository contaCorrenteRepository;

	@Autowired
	private ContaPoupancaRepository contaPoupancaRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ContaService contaService;

	@PostMapping("/{clienteId}")
	public ResponseEntity<MessageResponseDTO> criarConta(@PathVariable Long clienteId,
			@RequestBody TipoContaDTO tipoContaDTO) {
		Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
		String tipo = tipoContaDTO.getTipoConta();

		if (clienteOpt.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Cliente não encontrado.",
					HttpStatus.NOT_FOUND.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Cliente cliente = clienteOpt.get();

		if (tipo.equalsIgnoreCase("corrente")) {
			ContaCorrente contaCorrente = new ContaCorrente(cliente.getTipoCliente());
			contaCorrente.setCliente(cliente);
			contaCorrenteRepository.save(contaCorrente);

			MessageResponseDTO response = new MessageResponseDTO("Conta corrente criada com sucesso.",
					HttpStatus.CREATED.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} else if (tipo.equalsIgnoreCase("poupanca")) {
			ContaPoupanca contaPoupanca = new ContaPoupanca(cliente.getTipoCliente());
			contaPoupanca.setCliente(cliente);
			contaPoupancaRepository.save(contaPoupanca);

			MessageResponseDTO response = new MessageResponseDTO("Conta poupança criada com sucesso.",
					HttpStatus.CREATED.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		}

		MessageResponseDTO response = new MessageResponseDTO("Tipo de conta inválido. Use 'corrente' ou 'poupanca'.",
				HttpStatus.BAD_REQUEST.value(), Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@PostMapping("/{id}/depositar")
	public ResponseEntity<MessageResponseDTO> depositar(@PathVariable Long id, @RequestBody OperacaoDTO operacao) {
		try {
			contaService.depositar(id, operacao.getValor());
			MessageResponseDTO response = new MessageResponseDTO("Depósito realizado com sucesso.",
					HttpStatus.OK.value(), Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PostMapping("/{id}/sacar")
	public ResponseEntity<MessageResponseDTO> sacar(@PathVariable Long id, @RequestBody OperacaoDTO operacao) {
		try {
			contaService.sacar(id, operacao.getValor());
			MessageResponseDTO response = new MessageResponseDTO("Saque realizado com sucesso.", HttpStatus.OK.value(),
					Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PostMapping("/{id}/transferir")
	public ResponseEntity<MessageResponseDTO> transferir(@PathVariable Long id,
			@RequestBody OperacaoTransferirDTO operacaoTransferir) {
		try {
			contaService.tranferir(id, operacaoTransferir.getNumeroContaDestino(), operacaoTransferir.getValor());
			MessageResponseDTO response = new MessageResponseDTO("Transferência realizada com sucesso.",
					HttpStatus.OK.value(), Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PostMapping("/{id}/pix")
	public ResponseEntity<MessageResponseDTO> pagamentoViaPix(@PathVariable Long id,
			@RequestBody PagamentoPixDTO pagamento) {
		try {
			contaService.pagarViaPix(id, pagamento.getChavePix(), pagamento.getValor());
			MessageResponseDTO response = new MessageResponseDTO("Pagamento via Pix realizado com sucesso.",
					HttpStatus.OK.value(), Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PutMapping("/{id}/manutencao")
	public ResponseEntity<MessageResponseDTO> aplicarManutencao(@PathVariable Long id) {
		try {
			contaService.aplicarManutencao(id);
			MessageResponseDTO response = new MessageResponseDTO("Taxa de manutenção aplicada com sucesso.",
					HttpStatus.OK.value(), Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PutMapping("/{id}/rendimento")
	public ResponseEntity<MessageResponseDTO> aplicarRendimento(@PathVariable Long id) {
		try {
			contaService.aplicarRendimento(id);
			MessageResponseDTO response = new MessageResponseDTO("Rendimento aplicado com sucesso.",
					HttpStatus.OK.value(), Instant.now());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@GetMapping("/listAll")
	public ResponseEntity<List<Conta>> getAllContas() {
		List<Conta> contas = contaService.findAll();
		return new ResponseEntity<>(contas, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarContaPorId(@PathVariable Long id) {
		Optional<Conta> conta = contaService.buscarPorId(id);
		if (conta.isPresent()) {
			return new ResponseEntity<Conta>(conta.get(), HttpStatus.OK);
		} else {
			MessageResponseDTO response = new MessageResponseDTO("Conta não encontrada!", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@GetMapping("/{id}/saldo")
	public ResponseEntity<?> buscarSaldo(@PathVariable Long id) {
		Optional<Conta> conta = contaService.buscarPorId(id);
		if (conta.isPresent()) {
			return ResponseEntity.ok(new SaldoDTO(conta.get().getSaldo()));
		} else {
			MessageResponseDTO response = new MessageResponseDTO("Conta não encontrada!", HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

}
