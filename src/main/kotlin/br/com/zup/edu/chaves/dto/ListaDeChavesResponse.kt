package br.com.zup.edu.chaves.dto

import br.com.zup.edu.ListaResponse
import br.com.zup.edu.chaves.ChaveEntity
import br.com.zup.edu.utils.services.toListaResponse
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ListaDeChavesResponse(
    @field:NotNull
    val pixId: Long,

    @field:NotBlank
    val clienteId: String,

    @field:NotBlank
    val tipoChave:String,

    @field:NotBlank
    val valor:String,

    @field:NotBlank
    val tipoConta:String,

    @field:NotBlank
    val criadaEm: LocalDateTime
) {

    companion object {
        fun criarLista(lista: List<ChaveEntity>): ListaResponse {
            val listaResponse = lista.map { chave ->
                ListaDeChavesResponse(
                    pixId = chave.id!!,
                    clienteId = chave.idCliente,
                    tipoChave = chave.tipo.name,
                    valor = chave.valor,
                    tipoConta = chave.conta.tipoConta.name,
                    criadaEm = chave.criadaEm
                )
            }

            return toListaResponse(listaResponse)
        }
    }
}