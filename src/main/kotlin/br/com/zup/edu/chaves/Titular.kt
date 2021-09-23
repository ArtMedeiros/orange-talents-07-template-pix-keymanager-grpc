package br.com.zup.edu.chaves

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class Titular(
    @field:NotBlank
    val nomeCliente: String,

    @field:NotBlank
    val cpfCliente: String
)