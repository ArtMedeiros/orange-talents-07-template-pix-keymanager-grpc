package br.com.zup.edu.server

import br.com.zup.edu.ConsultarChaveServiceGrpc
import br.com.zup.edu.ListaRequest
import br.com.zup.edu.ListaResponse
import br.com.zup.edu.ListarChaveServiceGrpc
import br.com.zup.edu.utils.error.config.ErrorHandler
import br.com.zup.edu.utils.services.ChavePixService
import br.com.zup.edu.utils.services.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@ErrorHandler
class ListarChaveServer(
    val service: ChavePixService
) : ListarChaveServiceGrpc.ListarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun listarChave(request: ListaRequest, responseObserver: StreamObserver<ListaResponse>) {
        logger.info("Buscando chaves cadastradas")

        val dados = service.listar(request.clienteId)

        logger.info("Retornando chaves do cliente '${request.clienteId}'")
        responseObserver.onNext(dados)
        responseObserver.onCompleted()
    }
}