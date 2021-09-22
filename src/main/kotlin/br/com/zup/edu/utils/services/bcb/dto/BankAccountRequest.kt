package br.com.zup.edu.utils.services.bcb.dto

import br.com.zup.edu.utils.services.bcb.TipoContaBCB
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
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
