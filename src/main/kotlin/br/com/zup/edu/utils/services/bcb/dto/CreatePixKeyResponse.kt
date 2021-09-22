package br.com.zup.edu.utils.services.bcb.dto

import br.com.zup.edu.utils.services.bcb.TipoChaveBCB
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class CreatePixKeyResponse(
    @field:NotNull
    val keyType: TipoChaveBCB,

    @field:NotBlank
    val key: String,

    @field:NotNull
    val bankAccount: BankAccountRequest,

    @field:NotNull
    val owner: OwnerRequest,

    @field:NotNull
    val createdAt: LocalDateTime
)