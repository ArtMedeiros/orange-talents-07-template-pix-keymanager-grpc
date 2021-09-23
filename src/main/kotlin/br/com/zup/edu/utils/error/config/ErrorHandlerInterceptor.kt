package br.com.zup.edu.utils.error.config

import br.com.zup.edu.utils.error.ChaveDuplicadaException
import br.com.zup.edu.utils.error.ChaveNaoEncontradaException
import br.com.zup.edu.utils.error.ClienteNaoEncontradoException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<Any, Any> {

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        } catch (ex: Exception) {

            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (ex) {
                is ConstraintViolationException -> {
                    logger.error(ex.message)

                    Status.INVALID_ARGUMENT
                        .withCause(ex)
                        .withDescription(ex.message)
                }

                is ChaveNaoEncontradaException -> {
                    logger.error(ex.message)

                    Status.NOT_FOUND
                        .withDescription(ex.message)
                }

                is ClienteNaoEncontradoException -> {
                    logger.error(ex.message)

                    Status.NOT_FOUND
                        .withDescription(ex.message)
                }

                is ChaveDuplicadaException -> {
                    logger.error(ex.message)

                    Status.ALREADY_EXISTS
                        .withDescription(ex.message)
                }

                is HttpClientResponseException -> {
                    logger.error("Chave ou Cliente não encontrado")

                    Status.NOT_FOUND
                        .withCause(ex)
                        .withDescription("Chave ou Cliente não encontrado")
                }

                is HttpClientException -> {
                    logger.error("Serviço temporariamente indisponível")

                    Status.UNAVAILABLE
                        .withCause(ex)
                        .withDescription("Serviço temporariamente indisponível")
                }

                else -> {
                    logger.error("Não foi possível processar a requisição")

                    Status.UNKNOWN
                        .withCause(ex)
                        .withDescription("Não foi possível processar a requisição")
                }
            }

            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }

}