package br.com.zup.edu.chaves

import br.com.zup.edu.externo.bcb.TipoContaBCB

enum class TipoContaEntity {
    CONTA_CORRENTE {
        override fun converterBcb(): TipoContaBCB {
            return TipoContaBCB.CACC
        }
    },
    CONTA_POUPANCA {
        override fun converterBcb(): TipoContaBCB {
            return TipoContaBCB.SVGS
        }
    };

    abstract fun converterBcb(): TipoContaBCB
}