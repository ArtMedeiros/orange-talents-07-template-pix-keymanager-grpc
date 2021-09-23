package br.com.zup.edu.chaves.dto

import io.micronaut.core.annotation.Introspected

@Introspected
data class ConsultarChaveRequest(
    val clienteId: String,
    val pixId: Long,
    val chave: String
)