package br.com.zup.edu.utils.services.bcb.dto

import br.com.zup.edu.chaves.TipoContaEntity
import br.com.zup.edu.utils.services.bcb.TipoContaBCB
import br.com.zup.edu.utils.services.itau.dto.ContaItauResponse
import br.com.zup.edu.utils.services.itau.dto.InstituicaoResponse
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
) {

    fun tipoContaEntity(): TipoContaEntity {
        return when(accountType) {
            TipoContaBCB.SVGS -> TipoContaEntity.CONTA_POUPANCA
            else -> TipoContaEntity.CONTA_CORRENTE
        }
    }
}
