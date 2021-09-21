package br.com.zup.edu.externo.itau

import br.com.zup.edu.chaves.TipoContaEntity
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp.itau.host}")
interface ErpItauClient {

    @Get("\${erp.itau.get-client}")
    fun buscarCliente(@PathVariable clienteId: String, @QueryValue tipo: TipoContaEntity): ContaItauResponse?
}