package com.vitoria.ctf.challenge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContainerRequest(
        @NotBlank
        @Pattern(regexp = "^ctf-[a-z0-9._-]{1,60}:[a-zA-Z0-9._-]{1,30}$",
                 message = "a imagem deve começar com ctf- e ter uma versão (ex: ctf-sqli-login:1)")
        String image,

        @Min(1) @Max(65535)
        int port
) {
}
