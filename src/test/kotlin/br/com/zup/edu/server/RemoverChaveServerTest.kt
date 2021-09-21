package br.com.zup.edu.server

import br.com.zup.edu.ChavePixServiceGrpc
import br.com.zup.edu.RemoverRequest
import br.com.zup.edu.chaves.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class RemoverChaveServerTest(
    private val repository: ChavePixRepository,
    private val grpcClient: ChavePixServiceGrpc.ChavePixServiceBlockingStub
) {

    val chave: ChaveEntity = ChaveEntity(
        idCliente = "de95a228-1f27-4ad2-907e-e5a2d816e9bc",
        tipo = TipoChaveEntity.CPF,
        valor = "31643468081",
        tipoConta = TipoContaEntity.CONTA_CORRENTE
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
}