package br.com.zup.edu.chaves

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChaveEntity, Long>{

    fun existsByValor(valor: String) : Boolean

    fun findByIdAndIdCliente(id: Long, idCliente: String) : Optional<ChaveEntity>

    fun findByValor(valor: String) : ChaveEntity
}