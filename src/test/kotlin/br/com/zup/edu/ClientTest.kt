package br.com.zup.edu

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import jakarta.inject.Singleton

@Factory
class ClientsTest {

    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ChavePixServiceGrpc.ChavePixServiceBlockingStub? {
        return ChavePixServiceGrpc.newBlockingStub(channel)
    }
}