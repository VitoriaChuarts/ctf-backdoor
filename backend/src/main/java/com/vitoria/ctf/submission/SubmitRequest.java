package com.vitoria.ctf.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitRequest(
        @NotBlank @Size(max = 200) String flag
) {
}
