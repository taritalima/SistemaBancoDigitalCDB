package br.com.cdb.bancodigitaljpa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.cdb.bancodigitaljpa.entity.Cliente;
import br.com.cdb.bancodigitaljpa.entity.Conta;
import br.com.cdb.bancodigitaljpa.entity.ContaCorrente;
import br.com.cdb.bancodigitaljpa.entity.ContaPoupanca;
import br.com.cdb.bancodigitaljpa.repository.ClienteRepository;
import br.com.cdb.bancodigitaljpa.repository.ContaRepository;

@Service
public class ContaService {
	@Autowired
	private ContaRepository contaRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	public Conta salvarConta(Long clienteId, Conta conta) {
		Cliente cliente = clienteRepository.findById(clienteId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

		conta.setCliente(cliente);
		conta.setSaldo(0.0);

		if (conta instanceof ContaPoupanca) {
			ContaPoupanca cp = (ContaPoupanca) conta;

			if (cp.getTaxaRendimento() == null) {
				cp = new ContaPoupanca(cliente.getTipoCliente());
				cp.setCliente(cliente);
				cp.setSaldo(0.0);
				conta = cp;
			}
		}

		if (conta instanceof ContaCorrente cc) {
			if (cc.getTaxaManutencaoMensal() == null) {
				cc = new ContaCorrente(cliente.getTipoCliente());
				cc.setCliente(cliente);
				cc.setSaldo(0.0);
				conta = cc;
			}
		}

		return contaRepository.save(conta);
	}

	public List<Conta> findAll() {
		return contaRepository.findAll();

	}

	public Optional<Conta> buscarPorId(Long id) {
		return contaRepository.findById(id);
	}

	public void pagarViaPix(Long id, String chavePixDestino, Double valor) {
		Conta contaOrigem = contaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada."));

		if (valor == null || valor <= 0) {
			throw new IllegalArgumentException("Valor inválido para pagamento.");
		}

		if (contaOrigem.getSaldo() < valor) {
			throw new IllegalArgumentException("Saldo insuficiente para pagamento.");
		}

		contaOrigem.setSaldo(contaOrigem.getSaldo() - valor);
		contaRepository.save(contaOrigem);
	}

	public void depositar(Long id, Double valor) {
		Conta conta = contaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
		if (conta != null && valor > 0) {
			conta.setSaldo(conta.getSaldo() + valor);
			contaRepository.save(conta);
		} else {
			throw new IllegalArgumentException("Conta não encontrada ou valor inválido para depósito.");
		}
	}

	public void sacar(Long id, Double valor) {
		Conta conta = contaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
		if (conta == null) {
			throw new IllegalArgumentException("Conta não encontrada.");
		}

		if (valor == null || valor <= 0) {
			throw new IllegalArgumentException("Valor inválido para saque.");
		}

		if (conta.getSaldo() < valor) {
			throw new IllegalArgumentException("Saldo insuficiente.");
		}
		conta.setSaldo(conta.getSaldo() - valor);
		contaRepository.save(conta);
	}

	public void tranferir(Long idOrigem, int contaDestino, Double valor) {
		Conta contaD = contaRepository.findByNumeroConta(contaDestino);
		Conta contaO = contaRepository.findById(idOrigem)
				.orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

		if (contaD == null) {
			throw new IllegalArgumentException("Conta não encontrada.");

		}

		if (valor == null || valor <= 0) {
			throw new IllegalArgumentException("Valor inválido para tranfêrencia.");
		}

		if (contaO.getSaldo() < valor) {
			throw new IllegalArgumentException("Saldo insuficiente.");
		}
		contaO.setSaldo(contaO.getSaldo() - valor);
		contaRepository.save(contaO);

		contaD.setSaldo(contaD.getSaldo() + valor);
		contaRepository.save(contaD);

	}

	public void aplicarManutencao(Long id) {
	    Optional<Conta> contaOpt = contaRepository.findById(id);

	    if (!contaOpt.isPresent()) {
	        throw new IllegalArgumentException("Conta não encontrada!");
	    }
	    
	    Conta conta = contaOpt.get();

		if (conta instanceof ContaCorrente cc) {
			if (cc.getSaldo() < cc.getTaxaManutencaoMensal()) {
				throw new IllegalArgumentException("Saldo insuficiente para aplicar a taxa de manutenção.");
			}
			cc.setSaldo(cc.getSaldo() - cc.getTaxaManutencaoMensal());
			contaRepository.save(cc);
		} else {
			throw new IllegalArgumentException("Essa conta não é do tipo Conta Corrente.");
		}
	}

	public void aplicarRendimento(Long id) {
	    Optional<Conta> contaOpt = contaRepository.findById(id);

	    if (!contaOpt.isPresent()) {
	        throw new IllegalArgumentException("Conta não encontrada!");
	    }
	    
	    Conta conta = contaOpt.get();
	    
		if (conta instanceof ContaPoupanca cp) {
			cp.aplicarRendimento();
			contaRepository.save(cp);
		} else {
			throw new IllegalArgumentException("Essa conta não é do tipo Conta Poupança.");
		}
	}

}
