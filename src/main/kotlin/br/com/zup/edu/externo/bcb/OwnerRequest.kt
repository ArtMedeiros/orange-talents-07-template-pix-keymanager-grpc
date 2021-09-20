package br.com.zup.edu.externo.bcb

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class OwnerRequest(
    @field:NotNull
    val type: TipoUsuarioBCB,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val taxIdNumber: String
)
