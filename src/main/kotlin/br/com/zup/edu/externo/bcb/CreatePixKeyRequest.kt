package br.com.zup.edu.externo.bcb

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreatePixKeyRequest(
    @field:NotNull
    val keyType: TipoChaveBCB,

    @field:NotBlank
    val key: String,

    @field:NotNull
    val bankAccount: BankAccountRequest,

    @field:NotNull
    val owner: OwnerRequest
)