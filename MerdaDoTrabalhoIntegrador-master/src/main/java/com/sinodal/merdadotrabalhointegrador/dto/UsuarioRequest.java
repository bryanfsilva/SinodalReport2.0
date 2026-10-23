package com.sinodal.merdadotrabalhointegrador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 3, max = 50) String login,
        @NotBlank @Size(min = 6, max = 72) String senha,
        @NotBlank @Pattern(regexp = "ADMIN|CLIENTE", message = "deve ser ADMIN ou CLIENTE") String tipo) {
}
