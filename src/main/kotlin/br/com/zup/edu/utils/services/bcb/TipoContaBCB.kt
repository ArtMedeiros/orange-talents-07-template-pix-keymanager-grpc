package br.com.zup.edu.utils.services.bcb

import br.com.zup.edu.chaves.TipoContaEntity

enum class TipoContaBCB {
    CACC {
        override fun converterTipoContaEntity(): TipoContaEntity {
            return TipoContaEntity.CONTA_CORRENTE
        }
    },
    SVGS {
        override fun converterTipoContaEntity(): TipoContaEntity {
            return TipoContaEntity.CONTA_POUPANCA
        }
    };

    abstract fun converterTipoContaEntity(): TipoContaEntity
}