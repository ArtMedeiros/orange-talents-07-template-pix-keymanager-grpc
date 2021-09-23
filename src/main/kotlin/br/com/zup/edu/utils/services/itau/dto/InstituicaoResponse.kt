package br.com.zup.edu.utils.services.itau.dto

import br.com.zup.edu.chaves.Instituicao
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class InstituicaoResponse(
    @field:NotBlank
    val nome: String,
    @field:NotBlank
    val ispb: String
) {
    fun toModel(): Instituicao {
        return Instituicao(
            nomeInstituicao = nome,
            ispb = ispb
        )
    }
}