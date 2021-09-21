package br.com.zup.edu.chaves

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.utils.ChaveValida
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected @ChaveValida
data class ChaveGRPCRequest(
    @field:NotBlank
    val cliente: String,
    @field:NotNull
    val tipo: TipoChaveEntity?,
    var chave: String,
    @field:NotNull
    val tipoConta: TipoContaEntity?
) {
    fun toChaveEntity(): ChaveEntity {
        if(tipo == TipoChaveEntity.RANDOM)
            chave = UUID.randomUUID().toString()

        return ChaveEntity(cliente, tipo!!, chave, tipoConta!!)
    }
}