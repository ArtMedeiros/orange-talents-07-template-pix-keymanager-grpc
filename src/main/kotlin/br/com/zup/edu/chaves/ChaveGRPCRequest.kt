package br.com.zup.edu.chaves

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.utils.ChaveValida
import br.com.zup.edu.utils.services.itau.dto.ContaItauResponse
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
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

    private lateinit var conta: Conta

    private lateinit var criadaEm: LocalDateTime

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
            conta = conta,
            criadaEm = criadaEm
        )
    }

    fun chaveRandom(random: String) {
        valor = random
    }

    fun dadosConta(contaResponse: ContaItauResponse) {
        conta = contaResponse.toModel()
    }

    fun dataCriacao(data: LocalDateTime) {
        criadaEm = data
    }
}