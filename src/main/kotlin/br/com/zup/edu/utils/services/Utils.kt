package br.com.zup.edu.utils.services

import br.com.zup.edu.*
import br.com.zup.edu.ConsultaRequest.FiltroCase.FILTRO_NOT_SET
import br.com.zup.edu.TipoChave.INVALID
import br.com.zup.edu.TipoConta.UNKNOW
import br.com.zup.edu.chaves.TipoChaveEntity
import br.com.zup.edu.chaves.TipoContaEntity
import br.com.zup.edu.chaves.dto.*
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.ConstraintViolationException

fun ChaveRequest.toModel(): RegistrarChaveRequest {
    return RegistrarChaveRequest(
        cliente = cliente,
        tipo = when (tipoChave) {
            INVALID -> throw ConstraintViolationException("Request inválida", setOf())
            else -> TipoChaveEntity.valueOf(tipoChave.name)
        },
        chave = chave,
        tipoConta = when (conta) {
            UNKNOW -> throw ConstraintViolationException("Request inválida", setOf())
            else -> TipoContaEntity.valueOf(conta.name)
        }
    )
}

fun RemoverRequest.toModel(): RemoverChaveRequest {
    return RemoverChaveRequest(
        id = pixId,
        cliente = cliente
    )
}

fun ConsultaRequest.toModel(): ConsultarChaveRequest {
    return when(filtroCase) {
        FILTRO_NOT_SET -> throw ConstraintViolationException("Request inválida", setOf())
        else -> ConsultarChaveRequest(
            pixId = pixId.pixId,
            clienteId = pixId.clienteId,
            chave = chave
        )
    }
}

fun toConsultaResponse(dados: DetalhesDadosChave) : ConsultaResponse {
    return convertToConsultaChaveResponse(dados)
}

fun toListaResponse(dados: List<ListaDeChavesResponse>): ListaResponse {
    val chavesResponse = dados.map { chave ->
        ListaResponse.Chave.newBuilder()
            .setPixId(chave.pixId)
            .setClienteId(chave.clienteId)
            .setTipoChave(chave.tipoChave)
            .setChave(chave.valor)
            .setTipoConta(chave.tipoConta)
            .setCriadaEm(convertToTimestamp(chave.criadaEm))
            .build()
    }

    return ListaResponse.newBuilder()
        .addAllChaves(chavesResponse)
        .build()
}

private fun convertToConsultaChaveResponse(dados: DetalhesDadosChave): ConsultaResponse {
    return ConsultaResponse.newBuilder()
        .setPixId(dados.pixId)
        .setClienteId(dados.clienteId)
        .setChave(toChaveResponse(dados))
        .build()
}

private fun toChaveResponse(dados: DetalhesDadosChave) : ConsultaResponse.Chave {
    return ConsultaResponse.Chave.newBuilder()
        .setTipo(dados.tipoChave.name)
        .setChave(dados.chave)
        .setCriadaEm(convertToTimestamp(dados.criadaEm))
        .setConta(toContaResponse(dados))
        .build()
}

private fun toContaResponse(conta: DetalhesDadosChave): ConsultaResponse.Chave.Conta {
    return ConsultaResponse.Chave.Conta.newBuilder()
        .setTipo(conta.tipoConta.name)
        .setInstituicao(conta.instituicao)
        .setNomeTitular(conta.nomeTitular)
        .setCpfTitular(conta.cpfTitular)
        .setAgencia(conta.agencia)
        .setNumeroConta(conta.numeroConta)
        .build()
}

private fun convertToTimestamp(date: LocalDateTime): Timestamp {
    val instant = date.toInstant(ZoneOffset.UTC)

    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}