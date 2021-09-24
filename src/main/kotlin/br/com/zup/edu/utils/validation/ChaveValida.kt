package br.com.zup.edu.utils

import br.com.zup.edu.chaves.TipoChaveEntity
import br.com.zup.edu.chaves.TipoChaveEntity.*
import br.com.zup.edu.chaves.dto.RegistrarChaveRequest
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
class ChaveValidator : ConstraintValidator<ChaveValida, RegistrarChaveRequest> {
    override fun isValid(
        value: RegistrarChaveRequest,
        annotationMetadata: AnnotationValue<ChaveValida>,
        context: ConstraintValidatorContext
    ): Boolean {
        return validaEnum(value.chave, value.tipo)
    }

    private fun validaEnum(chave: String, tipo: TipoChaveEntity): Boolean {
        if (chave.isBlank() && tipo != RANDOM)
            return false

        return when(tipo) {
            CPF -> chave.matches("^[0-9]{11}\$".toRegex())
            TELEFONE -> chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
            EMAIL -> chave.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+\$".toRegex())
            else -> chave.isBlank()
        }
    }
}