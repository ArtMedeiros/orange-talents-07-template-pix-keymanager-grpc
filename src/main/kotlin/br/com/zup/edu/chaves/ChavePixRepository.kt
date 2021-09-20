package br.com.zup.edu.chaves

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChaveEntity, Long>{

    fun existsByValor(valor: String) : Boolean
}