package br.com.zup.edu.utils.services

import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.TipoChave.INVALID
import br.com.zup.edu.TipoConta
import br.com.zup.edu.TipoConta.*
import br.com.zup.edu.chaves.ChaveGRPCRequest
import br.com.zup.edu.chaves.TipoChaveEntity
import br.com.zup.edu.chaves.TipoContaEntity

fun ChaveRequest.toModel(): ChaveGRPCRequest {
    return ChaveGRPCRequest(
        cliente = cliente,
        tipo = when (tipoChave) {
            INVALID -> null
            else -> TipoChaveEntity.valueOf(tipoChave.name)
        },
        chave = chave,
        tipoConta = when (conta) {
            UNKNOW -> null
            else -> TipoContaEntity.valueOf(conta.name)
        }
    )
}