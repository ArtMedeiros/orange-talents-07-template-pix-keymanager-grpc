package br.com.zup.edu.validation

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chaves.ChaveGRPCRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Constraint(validatedBy = [ChaveValidator::class])
annotation class ChaveValida(
    val message: String = "Chave inválida"
)

@Singleton
class ChaveValidator : ConstraintValidator<ChaveValida, ChaveGRPCRequest> {
    override fun isValid(
        value: ChaveGRPCRequest,
        annotationMetadata: AnnotationValue<ChaveValida>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.tipoConta == TipoConta.UNKNOW) {
            context.messageTemplate("Tipo de conta inválida")
            return false
        }

        if (value.tipo == TipoChave.INVALID) {
            context.messageTemplate("Tipo de chave inválida")
            return false
        }

        return when(value.tipo) {
            TipoChave.CPF -> {
                context.messageTemplate("CPF inválido")
                validCPF(value.chave)
            }
            TipoChave.EMAIL -> {
                context.messageTemplate("E-mail inválido")
                validEmail(value.chave)
            }
            TipoChave.TELEFONE -> {
                context.messageTemplate("Telefone inválido")
                validTelefone(value.chave)
            }
            else -> value.chave.isNullOrBlank()
        }
    }

    private fun validCPF(valor: String?): Boolean {
        return if (valor.isNullOrBlank())
            false
        else
            valor.matches("^[0-9]{11}\$".toRegex())
    }

    private fun validEmail(valor: String?): Boolean {
        return if (valor.isNullOrBlank())
            false
        else
            valor.matches("/\\S+@\\S+\\.\\S+/".toRegex())
    }

    private fun validTelefone(valor: String?): Boolean {
        return if (valor.isNullOrBlank())
            false
        else
            valor.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
    }
}