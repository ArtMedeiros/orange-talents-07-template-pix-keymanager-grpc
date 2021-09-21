package br.com.zup.edu.server

import br.com.zup.edu.ChavePixServiceGrpc
import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chaves.ChaveEntity
import br.com.zup.edu.chaves.ChavePixRepository
import br.com.zup.edu.chaves.TipoChaveEntity
import br.com.zup.edu.chaves.TipoContaEntity
import br.com.zup.edu.utils.services.itau.ContaItauResponse
import br.com.zup.edu.utils.services.itau.ErpItauClient
import br.com.zup.edu.utils.services.itau.InstituicaoResponse
import br.com.zup.edu.utils.services.itau.TitularConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(
    rollback = false,
    transactional = false,
    transactionMode = TransactionMode.SEPARATE_TRANSACTIONS
)
internal class RegistrarChaveGRPCServerTest(
    val clienteERP: ErpItauClient,
    val repository: ChavePixRepository,
    val grpcClient: ChavePixServiceGrpc.ChavePixServiceBlockingStub
) {

    lateinit var contaItauResponse: ContaItauResponse

    @BeforeEach
    internal fun setUp() {
        val instituicaoResponse = InstituicaoResponse(
            nome = "ITAÚ UNIBANCO S.A.",
            ispb = "60701190"
        )
        val titularConta = TitularConta(
            id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            nome = "Rafael M C Ponte",
            cpf = "02467781054"
        )
        contaItauResponse = ContaItauResponse(
            tipo = TipoContaEntity.CONTA_CORRENTE,
            instituicao = instituicaoResponse,
            agencia = "0001",
            numero = "291900",
            titular = titularConta
        )
        repository.deleteAll()
    }

    @Test
    internal fun `deve cadastrar uma nova chave pix`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("00000000000")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                tipo = TipoContaEntity.CONTA_CORRENTE
            )
        ).thenReturn(contaItauResponse)

        val response = grpcClient.gerarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsById(pixId))
        }
    }

    @Test
    internal fun `deve cadastrar chave pix aleatoria`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.RANDOM)
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                tipo = TipoContaEntity.CONTA_CORRENTE
            )
        ).thenReturn(contaItauResponse)

        val response = grpcClient.gerarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsById(pixId))
        }
    }

    @Test
    internal fun `nao deve cadastrar chave com cliente nao cadastrado`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-111111111111")
            .setTipoChave(TipoChave.CPF)
            .setChave("00000000000")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2-111111111111",
                tipo = TipoContaEntity.CONTA_CORRENTE
            )
        ).thenReturn(null)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.gerarChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar com chave invalida`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("ABC")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                tipo = TipoContaEntity.CONTA_CORRENTE
            )
        ).thenReturn(contaItauResponse)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.gerarChave(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(this.status.description!!.endsWith("Chave inválida"))
        }
    }

    @Test
    internal fun `nao deve cadastrar chave duplicada`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("00000000000")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        val chave = ChaveEntity(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipo = TipoChaveEntity.CPF,
            valor = "00000000000",
            TipoContaEntity.CONTA_CORRENTE
        )

        repository.save(chave)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.gerarChave(request)
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave já cadastrada", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar caso erp indisponivel`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("00000000000")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                tipo = TipoContaEntity.CONTA_CORRENTE
            )
        ).thenThrow(HttpClientException::class.java)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.gerarChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar caso cliente invalido`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2")
            .setTipoChave(TipoChave.CPF)
            .setChave("00000000000")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2",
                tipo = TipoContaEntity.CONTA_CORRENTE
            )
        ).thenThrow(HttpClientResponseException::class.java)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.gerarChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }
    }

    @MockBean(ErpItauClient::class)
    fun erpMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @Factory
    class ClientsTest {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ChavePixServiceGrpc.ChavePixServiceBlockingStub? {
            return ChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}