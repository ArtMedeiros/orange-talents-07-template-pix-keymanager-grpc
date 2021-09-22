package br.com.zup.edu.utils.services.itau

import br.com.zup.edu.chaves.TipoContaEntity
import br.com.zup.edu.utils.services.itau.dto.ContaItauResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp.itau.host}")
interface ErpItauClient {

    @Get("\${erp.itau.get-client}")
    fun buscarCliente(@PathVariable clienteId: String, @QueryValue tipo: TipoContaEntity): ContaItauResponse?
}