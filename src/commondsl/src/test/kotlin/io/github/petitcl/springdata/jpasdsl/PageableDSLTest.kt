package io.github.petitcl.springdata.jpasdsl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Actor(
    @Id
    @GeneratedValue
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val birthYear: String? = "",
)

class PageableDSLTest {

    @Test
    fun `Should allow to create a paged page`() {
        Assertions.assertEquals(
            PageRequest.of(1, 2),
            paged(page = 1, size = 2),
        )
    }

    @Test
    fun `Should allow to create a limit page`() {
        Assertions.assertEquals(
            PageRequest.of(0, 2),
            limit(size = 2),
        )
    }

    @Test
    fun `Should allow to create an unpaged page`() {
        Assertions.assertEquals(
            Pageable.unpaged(),
            unpaged(),
        )
    }

    @Test
    fun `Should allow to use Sort Order functions`() {
        Assertions.assertEquals(
            Sort.Order.by("birthYear"),
            Actor::birthYear.asc(),
        )

        Assertions.assertEquals(
            Sort.Order(Sort.Direction.DESC, "birthYear"),
            Actor::birthYear.desc(),
        )

        Assertions.assertEquals(
            Sort.Order.by("birthYear").nullsFirst(),
            Actor::birthYear.nullsFirst(),
        )

        Assertions.assertEquals(
            Sort.Order.by("birthYear").nullsLast(),
            Actor::birthYear.nullsLast(),
        )

        Assertions.assertEquals(
            Sort.Order.by("birthYear").nullsNative(),
            Actor::birthYear.nullsNative(),
        )

        Assertions.assertEquals(
            Sort.Order.by("birthYear").ignoreCase(),
            Actor::birthYear.ignoreCase(),
        )

        Assertions.assertEquals(
            Sort.Order(Sort.Direction.DESC, "birthYear").nullsFirst().ignoreCase(),
            Actor::birthYear.desc().nullsFirst().ignoreCase(),
        )
    }

    @Test
    fun `Should allow to use sort aliases`() {
        Assertions.assertEquals(Actor::birthYear.asc(), Actor::birthYear.ascending())
        Assertions.assertEquals(Actor::birthYear.desc(), Actor::birthYear.descending())
    }

    @Test
    fun `Should allow to combine more than 2 orders`() {
        val result1 = sortedBy(
            Actor::birthYear.asc(),
            Actor::firstName.desc(),
            Actor::lastName.asc(),
        )

        val result2 = Actor::birthYear.asc() andThen Actor::firstName.desc() andThen Actor::lastName.asc()

        val result3 = sortedBy(Actor::birthYear.asc()) andThen (Actor::firstName.desc() andThen Actor::lastName.asc())

        val result4 =  Actor::birthYear andThen Actor::firstName.desc() andThen Actor::lastName

        val expected = Sort.by(
            Sort.Order.asc("birthYear"),
            Sort.Order.desc("firstName"),
            Sort.Order.asc("lastName"),
        )

        Assertions.assertEquals(expected, result1)
        Assertions.assertEquals(expected, result2)
        Assertions.assertEquals(expected, result3)
        Assertions.assertEquals(expected, result4)
    }

}