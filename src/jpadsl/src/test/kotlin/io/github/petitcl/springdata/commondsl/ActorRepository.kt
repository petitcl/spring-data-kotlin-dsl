package io.github.petitcl.springdata.commondsl

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Repository
interface ActorRepository : CrudRepository<Actor, Int>, JpaSpecificationExecutor<Actor>

@Entity
data class Actor(
    @Id
    @GeneratedValue
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val birthYear: String? = "",
)
