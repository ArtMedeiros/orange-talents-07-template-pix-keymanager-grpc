package br.com.zup.edu.chaves

import br.com.zup.edu.utils.services.bcb.TipoChaveBCB
import io.micronaut.validation.validator.constraints.EmailValidator

enum class TipoChaveEntity {
    CPF {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.CPF
        }

        override fun valida(valor: String): Boolean {
            return if (valor.isNullOrBlank())
                false
            else
                valor.matches("^[0-9]{11}\$".toRegex())
        }
    },
    TELEFONE {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.PHONE
        }

        override fun valida(valor: String): Boolean {
            return if (valor.isNullOrBlank())
                false
            else
                valor.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.EMAIL
        }

        override fun valida(valor: String): Boolean {
            return EmailValidator().run {
                initialize(null)
                isValid(valor, null)
            }
        }
    },
    RANDOM {
        override fun converterBcb(): TipoChaveBCB {
            return TipoChaveBCB.RANDOM
        }

        override fun valida(valor: String): Boolean {
            return valor.isNullOrBlank()
        }
    };

    abstract fun converterBcb(): TipoChaveBCB
    abstract fun valida(valor: String): Boolean
}