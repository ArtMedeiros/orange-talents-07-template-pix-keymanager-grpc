package br.com.zup.edu.chaves

import br.com.zup.edu.externo.bcb.TipoChaveBCB

enum class TipoChaveEntity {
    CPF {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.CPF
        }
    },
    TELEFONE {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.PHONE
        }
    },
    EMAIL {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.EMAIL
        }
    },
    RANDOM {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.RANDOM
        }
    };

    abstract fun converterBcb(): TipoChaveBCB
}