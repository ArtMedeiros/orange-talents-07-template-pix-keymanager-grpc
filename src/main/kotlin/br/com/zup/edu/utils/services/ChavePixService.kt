package br.com.zup.edu.utils.services

import br.com.zup.edu.chaves.ChaveEntity
import br.com.zup.edu.chaves.ChaveGRPCRequest
import br.com.zup.edu.chaves.ChavePixRepository
import br.com.zup.edu.utils.error.ChaveDuplicadaException
import br.com.zup.edu.utils.services.itau.ErpItauClient
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
open class ChavePixService(
    val repository: ChavePixRepository,
    val erpCliet: ErpItauClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    open fun registra(@Valid novaChave: ChaveGRPCRequest) : ChaveEntity {
        if (repository.existsByValor(novaChave.chave)) {
            throw ChaveDuplicadaException("Chave já cadastrada")
        }

        erpCliet.buscarCliente(novaChave.cliente, novaChave.tipoConta!!)
            ?: throw HttpClientException("Clientão não encontrado")

        val chave = novaChave.toChaveEntity()
        repository.save(chave)

        return chave
    }

}