package br.com.zup.edu.chaves

import io.micronaut.core.annotation.Introspected

@Introspected
data class ConsultarChaveRequest(
    val clienteId: String,
    val pixId: Long,
    val chave: String
)