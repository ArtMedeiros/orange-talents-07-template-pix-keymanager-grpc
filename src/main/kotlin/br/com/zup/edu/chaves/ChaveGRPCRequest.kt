package br.com.zup.edu.chaves

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.validation.ChaveValida
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected @ChaveValida
data class ChaveGRPCRequest(
    @field:NotBlank
    val cliente: String,
    @field:NotNull
    val tipo: TipoChave,
    var chave: String,
    @field:NotNull
    val tipoConta: TipoConta
) {
    init {
        if(tipo == TipoChave.RANDOM)
            chave == UUID.randomUUID().toString()
    }

    fun toChaveEntity(): ChaveEntity {
        val tipoChave = TipoChaveEntity.valueOf(tipo.name)
        val conta = TipoContaEntity.valueOf(tipoConta.name)

        return ChaveEntity(cliente, tipoChave, chave, conta)
    }
}