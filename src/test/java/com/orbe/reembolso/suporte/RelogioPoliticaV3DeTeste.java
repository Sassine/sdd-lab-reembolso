package com.orbe.reembolso.suporte;

import static org.mockito.Mockito.spy;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.orbe.reembolso.config.ClockConfig;

@TestConfiguration
public class RelogioPoliticaV3DeTeste {

    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 3, 10, 0);

    @Bean
    @Primary
    Clock relogioPoliticaV3() {
        return spy(Clock.fixed(AGORA.atZone(ClockConfig.FUSO_PADRAO).toInstant(), ClockConfig.FUSO_PADRAO));
    }
}
