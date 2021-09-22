package br.com.zup.edu.utils.services.bcb

import br.com.zup.edu.utils.services.bcb.dto.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import javax.validation.Valid

@Client("\${bcb.pix.host}")
@Consumes(value = [MediaType.APPLICATION_XML])
@Produces(value = [MediaType.APPLICATION_XML])
interface BcbClient {

    @Post("\${bcb.pix.services}")
    fun cadastrarChave(@Valid @Body request: CreatePixKeyRequest) : CreatePixKeyResponse?

    @Delete("\${bcb.pix.services}/{key}")
    fun removerChave(@PathVariable key: String, @Valid @Body request: DeletePixKeyRequest) : HttpResponse<Any?>
}