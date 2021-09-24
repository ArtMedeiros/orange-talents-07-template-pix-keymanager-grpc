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

    @Get("\${bcb.pix.services}/{key}")
    fun buscarChaveByValor(@PathVariable key: String) : PixKeyDetailsResponse?

    @Post("\${bcb.pix.services}")
    fun cadastrarChave(@Body @Valid request: CreatePixKeyRequest) : CreatePixKeyResponse?

    @Delete("\${bcb.pix.services}/{key}")
    fun removerChave(@PathVariable key: String, @Body @Valid request: DeletePixKeyRequest) : HttpResponse<Any?>
}