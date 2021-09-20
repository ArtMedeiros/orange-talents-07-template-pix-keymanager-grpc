package br.com.zup.edu.chaves

import javax.persistence.*
import javax.persistence.EnumType.*
import javax.persistence.GenerationType.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChaveEntity(
    @NotBlank
    val idCliente: String,
    @NotNull @Enumerated(value = STRING)
    val tipo: TipoChaveEntity,
    @NotBlank
    var valor: String,
    @NotNull @Enumerated(value = STRING)
    val tipoConta: TipoContaEntity
) {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null
}