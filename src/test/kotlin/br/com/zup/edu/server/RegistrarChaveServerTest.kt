package br.com.zup.edu.server

import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.RegistrarChaveServiceGrpc
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chaves.*
import br.com.zup.edu.utils.services.bcb.BcbClient
import br.com.zup.edu.utils.services.bcb.TipoChaveBCB
import br.com.zup.edu.utils.services.bcb.TipoContaBCB
import br.com.zup.edu.utils.services.bcb.TipoUsuarioBCB
import br.com.zup.edu.utils.services.bcb.dto.BankAccountRequest
import br.com.zup.edu.utils.services.bcb.dto.CreatePixKeyRequest
import br.com.zup.edu.utils.services.bcb.dto.CreatePixKeyResponse
import br.com.zup.edu.utils.services.bcb.dto.OwnerRequest
import br.com.zup.edu.utils.services.itau.ErpItauClient
import br.com.zup.edu.utils.services.itau.dto.ContaItauResponse
import br.com.zup.edu.utils.services.itau.dto.InstituicaoResponse
import br.com.zup.edu.utils.services.itau.dto.TitularConta
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
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.mockito.internal.matchers.InstanceOf
import org.mockito.internal.progress.ThreadSafeMockingProgress
import java.time.LocalDateTime

@MicronautTest(
    transactional = false
)
internal class RegistrarChaveServerTest(
    val clienteERP: ErpItauClient,
    val bcbClient: BcbClient,
    val repository: ChavePixRepository,
    val grpcClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub
) {
    lateinit var contaItauResponse: ContaItauResponse
    lateinit var contaItauResponsePoupanca: ContaItauResponse
    lateinit var bcbRequest: CreatePixKeyRequest
    lateinit var bcbRequestPoupanca: CreatePixKeyRequest
    lateinit var bcbResponse: CreatePixKeyResponse

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

        contaItauResponsePoupanca = ContaItauResponse(
            tipo = TipoContaEntity.CONTA_POUPANCA,
            instituicao = instituicaoResponse,
            agencia = "0001",
            numero = "291900",
            titular = titularConta
        )

        bcbRequest = CreatePixKeyRequest(
            keyType = TipoChaveBCB.CPF,
            key = "00000000000",
            bankAccount = BankAccountRequest(
                participant = instituicaoResponse.ispb,
                branch = contaItauResponse.agencia,
                accountNumber = contaItauResponse.numero,
                accountType = contaItauResponse.tipo.converterBcb()
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = titularConta.nome,
                taxIdNumber = titularConta.cpf
            )
        )

        bcbRequestPoupanca = CreatePixKeyRequest(
            keyType = TipoChaveBCB.EMAIL,
            key = "testmail@mail.com",
            bankAccount = BankAccountRequest(
                participant = instituicaoResponse.ispb,
                branch = contaItauResponse.agencia,
                accountNumber = contaItauResponse.numero,
                accountType = contaItauResponsePoupanca.tipo.converterBcb()
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = titularConta.nome,
                taxIdNumber = titularConta.cpf
            )
        )

        bcbResponse = CreatePixKeyResponse(
            keyType = TipoChaveBCB.CPF,
            key = "00000000000",
            bankAccount = BankAccountRequest(
                participant = instituicaoResponse.ispb,
                branch = contaItauResponse.agencia,
                accountNumber = contaItauResponse.numero,
                accountType = contaItauResponse.tipo.converterBcb()
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = titularConta.nome,
                taxIdNumber = titularConta.cpf
            ),
            createdAt = LocalDateTime.now()
        )

        repository.deleteAll()
    }

    @Test
    internal fun `deve cadastrar uma nova chave pix`() {
        val request = ChaveRequest.newBuilder()
            .setCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.EMAIL)
            .setChave("testmail@mail.com")
            .setConta(TipoConta.CONTA_POUPANCA)
            .build()

        Mockito.`when`(
            clienteERP.buscarCliente(
                clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                tipo = TipoContaEntity.CONTA_POUPANCA
            )
        ).thenReturn(contaItauResponsePoupanca)

        Mockito.`when`(
            bcbClient.cadastrarChave(bcbRequestPoupanca)
        ).thenReturn(bcbResponse)

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
            .setTipoChave(TipoChave.TELEFONE)
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

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
            .setTipoChave(TipoChave.TELEFONE)
            .setChave("+559999999999")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        val chave = ChaveEntity(
            idCliente = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipo = TipoChaveEntity.TELEFONE,
            valor = "+559999999999",
            conta = contaItauResponse.toModel(),
            criadaEm = LocalDateTime.now()
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
            assertEquals(Status.UNAVAILABLE.code, status.code)
            assertEquals("Serviço temporariamente indisponível", status.description)
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
            assertEquals("Recurso não encontrado", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar caso bcb indisponivel`() {
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
        ).thenReturn(contaItauResponse)

        Mockito.`when`(
            bcbClient.cadastrarChave(bcbRequest)
        ).thenThrow(HttpClientException::class.java)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.gerarChave(request)
        }

        with(error) {
            assertEquals(Status.UNAVAILABLE.code, status.code)
            assertEquals("Serviço temporariamente indisponível", status.description)
        }
    }

    @Test
    internal fun `deve cadastrar chave aleatoria`() {
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

        val bcbReq = CreatePixKeyRequest(
            keyType = TipoChaveBCB.RANDOM,
            key = TipoChaveEntity.RANDOM.name,
            bankAccount = BankAccountRequest(
                participant = contaItauResponse.instituicao.ispb,
                branch = contaItauResponse.agencia,
                accountNumber = contaItauResponse.numero,
                accountType = contaItauResponse.tipo.converterBcb()
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = contaItauResponse.titular.nome,
                taxIdNumber = contaItauResponse.titular.cpf
            )
        )

        val bcbResp = CreatePixKeyResponse(
            keyType = TipoChaveBCB.RANDOM,
            key = "aa52cd23-f52b-4c01-b60b-9950f3ac3711",
            bankAccount = BankAccountRequest(
                participant = contaItauResponse.instituicao.ispb,
                branch = contaItauResponse.agencia,
                accountNumber = contaItauResponse.numero,
                accountType = contaItauResponse.tipo.converterBcb()
            ),
            owner = OwnerRequest(
                type = TipoUsuarioBCB.NATURAL_PERSON,
                name = contaItauResponse.titular.nome,
                taxIdNumber = contaItauResponse.titular.cpf
            ),
            createdAt = LocalDateTime.now()
        )

        Mockito.`when`(
            bcbClient.cadastrarChave(bcbReq)
        ).thenReturn(bcbResp)

        val response = grpcClient.gerarChave(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsById(pixId))
        }
    }

    @MockBean(ErpItauClient::class)
    fun erpMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class ClientsTest {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub? {
            return RegistrarChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}