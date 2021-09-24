package br.com.zup.edu.chaves

import br.com.zup.edu.utils.services.bcb.TipoChaveBCB

enum class TipoChaveEntity {
    CPF {
        override fun converterBcb(): TipoChaveBCB = TipoChaveBCB.CPF
    },
    TELEFONE {
        override fun converterBcb(): TipoChaveBCB = TipoChaveBCB.PHONE
    },
    EMAIL {
        override fun converterBcb(): TipoChaveBCB = TipoChaveBCB.EMAIL
    },
    RANDOM {
        override fun converterBcb(): TipoChaveBCB = TipoChaveBCB.RANDOM
    };

    abstract fun converterBcb(): TipoChaveBCB
}