package br.com.zup.edu.utils.services.bcb.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class DeletePixKeyRequest(
    @field:NotBlank
    val key: String,

    @field:NotBlank
    val participant: String
)