package br.com.zup.edu.utils.services.bcb.dto

import br.com.zup.edu.utils.services.bcb.TipoUsuarioBCB
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class OwnerRequest(
    val type: TipoUsuarioBCB,
    val name: String,
    val taxIdNumber: String
)
