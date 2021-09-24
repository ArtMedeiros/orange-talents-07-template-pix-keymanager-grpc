package br.com.zup.edu.utils.services.itau.dto

import br.com.zup.edu.chaves.Titular
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class TitularConta(
    val id: String,
    val nome: String,
    val cpf: String,
) {
    fun toModel(): Titular {
        return Titular(
            nomeCliente = nome,
            cpfCliente = cpf
        )
    }
}
