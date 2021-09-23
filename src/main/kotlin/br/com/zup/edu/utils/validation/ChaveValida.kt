package br.com.zup.edu.utils

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
        return value.tipo?.valida(value.chave) ?: false
    }
}