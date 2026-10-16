package com.orbe.reembolso.suporte;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.orbe.reembolso.config.ClockConfig;

/** "Hoje", para todos os testes, é 20/10/2026 às 10h em São Paulo. */
@TestConfiguration
public class RelogioDeTeste {

    public static final LocalDate HOJE = LocalDate.of(2026, 10, 20);

    public static Clock fixo() {
        return Clock.fixed(ZonedDateTime.of(HOJE.atTime(10, 0), ClockConfig.FUSO_PADRAO).toInstant(),
                ClockConfig.FUSO_PADRAO);
    }

    @Bean
    @Primary
    Clock relogioFixo() {
        return fixo();
    }
}
