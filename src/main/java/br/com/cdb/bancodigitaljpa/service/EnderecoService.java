package br.com.cdb.bancodigitaljpa.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import br.com.cdb.bancodigitaljpa.entity.Endereco;

@Service
public class EnderecoService {

	private final RestTemplate restTemplate = new RestTemplate();

	public Endereco buscarEnderecoPorCep(String cep) {
		if (cep == null) {
		    throw new IllegalArgumentException("CEP não pode ser nulo.");
		}

		if (!cep.matches("^\\d{5}-\\d{3}$")) {
		    throw new IllegalArgumentException("Formato do CEP inválido. Use xxxxx-xxx.");
		}
		
		String cepLimpo = cep.replace("-", "");

		String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

		try {
			Endereco endereco = restTemplate.getForObject(url, Endereco.class);

			if (endereco == null || endereco.getCep() == null) {
				throw new IllegalArgumentException("CEP inválido ou não encontrado.");
			}

			endereco.setCep(cep);

			return endereco;
		} catch (RestClientException e) {
			throw new RuntimeException("Erro ao buscar endereço pelo CEP: " + e.getMessage());
		}
	}
}
