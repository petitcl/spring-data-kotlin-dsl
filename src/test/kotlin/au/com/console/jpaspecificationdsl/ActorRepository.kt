package au.com.console.jpaspecificationdsl

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

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
