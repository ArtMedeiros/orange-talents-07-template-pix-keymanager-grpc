package br.com.zup.edu.server

import br.com.zup.edu.ListaRequest
import br.com.zup.edu.ListarChaveServiceGrpc
import br.com.zup.edu.chaves.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(
    transactional = false
)
internal class ListarChaveServerTest(
    val grpcClient: ListarChaveServiceGrpc.ListarChaveServiceBlockingStub,
    val repository: ChavePixRepository
){

    lateinit var listaEntity: List<ChaveEntity>

    @BeforeEach
    internal fun setUp() {
        val conta1 = Conta(
            tipoConta = TipoContaEntity.CONTA_CORRENTE,
            instituicao = Instituicao(
                nomeInstituicao = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "1234",
            titular = Titular(
                nomeCliente = "Tester Lista",
                cpfCliente = "12345678910"
            )
        )

        val conta2 = Conta(
            tipoConta = TipoContaEntity.CONTA_POUPANCA,
            instituicao = Instituicao(
                nomeInstituicao = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "55512",
            titular = Titular(
                nomeCliente = "Tester Lista",
                cpfCliente = "12345678910"
            )
        )

        val entity1 = ChaveEntity(
            idCliente = "123456",
            tipo = TipoChaveEntity.EMAIL,
            valor = "mail@mail.com",
            conta = conta1,
            criadaEm = LocalDateTime.now()
        )

        val entity2 = ChaveEntity(
            idCliente = "123456",
            tipo = TipoChaveEntity.RANDOM,
            valor = UUID.randomUUID().toString(),
            conta = conta1,
            criadaEm = LocalDateTime.now()
        )

        val entity3 = ChaveEntity(
            idCliente = "123456",
            tipo = TipoChaveEntity.RANDOM,
            valor = UUID.randomUUID().toString(),
            conta = conta2,
            criadaEm = LocalDateTime.now()
        )

        listaEntity = listOf(entity1, entity2, entity3)
        repository.deleteAll()
        repository.saveAll(listaEntity)
    }

    @Test
    internal fun `deve retornar todas as chaves do cliente`() {
        val request = ListaRequest.newBuilder()
            .setClienteId("123456")
            .build()

        val response = grpcClient.listarChave(request)

        with(response) {
            assertEquals(listaEntity.size, response.chavesCount)
        }
    }

    @Test
    internal fun `nao deve retornar dado para request invalida`() {
        val request = ListaRequest.newBuilder()
            .build()


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.listarChave(request)
        }


        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertTrue(status.description!!.contains("Id do cliente não informado"))
        }
    }

    @Test
    internal fun `nao deve retornar dados para cliente inexistente`() {
        val request = ListaRequest.newBuilder()
            .setClienteId("00000")
            .build()


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.listarChave(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Nenhuma chave encontrada", status.description)
        }
    }

    @Factory
    class ClientsTest {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListarChaveServiceGrpc.ListarChaveServiceBlockingStub? {
            return ListarChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}