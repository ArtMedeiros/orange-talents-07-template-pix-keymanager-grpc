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
    val chave: String,
    @field:NotNull
    val tipoConta: TipoContaEntity?
) {

    var valor: String
        private set

    var banco: String = ""
        private set

    init{
        valor = when(tipo) {
            TipoChaveEntity.RANDOM -> UUID.randomUUID().toString()
            else -> chave
        }
    }

    fun toChaveEntity(): ChaveEntity {
        return ChaveEntity(
            idCliente = cliente,
            tipo = tipo!!,
            valor = valor,
            tipoConta = tipoConta!!,
            banco = banco
        )
    }

    fun chaveRandom(random: String) {
        valor = random
    }

    fun atualizarBanco(ispb: String) {
        banco = ispb
    }
}