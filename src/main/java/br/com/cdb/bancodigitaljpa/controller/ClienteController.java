package br.com.cdb.bancodigitaljpa.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cdb.bancodigitaljpa.dto.MessageResponseDTO;
import br.com.cdb.bancodigitaljpa.entity.Cliente;
import br.com.cdb.bancodigitaljpa.entity.Endereco;
import br.com.cdb.bancodigitaljpa.service.ClienteService;
import br.com.cdb.bancodigitaljpa.service.EnderecoService;
import java.time.Instant;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private EnderecoService enderecoService;

	@PostMapping("/add")
	public ResponseEntity<MessageResponseDTO> addCliente(@RequestBody Cliente cliente) {
		if (clienteService.existsByCpf(cliente.getCpf())) {
			MessageResponseDTO response = new MessageResponseDTO("CPF já cadastrado!", HttpStatus.CONFLICT.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		if (!clienteService.dataNascimentoValida(cliente)) {
			MessageResponseDTO response = new MessageResponseDTO("Data de nascimento inválida ou menor de 18 anos.",
					HttpStatus.BAD_REQUEST.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		try {
			String cep = cliente.getEndereco().getCep();
			Endereco endereco = enderecoService.buscarEnderecoPorCep(cep);

			endereco.setNumero(cliente.getEndereco().getNumero());
			endereco.setComplemento(cliente.getEndereco().getComplemento());

			cliente.setEndereco(endereco);

			Cliente clienteAdicionado = clienteService.salvarCliente(cliente);

			if (clienteAdicionado != null) {
				MessageResponseDTO response = new MessageResponseDTO(
						"Cliente adicionado com sucesso: " + clienteAdicionado.getNome(), HttpStatus.CREATED.value(),
						Instant.now());
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			} else {
				MessageResponseDTO response = new MessageResponseDTO("Erro ao salvar cliente.",
						HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
					Instant.now());
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/listAll")
	public ResponseEntity<?> getAllClientes() {
		List<Cliente> clientes = clienteService.getClientes();

		if (clientes.isEmpty()) {
			MessageResponseDTO response = new MessageResponseDTO("Nenhum cliente encontrado!",
					HttpStatus.NOT_FOUND.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		return ResponseEntity.ok(clientes);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarClientePorId(@PathVariable Long id) {
		Optional<Cliente> cliente = clienteService.buscarPorId(id);
		if (cliente.isPresent()) {
			return ResponseEntity.ok(cliente.get());
		} else {
			MessageResponseDTO response = new MessageResponseDTO("Cliente não encontrado!",
					HttpStatus.NOT_FOUND.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
		try {
			Cliente clienteUpdate = clienteService.buscarPorId(id)
					.orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com id: " + id));

			if (!clienteUpdate.getNome().equals(cliente.getNome())) {
				MessageResponseDTO response = new MessageResponseDTO("Não é permitido o nome.",
						HttpStatus.BAD_REQUEST.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			if (!clienteUpdate.getDataNascimento().equals(cliente.getDataNascimento())) {
				MessageResponseDTO response = new MessageResponseDTO("Não é permitido alterar a data de nascimento.",
						HttpStatus.BAD_REQUEST.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			if (!clienteUpdate.getCpf().equals(cliente.getCpf())) {
				MessageResponseDTO response = new MessageResponseDTO("Não é permitido alterar o CPF.",
						HttpStatus.BAD_REQUEST.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			if (!clienteUpdate.getTipoCliente().equals(cliente.getTipoCliente())) {
				MessageResponseDTO response = new MessageResponseDTO("Não é permitido alterar o tipo do cliente.",
						HttpStatus.BAD_REQUEST.value(), Instant.now());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			Endereco novoEndereco = cliente.getEndereco();
			Endereco enderecoAtual = clienteUpdate.getEndereco();
			enderecoAtual.setNumero(novoEndereco.getNumero());
			enderecoAtual.setComplemento(novoEndereco.getComplemento());
			enderecoAtual.setCep(novoEndereco.getCep());

			Cliente update = clienteService.salvarCliente(clienteUpdate);

			return ResponseEntity.ok(update);

		} catch (IllegalArgumentException e) {
			MessageResponseDTO response = new MessageResponseDTO(e.getMessage(), HttpStatus.NOT_FOUND.value(),
					Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		} catch (Exception e) {
			MessageResponseDTO response = new MessageResponseDTO("Erro interno: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<MessageResponseDTO> deletarCliente(@PathVariable Long id) {
		Optional<Cliente> clienteExist = clienteService.buscarPorId(id);

		if (clienteExist.isPresent()) {
			clienteService.deletarClientePorId(id);
			MessageResponseDTO response = new MessageResponseDTO("Cliente removido com sucesso!", HttpStatus.OK.value(),
					Instant.now());
			return ResponseEntity.ok(response);
		} else {
			MessageResponseDTO response = new MessageResponseDTO("Cliente não encontrado!",
					HttpStatus.NOT_FOUND.value(), Instant.now());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

}
