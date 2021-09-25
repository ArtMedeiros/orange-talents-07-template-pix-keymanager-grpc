package br.com.zup.edu.chaves.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ConsultarChaveRequest(
    @field:NotBlank
    val clienteId: String,

    @field:NotNull
    val pixId: Long,

    @field:NotBlank
    val chave: String
)