package br.com.cdb.bancodigitaljpa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.cdb.bancodigitaljpa.entity.Cliente;
import br.com.cdb.bancodigitaljpa.entity.Endereco;
import br.com.cdb.bancodigitaljpa.repository.ClienteRepository;
import br.com.cdb.bancodigitaljpa.utils.CpfUtil;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private EnderecoService enderecoService;

	@Autowired
	private CpfUtil cpfUtil;

	public Cliente salvarCliente(Cliente cliente) {

		if (cliente.getNome() == null) {
			throw new IllegalArgumentException("Nome não pode ser vazio!");
		}
		if (!cliente.getNome().matches("^[A-Za-zÀ-ÿ ]+$")) {
			throw new IllegalArgumentException("Nome inválido: Deve conter apenas letras e espaços");
		}

		if (cliente.getNome().length() < 2 || cliente.getNome().length() > 100) {
			throw new IllegalArgumentException("Nome deve ter entre 2 e 100 caracteres.");
		}
		
    
		if (!cliente.getCpf().matches("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")) {
			throw new IllegalArgumentException("Formato do CPF inválido. Use xxx.xxx.xxx-xx");
		}

		if (!cpfUtil.isValid(cliente.getCpf())) {
			throw new IllegalArgumentException("CPF inválido.");
		}
		
		try {
			Endereco endereco = enderecoService.buscarEnderecoPorCep(cliente.getEndereco().getCep());
			endereco.setNumero(cliente.getEndereco().getNumero());
			endereco.setComplemento(cliente.getEndereco().getComplemento());
			cliente.setEndereco(endereco);
			return clienteRepository.save(cliente);
			
		} catch (RuntimeException e) {
			throw new RuntimeException("Falha ao validar CEP: " + e.getMessage());
		}
	}

	public void deletarClientePorId(Long id) {
		clienteRepository.deleteById(id);
	}

	public List<Cliente> getClientes() {
		return clienteRepository.findAll();
	}

	public Optional<Cliente> buscarPorId(Long id) {
		return clienteRepository.findById(id);
	}

	public boolean existsByCpf(String cpf) {
		return clienteRepository.existsByCpf(cpf);
	}

	public boolean dataNascimentoValida(Cliente cliente) {
		try {
			LocalDate dataNascimento = LocalDate.parse(cliente.getDataNascimento(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));

			LocalDate hoje = LocalDate.now();

			if (dataNascimento.isAfter(hoje)) {
				return false;
			}

			if (dataNascimento.isAfter(hoje.minusYears(18))) {
				return false;
			}

			return true;

		} catch (DateTimeParseException e) {
			return false;
		}
	}

}
