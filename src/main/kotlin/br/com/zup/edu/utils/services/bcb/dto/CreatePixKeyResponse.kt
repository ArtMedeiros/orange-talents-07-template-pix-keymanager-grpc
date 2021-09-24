package br.com.zup.edu.utils.services.bcb.dto

import br.com.zup.edu.utils.services.bcb.TipoChaveBCB
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class CreatePixKeyResponse(
    val keyType: TipoChaveBCB,

    val key: String,

    val bankAccount: BankAccountRequest,

    val owner: OwnerRequest,

    val createdAt: LocalDateTime
)