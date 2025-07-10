package br.com.cdb.bancodigitaljpa.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MessageResponseDTO {
	private String message;
	private int status;
	private Instant timestamp;
}
