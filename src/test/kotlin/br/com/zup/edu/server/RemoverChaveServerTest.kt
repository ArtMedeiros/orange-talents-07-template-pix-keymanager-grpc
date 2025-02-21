package br.com.zup.edu.server

import br.com.zup.edu.RemoverChaveServiceGrpc
import br.com.zup.edu.RemoverRequest
import br.com.zup.edu.chaves.ChaveEntity
import br.com.zup.edu.chaves.ChavePixRepository
import br.com.zup.edu.chaves.TipoChaveEntity
import br.com.zup.edu.chaves.TipoContaEntity
import br.com.zup.edu.utils.error.ChaveNaoEncontradaException
import br.com.zup.edu.utils.services.bcb.BcbClient
import br.com.zup.edu.utils.services.bcb.dto.DeletePixKeyRequest
import br.com.zup.edu.utils.services.itau.dto.ContaItauResponse
import br.com.zup.edu.utils.services.itau.dto.InstituicaoResponse
import br.com.zup.edu.utils.services.itau.dto.TitularConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class RemoverChaveServerTest(
    val bcbClient: BcbClient,
    private val repository: ChavePixRepository,
    private val grpcClient: RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub
) {

    val instituicaoResponse = InstituicaoResponse(
        nome = "ITAÚ UNIBANCO S.A.",
        ispb = "60701190"
    )

    val titularConta = TitularConta(
        id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
        nome = "Rafael M C Ponte",
        cpf = "02467781054"
    )

    val contaItauResponse = ContaItauResponse(
        tipo = TipoContaEntity.CONTA_CORRENTE,
        instituicao = instituicaoResponse,
        agencia = "0001",
        numero = "291900",
        titular = titularConta
    )

    val chave: ChaveEntity = ChaveEntity(
        idCliente = "de95a228-1f27-4ad2-907e-e5a2d816e9bc",
        tipo = TipoChaveEntity.CPF,
        valor = "31643468081",
        conta = contaItauResponse.toModel(),
        criadaEm = LocalDateTime.now()
    )

    val deleteKey = DeletePixKeyRequest(
        key = chave.valor,
        participant = chave.instituicao().ispb
    )

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve remover uma chave`() {
        val obj = repository.save(chave)

        val request = RemoverRequest
            .newBuilder()
            .setCliente("de95a228-1f27-4ad2-907e-e5a2d816e9bc")
            .setPixId(obj.id!!)
            .build()

        Mockito.`when`(bcbClient.removerChave(chave.valor, deleteKey)).thenReturn(HttpResponse.ok())

        val response = grpcClient.removerChave(request)

        with(response) {
            assertEquals("OK", status)
            assertFalse(repository.existsById(obj.id!!))
        }
    }

    @Test
    internal fun `nao deve remover uma chave com atributos invalidos`() {
        repository.save(chave)

        val request = RemoverRequest
            .newBuilder()
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(repository.count() > 0)
        }
    }

    @Test
    internal fun `nao deve remover uma chave que nao eh do cliente`() {
        val obj = repository.save(chave)

        val request = RemoverRequest
            .newBuilder()
            .setCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setPixId(obj.id!!)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
            assertTrue(repository.count() > 0)
        }
    }

    @Test
    internal fun `nao deve remover uma chave inexistente`() {
        val obj = repository.save(chave)

        val request = RemoverRequest
            .newBuilder()
            .setCliente("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setPixId(obj.id!!)
            .build()

        Mockito.`when`(bcbClient.removerChave(chave.valor, deleteKey)).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
            assertTrue(repository.count() > 0)
        }
    }

    @Test
    internal fun `nao deve remover uma chave se bcb offline`() {
        val obj = repository.save(chave)

        val request = RemoverRequest
            .newBuilder()
            .setCliente("de95a228-1f27-4ad2-907e-e5a2d816e9bc")
            .setPixId(obj.id!!)
            .build()

        Mockito.`when`(bcbClient.removerChave(chave.valor, deleteKey)).thenThrow(HttpClientException::class.java)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(error) {
            assertEquals(Status.UNAVAILABLE.code, status.code)
            assertEquals("Serviço temporariamente indisponível", status.description)
            assertTrue(repository.count() > 0)
        }
    }

    @Test
    internal fun `nao remover chave que nao existe no bcb`() {
        val obj = repository.save(chave)

        val request = RemoverRequest
            .newBuilder()
            .setCliente(chave.idCliente)
            .setPixId(obj.id!!)
            .build()

        Mockito.`when`(bcbClient.removerChave(chave.valor, deleteKey)).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
            assertTrue(repository.count() > 0)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class ClientsTest {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub? {
            return RemoverChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}