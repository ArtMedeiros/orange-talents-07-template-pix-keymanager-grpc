package br.com.zup.edu.chaves

import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Embeddable
class Conta(
    @field:NotNull @Enumerated(EnumType.STRING)
    val tipoConta: TipoContaEntity,

    @field:NotNull
    @Embedded
    val instituicao: Instituicao,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numero: String,

    @field:NotBlank
    @Embedded
    val titular: Titular
) {
    fun nomeTitular(): String {
        return titular.nomeCliente
    }
}