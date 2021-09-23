package br.com.zup.edu.chaves.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class RemoverChaveRequest(
    @field:NotNull
    val id: Long,
    @field:NotBlank
    val cliente: String
)