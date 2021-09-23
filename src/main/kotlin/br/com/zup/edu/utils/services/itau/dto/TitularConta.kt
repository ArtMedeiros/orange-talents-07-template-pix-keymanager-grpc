package br.com.zup.edu.utils.services.itau.dto

import br.com.zup.edu.chaves.Titular
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class TitularConta(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val nome: String,

    @field:NotBlank
    val cpf: String,
) {
    fun toModel(): Titular {
        return Titular(
            nomeCliente = nome,
            cpfCliente = cpf
        )
    }
}
