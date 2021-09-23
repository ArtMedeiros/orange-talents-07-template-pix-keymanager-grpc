package br.com.zup.edu.utils.services

import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.ConsultaRequest
import br.com.zup.edu.ConsultaRequest.FiltroCase.*
import br.com.zup.edu.ConsultaResponse
import br.com.zup.edu.RemoverRequest
import br.com.zup.edu.TipoChave.INVALID
import br.com.zup.edu.TipoConta.UNKNOW
import br.com.zup.edu.chaves.*
import br.com.zup.edu.chaves.dto.RegistrarChaveRequest
import br.com.zup.edu.chaves.dto.ConsultarChaveRequest
import br.com.zup.edu.chaves.dto.RemoverChaveRequest
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.validation.ConstraintViolationException

fun ChaveRequest.toModel(): RegistrarChaveRequest {
    return RegistrarChaveRequest(
        cliente = cliente,
        tipo = when (tipoChave) {
            INVALID -> null
            else -> TipoChaveEntity.valueOf(tipoChave.name)
        },
        chave = chave,
        tipoConta = when (conta) {
            UNKNOW -> null
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

fun convertToConsultaChaveResponse(dados: ChaveEntity, request: ConsultarChaveRequest): ConsultaResponse {
    return ConsultaResponse.newBuilder()
        .setPixId(request.pixId ?: 0)
        .setClienteId(request.clienteId ?: "")
        .setChave(toChaveResponse(dados))
        .build()
}

fun toChaveResponse(dados: ChaveEntity) : ConsultaResponse.Chave {
    return ConsultaResponse.Chave.newBuilder()
        .setTipo(dados.tipo.name)
        .setChave(dados.valor)
        .setCriadaEm(convertToTimestamp(dados.criadaEm))
        .setConta(toContaResponse(dados))
        .build()
}

fun toContaResponse(dados: ChaveEntity): ConsultaResponse.Chave.Conta {
    return ConsultaResponse.Chave.Conta.newBuilder()
        .setTipo(dados.tipo.name)
        .setInstituicao(dados.instituicao().nomeInstituicao)
        .setNomeTitular(dados.titular().nomeCliente)
        .setCpfTitular(dados.titular().cpfCliente)
        .setAgencia(dados.conta.agencia)
        .setNumeroConta(dados.conta.numero)
        .build()
}

fun convertToTimestamp(date: LocalDateTime): Timestamp {
    val instant = date.toInstant(ZoneOffset.UTC)

    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}