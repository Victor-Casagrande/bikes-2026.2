package br.edu.ifc.bikes.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

//dados do cartão informados na solicitação; apenas os 4 últimos dígitos são gravados na locação
public record CartaoDto(
        //apenas dígitos; @CreditCardNumber confere o dígito verificador (algoritmo de Luhn)
        @NotBlank(message = "O número do cartão é obrigatório")
        @Pattern(regexp = "\\d{13,19}", message = "O número do cartão deve ter de 13 a 19 dígitos, sem espaços")
        @CreditCardNumber(message = "O número do cartão é inválido")
        String numero,
        @NotBlank(message = "O nome do titular é obrigatório")
        @Size(min = 3, max = 100, message = "O nome do titular deve ter entre 3 e 100 caracteres")
        String titular,
        @NotBlank(message = "A validade é obrigatória")
        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "A validade deve estar no formato MM/AA")
        String validade,
        @NotBlank(message = "O CVV é obrigatório")
        @Pattern(regexp = "\\d{3,4}", message = "O CVV deve ter 3 ou 4 dígitos")
        String cvv
) {
    //o cartão vale até o último dia do mês da validade
    @JsonIgnore
    @AssertTrue(message = "O cartão está vencido")
    public boolean isDentroDaValidade() {
        if (validade == null) return true; //o @NotBlank já acusa o erro
        try {
            YearMonth mesValidade = YearMonth.parse(validade, DateTimeFormatter.ofPattern("MM/yy"));
            return !mesValidade.isBefore(YearMonth.now());
        } catch (DateTimeParseException ex) {
            return true; //formato inválido: o @Pattern já acusa o erro
        }
    }

    public String ultimosDigitos() {
        return numero.substring(numero.length() - 4);
    }
}
