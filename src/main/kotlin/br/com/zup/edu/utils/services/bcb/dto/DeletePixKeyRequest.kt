package br.com.zup.edu.utils.services.bcb.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class DeletePixKeyRequest(
    @field:NotBlank
    val key: String,

    @field:NotBlank
    val participant: String
)