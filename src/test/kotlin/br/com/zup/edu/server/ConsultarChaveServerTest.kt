package br.com.zup.edu.server

import br.com.zup.edu.ConsultaRequest
import br.com.zup.edu.ConsultarChaveServiceGrpc
import br.com.zup.edu.chaves.*
import br.com.zup.edu.utils.services.bcb.BcbClient
import br.com.zup.edu.utils.services.bcb.TipoChaveBCB
import br.com.zup.edu.utils.services.bcb.TipoContaBCB
import br.com.zup.edu.utils.services.bcb.TipoUsuarioBCB
import br.com.zup.edu.utils.services.bcb.dto.BankAccountRequest
import br.com.zup.edu.utils.services.bcb.dto.OwnerRequest
import br.com.zup.edu.utils.services.bcb.dto.PixKeyDetailsResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import br.com.zup.edu.chaves.Conta as ContaEntity

@MicronautTest(transactional = false)
internal class ConsultarChaveServerTest(
    val bcbClient: BcbClient,
    val repository: ChavePixRepository,
    val grpcClient: ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub
) {

    lateinit var chaveEntity: ChaveEntity
    lateinit var bcbResponse: PixKeyDetailsResponse

    @BeforeEach
    internal fun setUp() {
        val conta = ContaEntity(
            tipoConta = TipoContaEntity.CONTA_CORRENTE,
            instituicao = Instituicao(
                nomeInstituicao = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "123455",
            titular = Titular(
                nomeCliente = "Yuri Matheus",
                cpfCliente = "86135457004"
            )
        )

        chaveEntity = ChaveEntity(
            idCliente = "5260263c-a3c1-4727-ae32-3bdb2538841b",
            tipo = TipoChaveEntity.CPF,
            valor = "00000000000",
            conta = conta,
            criadaEm = LocalDateTime.now()
        )

        bcbResponse = PixKeyDetailsResponse(
            keyType = TipoChaveBCB.PHONE,
            key = "12345678901234",
            bankAccount = BankAccountRequest(
                participant = "00000000",
                branch = "1234",
                accountNumber = "1234",
                accountType = TipoContaBCB.CACC
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = "Teste",
                taxIdNumber = "1234"
            ),
            createdAt = LocalDateTime.now()
        )

        repository.deleteAll()
    }

    @Test
    internal fun `deve retornar os dados da chave bcb pelo valor`() {
        val request = ConsultaRequest.newBuilder()
            .setChave("12345678901234")
            .build()

        Mockito.`when`(
            bcbClient.buscarChaveByValor(request.chave)
        ).thenReturn(bcbResponse)

        val response = grpcClient.consultarChave(request)

        with(response) {
            assertTrue(clienteId.isBlank())
            assertEquals(bcbResponse.owner.taxIdNumber, chave.conta.cpfTitular)
        }
    }

    @Test
    internal fun `deve retornar os dados da chave local pelo valor`() {
        val obj = repository.save(chaveEntity)

        val request = ConsultaRequest.newBuilder()
            .setChave("00000000000")
            .build()

        Mockito.`when`(
            bcbClient.buscarChaveByValor(request.chave)
        ).thenReturn(bcbResponse)

        val response = grpcClient.consultarChave(request)

        with(response) {
            assertTrue(clienteId.isNotBlank())
            assertEquals(obj.titular().cpfCliente, chave.conta.cpfTitular)
        }
    }

    @Test
    internal fun `deve retornar os dados da chave pelo id e cliente`() {
        val obj = repository.save(chaveEntity)

        val pixId = ConsultaRequest.ConsultaInterna.newBuilder()
            .setPixId(obj.id!!)
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .build()

        val request = ConsultaRequest.newBuilder()
            .setPixId(pixId)
            .build()

        val response = grpcClient.consultarChave(request)

        with(response) {
            assertEquals(chaveEntity.idCliente ,clienteId)
            assertEquals(chaveEntity.conta.titular.cpfCliente, chave.conta.cpfTitular)
        }
    }

    @Test
    internal fun `nao deve retornar os dados da chave vazia`() {
        val request = ConsultaRequest.newBuilder().build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description!!.contains("Request inválida"))
        }
    }

    @Test
    internal fun `nao deve retornar os dados da chave invalida`() {
        val request = ConsultaRequest.newBuilder()
            .setChave("00000000000")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
        }
    }

    @Test
    internal fun `nao deve retornar os dados de uma chave inexistente`() {
        val pixId = ConsultaRequest.ConsultaInterna.newBuilder()
            .setPixId(1)
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .build()

        val request = ConsultaRequest.newBuilder()
            .setPixId(pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
        }
    }

    @Test
    internal fun `verificar handler de erro geral`() {
        val request = ConsultaRequest.newBuilder()
            .setChave("00000000000")
            .build()

        Mockito.`when`(bcbClient.buscarChaveByValor("00000000000")).thenThrow(RuntimeException("Num vai da não"))

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultarChave(request)
        }

        with(error) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertTrue(status.description!!.contains("Não foi possível processar a requisição"))
        }
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class ClientsTest {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub? {
            return ConsultarChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}