package br.com.zup.edu.server

import br.com.zup.edu.ConsultaRequest
import br.com.zup.edu.ConsultaResponse
import br.com.zup.edu.ConsultarChaveServiceGrpc
import br.com.zup.edu.utils.error.config.ErrorHandler
import br.com.zup.edu.utils.services.ChavePixService
import br.com.zup.edu.utils.services.convertToConsultaChaveResponse
import br.com.zup.edu.utils.services.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@ErrorHandler
class ConsultarChaveServer(
    val service: ChavePixService
) : ConsultarChaveServiceGrpc.ConsultarChaveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun consultarChave(request: ConsultaRequest, responseObserver: StreamObserver<ConsultaResponse>) {
        logger.info("Buscando chave")

        val consultaRequest = request.toModel()
        val dados = service.consulta(consultaRequest)

        val response: ConsultaResponse = convertToConsultaChaveResponse(dados, consultaRequest)

        logger.info("Retornando dados da chave ${dados.valor}")
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}