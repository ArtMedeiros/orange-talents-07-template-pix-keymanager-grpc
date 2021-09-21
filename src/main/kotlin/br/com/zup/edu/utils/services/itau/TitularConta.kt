package br.com.zup.edu.utils.services.itau

import javax.validation.constraints.NotBlank

data class TitularConta(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val nome: String,

    @field:NotBlank
    val cpf: String,
)
