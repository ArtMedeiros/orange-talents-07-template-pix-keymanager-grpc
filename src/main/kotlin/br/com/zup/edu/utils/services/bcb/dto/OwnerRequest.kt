package br.com.zup.edu.utils.services.bcb.dto

import br.com.zup.edu.utils.services.bcb.TipoUsuarioBCB
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class OwnerRequest(
    @field:NotNull
    val type: TipoUsuarioBCB,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val taxIdNumber: String
)
