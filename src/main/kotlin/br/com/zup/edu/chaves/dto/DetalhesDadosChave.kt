package br.com.zup.edu.chaves.dto

import br.com.zup.edu.ConsultaResponse
import br.com.zup.edu.chaves.ChaveEntity
import br.com.zup.edu.chaves.Instituicao
import br.com.zup.edu.chaves.TipoChaveEntity
import br.com.zup.edu.chaves.TipoContaEntity
import br.com.zup.edu.utils.services.bcb.TipoChaveBCB
import br.com.zup.edu.utils.services.bcb.dto.PixKeyDetailsResponse
import br.com.zup.edu.utils.services.toConsultaResponse
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class DetalhesDadosChave(
    @field:NotBlank
    var pixId: Long,

    @field:NotBlank
    val clienteId: String,

    @field:NotNull
    val tipoChave: TipoChaveBCB,

    @field:NotBlank
    val chave: String,

    @field:NotNull
    val tipoConta: TipoContaEntity,

    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeTitular: String,

    @field:NotBlank
    val cpfTitular: String,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numeroConta: String,

    @field:NotNull
    val criadaEm: LocalDateTime
){
    companion object {
        fun toConsultaResponse(chave: ChaveEntity) : ConsultaResponse {
            val detalhes = DetalhesDadosChave(
                pixId = chave.id!!,
                clienteId = chave.idCliente,
                tipoChave = chave.tipo.converterBcb(),
                chave = chave.valor,
                tipoConta = chave.conta.tipoConta,
                instituicao = chave.instituicao().nomeInstituicao,
                nomeTitular = chave.titular().nomeCliente,
                cpfTitular = chave.titular().cpfCliente,
                agencia = chave.conta.agencia,
                numeroConta = chave.conta.numero,
                criadaEm = chave.criadaEm
            )

            return toConsultaResponse(detalhes)
        }

        fun toConsultaResponse(dados: PixKeyDetailsResponse): ConsultaResponse {
            val detalhes = DetalhesDadosChave(
                pixId = 0,
                clienteId = "",
                tipoChave = dados.keyType,
                chave = dados.key,
                tipoConta = dados.bankAccount.tipoContaEntity(),
                instituicao = Instituicao.nomeByIspb(dados.bankAccount.participant),
                nomeTitular = dados.owner.name,
                cpfTitular = dados.owner.taxIdNumber,
                agencia = dados.bankAccount.branch,
                numeroConta = dados.bankAccount.accountNumber,
                criadaEm = dados.createdAt
            )

            return toConsultaResponse(detalhes)
        }
    }
}