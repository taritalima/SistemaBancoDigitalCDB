package br.com.cdb.bancodigitaljpa.utils;

import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import org.springframework.stereotype.Component;

@Component
public class CpfUtil {

    public boolean isValid(String cpf) {
        if (cpf == null) return false;

        String cpfLimpo = cpf.replaceAll("[^\\d]", "");

        if (cpfLimpo.length() != 11) return false;

        CPFValidator validator = new CPFValidator();
        try {
            validator.assertValid(cpfLimpo); 
            return true;
        } catch (InvalidStateException e) {
            return false;
        }
    }
}
