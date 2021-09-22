package br.com.zup.edu.utils.services

import br.com.zup.edu.RemoverRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chaves.*
import br.com.zup.edu.utils.error.ChaveDuplicadaException
import br.com.zup.edu.utils.error.ChaveNaoEncontradaException
import br.com.zup.edu.utils.services.bcb.BcbClient
import br.com.zup.edu.utils.services.bcb.TipoUsuarioBCB
import br.com.zup.edu.utils.services.bcb.dto.BankAccountRequest
import br.com.zup.edu.utils.services.bcb.dto.CreatePixKeyRequest
import br.com.zup.edu.utils.services.bcb.dto.DeletePixKeyRequest
import br.com.zup.edu.utils.services.bcb.dto.OwnerRequest
import br.com.zup.edu.utils.services.itau.ErpItauClient
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.transaction.Status
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
open class ChavePixService(
    val repository: ChavePixRepository,
    val erpCliet: ErpItauClient,
    val bcbClient: BcbClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    open fun registra(@Valid novaChave: ChaveGRPCRequest): ChaveEntity {
        if (repository.existsByValor(novaChave.chave)) {
            throw ChaveDuplicadaException("Chave já cadastrada")
        }

        val cliente = erpCliet.buscarCliente(novaChave.cliente, novaChave.tipoConta!!)
            ?: throw HttpClientException("Cliente não encontrado")

        novaChave.atualizarBanco(cliente.instituicao.ispb)

        val bcbRequest = CreatePixKeyRequest(
            keyType = novaChave.tipo!!.converterBcb(),
            key = novaChave.valor,
            bankAccount = BankAccountRequest(
                participant = cliente.instituicao.ispb,
                branch = cliente.agencia,
                accountNumber = cliente.numero,
                accountType = cliente.tipo.converterBcb()
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = cliente.titular.nome,
                taxIdNumber = cliente.titular.cpf
            )
        )

        logger.info("Registrando chave")
        val bcbResponse = bcbClient.cadastrarChave(bcbRequest) ?: throw ChaveDuplicadaException("Chave já cadastrada")

        if(novaChave.tipo == TipoChaveEntity.RANDOM){
            novaChave.chaveRandom(bcbResponse.key)
        }

        val chave = novaChave.toChaveEntity()
        repository.save(chave)

        return chave
    }

    @Transactional
    open fun remove(@Valid removerChave: RemoverChaveRequest) : ChaveEntity {

        logger.info("Verificando chave")
        val chaveBanco = repository.findByIdAndIdCliente(removerChave.id, removerChave.cliente)
        if (chaveBanco.isEmpty)
            throw ChaveNaoEncontradaException("Chave não encontrada")

        val chave = chaveBanco.get()

        val bcbRequest = DeletePixKeyRequest(
            key = chave.valor,
            participant = chave.banco
        )

        logger.info("Desvinculando chave")
        val bcbResponse = bcbClient.removerChave(chave.valor, bcbRequest)
        if (bcbResponse.status != HttpStatus.OK)
            throw ChaveNaoEncontradaException("Chave não encontrada")

        repository.delete(chave)

        return chave
    }

}