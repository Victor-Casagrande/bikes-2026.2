package br.edu.ifc.bikes.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

//sem @Configuration o Spring não cria esta classe e o @PostConstruct nunca é executado
@Configuration
public class SpringTimezoneConfig {

    @PostConstruct //garante que o método seja executado logo após o construtor
                   // da classe inicializada pelo Spring
    public void timezoneConfig(){

        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }
}