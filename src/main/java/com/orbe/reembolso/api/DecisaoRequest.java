package com.orbe.reembolso.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DecisaoRequest(
        @NotBlank @Size(max = 80) String responsavel,
        @Size(max = 500) String motivo) {
}
