package br.com.zup.edu.server

import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.ChaveResponse
import br.com.zup.edu.RegistrarChaveServiceGrpc
import br.com.zup.edu.utils.error.ChaveDuplicadaException
import br.com.zup.edu.utils.services.ChavePixService
import br.com.zup.edu.utils.services.toModel
import io.grpc.Status.*
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Singleton
class RegistrarChaveServer(
    val service: ChavePixService
) : RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun gerarChave(request: ChaveRequest, responseObserver: StreamObserver<ChaveResponse>) {
        logger.info("Gerando chave...")

        try {
            val chaveRequest = request.toModel()
            val chave = service.registra(chaveRequest)

            val response = ChaveResponse.newBuilder()
                .setPixId(chave.id!!)
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
            logger.info("Chave pix '${chave.valor}' cadastrada com sucesso!")

        } catch (e: ChaveDuplicadaException) {
            responseObserver.onError(
                ALREADY_EXISTS
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )

            logger.error(e.message)
        } catch (e: HttpClientException) {
            responseObserver.onError(
                NOT_FOUND
                    .withDescription("Cliente não encontrado")
                    .asRuntimeException()
            )

            logger.error("Serviço indisponível")
        }
    }
}