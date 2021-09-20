package br.com.zup.edu.externo.itau

import javax.validation.constraints.NotBlank

data class TitularConta(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val nome: String,

    @field:NotBlank
    val cpf: String,
)
