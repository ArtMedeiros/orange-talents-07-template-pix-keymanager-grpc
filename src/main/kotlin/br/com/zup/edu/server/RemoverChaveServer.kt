package br.com.zup.edu.server

import br.com.zup.edu.RemoverChaveServiceGrpc
import br.com.zup.edu.RemoverRequest
import br.com.zup.edu.RemoverResponse
import br.com.zup.edu.utils.error.ChaveNaoEncontradaException
import br.com.zup.edu.utils.services.ChavePixService
import br.com.zup.edu.utils.services.toModel
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Singleton
class RemoverChaveServer (
    val service: ChavePixService
) : RemoverChaveServiceGrpc.RemoverChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun removerChave(request: RemoverRequest, responseObserver: StreamObserver<RemoverResponse>) {
        try {
            logger.info("Removendo chave pix...")
            val remover = request.toModel()
            val chave = service.remove(remover)

            val response = RemoverResponse.newBuilder()
                .setStatus("OK")
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()

            logger.info("Chave pix '${chave.valor}' removida!")
        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )

            logger.error(e.message)
        } catch (e: ChaveNaoEncontradaException) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )

            logger.error("Chave não encontrada")
        } catch (e: HttpClientException) {
            responseObserver.onError(
                Status.UNAVAILABLE
                    .withDescription("Serviço indisponível")
                    .asRuntimeException()
            )

            logger.error("Serviço indisponível")
        }
    }
}