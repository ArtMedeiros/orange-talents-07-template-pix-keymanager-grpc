package br.com.zup.edu.utils.services

import br.com.zup.edu.ConsultaResponse
import br.com.zup.edu.ListaResponse
import br.com.zup.edu.chaves.*
import br.com.zup.edu.chaves.dto.*
import br.com.zup.edu.utils.error.ChaveDuplicadaException
import br.com.zup.edu.utils.error.ChaveNaoEncontradaException
import br.com.zup.edu.utils.error.ClienteNaoEncontradoException
import br.com.zup.edu.utils.services.bcb.BcbClient
import br.com.zup.edu.utils.services.bcb.TipoUsuarioBCB
import br.com.zup.edu.utils.services.bcb.dto.*
import br.com.zup.edu.utils.services.itau.ErpItauClient
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.validation.Valid

@Singleton
open class ChavePixService(
    val repository: ChavePixRepository,
    val erpCliet: ErpItauClient,
    val bcbClient: BcbClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    open fun registra(@Valid novaChave: RegistrarChaveRequest): ChaveEntity {
        logger.info("Verificando chave")

        if (repository.existsByValor(novaChave.chave)) {
            throw ChaveDuplicadaException("Chave já cadastrada")
        }

        logger.info("Validando dados")
        val cliente = erpCliet.buscarCliente(novaChave.cliente, novaChave.tipoConta)
            ?: throw ClienteNaoEncontradoException("Cliente não encontrado")

        novaChave.dadosConta(cliente)

        val bcbRequest = CreatePixKeyRequest(
            keyType = novaChave.tipo.converterBcb(),
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
        novaChave.dataCriacao(bcbResponse.createdAt)

        if (novaChave.tipo == TipoChaveEntity.RANDOM) {
            novaChave.chaveRandom(bcbResponse.key)
        }

        val chave = novaChave.toChaveEntity()
        repository.save(chave)

        return chave
    }

    open fun remove(@Valid removerChave: RemoverChaveRequest): ChaveEntity {

        logger.info("Verificando chave")
        val chaveBanco = repository.findByIdAndIdCliente(removerChave.id, removerChave.cliente)
        if (chaveBanco.isEmpty)
            throw ChaveNaoEncontradaException("Chave não encontrada")

        val chave = chaveBanco.get()

        val bcbRequest = DeletePixKeyRequest(
            key = chave.valor,
            participant = chave.instituicao().ispb
        )

        logger.info("Desvinculando chave")
        val bcbResponse = bcbClient.removerChave(chave.valor, bcbRequest)
        if (bcbResponse.status != HttpStatus.OK)
            throw ChaveNaoEncontradaException("Chave não encontrada")

        repository.delete(chave)

        return chave
    }

    open fun consulta(consultaChave: ConsultarChaveRequest): ConsultaResponse {
        return if (consultaChave.chave.isBlank()) {
            val chaveBanco = repository.findByIdAndIdCliente(consultaChave.pixId, consultaChave.clienteId)
            if (chaveBanco.isEmpty)
                throw ChaveNaoEncontradaException("Chave não encontrada")

            DetalhesDadosChave.toConsultaResponse(chaveBanco.get())
        } else {
            val chaveBanco = repository.findByValor(consultaChave.chave)
            if (chaveBanco.isPresent) {
                DetalhesDadosChave.toConsultaResponse(chaveBanco.get())
            } else {
                val pixDetails = bcbClient.buscarChaveByValor(consultaChave.chave)
                    ?: throw ChaveNaoEncontradaException("Chave não encontrada")

                DetalhesDadosChave.toConsultaResponse(pixDetails)
            }
        }
    }

    fun listar(clienteId: String): ListaResponse {
        if(clienteId.isBlank())
            throw ConstraintViolationException("Id do cliente não informado", setOf())

        val lista = repository.findAllByIdCliente(clienteId)
        if (lista.isEmpty())
            throw ChaveNaoEncontradaException("Nenhuma chave encontrada")

        return ListaDeChavesResponse.criarLista(lista)
    }
}
