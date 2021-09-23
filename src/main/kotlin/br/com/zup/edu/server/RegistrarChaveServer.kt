package br.com.zup.edu.server

import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.ChaveResponse
import br.com.zup.edu.RegistrarChaveServiceGrpc
import br.com.zup.edu.utils.error.config.ErrorHandler
import br.com.zup.edu.utils.services.ChavePixService
import br.com.zup.edu.utils.services.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@ErrorHandler
class RegistrarChaveServer(
    val service: ChavePixService
) : RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun gerarChave(request: ChaveRequest, responseObserver: StreamObserver<ChaveResponse>) {
        logger.info("Gerando chave...")

        val chaveRequest = request.toModel()
        val chave = service.registra(chaveRequest)

        val response = ChaveResponse.newBuilder()
            .setPixId(chave.id!!)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
        logger.info("Chave pix '${chave.valor}' cadastrada com sucesso!")
    }
}