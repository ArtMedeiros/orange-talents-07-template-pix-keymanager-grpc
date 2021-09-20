package br.com.zup.edu.externo.bcb

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class BankAccountRequest(
    @field:NotBlank
    val participant: String,

    @field:NotBlank
    val branch: String,

    @field:NotBlank
    val accountNumber: String,

    @field:NotNull
    val accountType: TipoContaBCB
)
