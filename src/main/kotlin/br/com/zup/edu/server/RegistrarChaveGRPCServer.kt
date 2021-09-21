package br.com.zup.edu.server

import br.com.zup.edu.ChavePixServiceGrpc
import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.ChaveResponse
import br.com.zup.edu.chaves.ChaveGRPCRequest
import br.com.zup.edu.chaves.ChavePixRepository
import br.com.zup.edu.externo.itau.ErpItauClient
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.validator.Validator
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Singleton
class RegistrarChaveGRPCServer(
    val validator: Validator,
    val repository: ChavePixRepository,
    val clienteERP: ErpItauClient
) : ChavePixServiceGrpc.ChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun gerarChave(request: ChaveRequest, responseObserver: StreamObserver<ChaveResponse>) {
        logger.info("Gerando chave...")

        try {
            val chaveRequest = ChaveGRPCRequest(
                cliente = request.cliente,
                tipo = request.tipoChave,
                chave = request.chave,
                tipoConta = request.conta
            )
            validator.validate(chaveRequest).let {
                if (it.isNotEmpty())
                    throw ConstraintViolationException(it)
            }

            if (repository.existsByValor(chaveRequest.chave)) {
                responseObserver.onError(
                    Status.ALREADY_EXISTS
                        .withDescription("Chave já cadastrada")
                        .asRuntimeException()
                )

                logger.error("Chave já cadastrada")
                return
            }

            val chave = chaveRequest.toChaveEntity()

            val cliente = clienteERP.buscarCliente(chave.idCliente, chave.tipoConta)
            if (cliente == null) {
                responseObserver.onError(
                    Status.NOT_FOUND
                        .withDescription("Cliente não encontrado")
                        .asRuntimeException()
                )

                logger.error("Cliente não encontrado")
                return
            }

            repository.save(chave)

            val response = ChaveResponse.newBuilder()
                .setPixId(chave.id!!)
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
            logger.info("Chave pix cadastrada com sucesso!")

        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )

            logger.error(e.message)
        } catch (e: HttpClientResponseException) {
            responseObserver.onError(
                Status.UNAVAILABLE
                    .withDescription("Serviço indisponível")
                    .asRuntimeException()
            )

            logger.error("Serviço indisponível")
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Falha ao processar requisição")
                    .asRuntimeException()
            )

            logger.error(e.cause.toString())
        }
    }

}