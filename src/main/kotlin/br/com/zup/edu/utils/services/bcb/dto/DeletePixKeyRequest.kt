package br.com.zup.edu.utils.services.bcb.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)