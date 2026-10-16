package com.orbe.reembolso.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Toda regra que depende de data usa este Clock. Nos testes ele é substituído
 * por um relógio fixo, para que "hoje" seja sempre o mesmo dia.
 */
@Configuration
public class ClockConfig {

    public static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock clock() {
        return Clock.system(FUSO_PADRAO);
    }
}
