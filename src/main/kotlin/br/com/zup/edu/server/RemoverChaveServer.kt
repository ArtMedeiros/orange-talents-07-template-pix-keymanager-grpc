package br.com.zup.edu.server

import br.com.zup.edu.RemoverChaveServiceGrpc
import br.com.zup.edu.RemoverRequest
import br.com.zup.edu.RemoverResponse
import br.com.zup.edu.utils.error.config.ErrorHandler
import br.com.zup.edu.utils.services.ChavePixService
import br.com.zup.edu.utils.services.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@ErrorHandler
class RemoverChaveServer (
    val service: ChavePixService
) : RemoverChaveServiceGrpc.RemoverChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun removerChave(request: RemoverRequest, responseObserver: StreamObserver<RemoverResponse>) {
        logger.info("Removendo chave pix...")
        val remover = request.toModel()
        val chave = service.remove(remover)

        val response = RemoverResponse.newBuilder()
            .setStatus("OK")
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()

        logger.info("Chave pix '${chave.valor}' removida!")
    }
}