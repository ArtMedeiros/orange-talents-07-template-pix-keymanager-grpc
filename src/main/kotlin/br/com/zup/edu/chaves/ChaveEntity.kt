package br.com.zup.edu.chaves

import java.time.LocalDateTime
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

    @field:NotNull
    @Embedded
    val conta: Conta,

    @field:NotNull
    val criadaEm: LocalDateTime
) {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null

    fun instituicao(): Instituicao {
        return conta.instituicao
    }

    fun titular(): Titular {
        return conta.titular
    }
}