package br.com.zup.edu.chaves

import javax.persistence.*
import javax.persistence.EnumType.*
import javax.persistence.GenerationType.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChaveEntity(
    @field:NotBlank
    val idCliente: String,

    @field:NotNull @Enumerated(value = STRING)
    val tipo: TipoChaveEntity,

    @field:NotBlank @Size(max = 77)
    var valor: String,

    @field:NotNull @Enumerated(value = STRING)
    val tipoConta: TipoContaEntity,

    @field:NotNull
    val banco: String
) {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null
}