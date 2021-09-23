package br.com.zup.edu.utils.services.itau.dto

import br.com.zup.edu.chaves.Conta
import br.com.zup.edu.chaves.TipoContaEntity
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ContaItauResponse(
    @field:NotNull
    val tipo: TipoContaEntity,

    @field:NotNull
    val instituicao: InstituicaoResponse,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numero: String,

    @field:NotNull
    val titular: TitularConta
) {

    fun toModel(): Conta {
        return Conta(
            tipoConta = tipo,
            instituicao = instituicao.toModel(),
            agencia = agencia,
            numero = numero,
            titular = titular.toModel()
        )
    }
}