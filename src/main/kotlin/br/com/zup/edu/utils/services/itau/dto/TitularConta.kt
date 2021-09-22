package br.com.zup.edu.utils.services.itau.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class TitularConta(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val nome: String,

    @field:NotBlank
    val cpf: String,
)
