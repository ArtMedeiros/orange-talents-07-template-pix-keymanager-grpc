package br.com.zup.edu.utils.services.itau

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class InstituicaoResponse(
    @field:NotBlank
    val nome: String,
    @field:NotBlank
    val ispb: String
)