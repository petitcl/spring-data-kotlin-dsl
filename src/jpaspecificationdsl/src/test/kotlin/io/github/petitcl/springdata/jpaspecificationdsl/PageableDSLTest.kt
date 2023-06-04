package io.github.petitcl.jpaspecificationdsl

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
open class PageableDSLTest {

    @Autowired
    lateinit var actorRepo: ActorRepository
    private lateinit var bWhite: Actor
    private lateinit var cEastwood: Actor
    private lateinit var sConnery: Actor
    private lateinit var mFreeman: Actor
    private lateinit var hFord: Actor
    private lateinit var rWilliams: Actor
    private lateinit var tHanks: Actor
    private lateinit var sBullock: Actor
    private lateinit var kReeves: Actor
    private lateinit var actors: List<Actor>

    @BeforeEach
    fun setup() {
        with(actorRepo) {
            mFreeman = save(Actor(firstName = "Morgan", lastName = "Freeman", birthYear = "1937"))
            cEastwood = save(Actor(firstName = "Clint", lastName = "Eastwood", birthYear = "1930"))
            rWilliams = save(Actor(firstName = "Robin", lastName = "Williams", birthYear = "1951"))
            kReeves = save(Actor(firstName = "Keanu", lastName = "Reeves", birthYear = "1964"))
            hFord = save(Actor(firstName = "Harrison", lastName = "Ford", birthYear = "1942"))
            bWhite = save(Actor(firstName = "Betty", lastName = "White", birthYear = "1922"))
            sBullock = save(Actor(firstName = "Sandra", lastName = "Bullock", birthYear = "1964"))
            tHanks = save(Actor(firstName = "Tom", lastName = "Hanks", birthYear = "1956"))
            sConnery = save(Actor(firstName = "Sean", lastName = "Connery", birthYear = "1930"))
        }

        actors = listOf(
            mFreeman,
            cEastwood,
            rWilliams,
            kReeves,
            hFord,
            bWhite,
            sBullock,
            tHanks,
            sConnery,
        )
    }

    @AfterEach
    fun tearDown() {
        actorRepo.deleteAll()
    }

    @Test
    fun `Should allow to specify no sort`() {
        val result = actorRepo.findAll(
            and(),
            unsorted()
        )

        assertThat(result, Matchers.containsInAnyOrder(*actors.toTypedArray()))
    }

    @Test
    fun `Should allow to sort by single field asc`() {
        val result = actorRepo.findAll(
            and(),
            sortedBy(Actor::firstName.asc())
        )

        val expected = actors.sortedWith(compareBy { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by single field desc`() {
        val result = actorRepo.findAll(
            and(),
            sortedBy(Actor::firstName.desc())
        )

        val expected = actors.sortedWith(compareByDescending { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with sortedBy without specifying direction`() {
        val result = actorRepo.findAll(
            and(),
            sortedBy(
                Actor::birthYear,
                Actor::firstName,
            )
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenBy { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with sortedBy and different directions`() {
        val result = actorRepo.findAll(
            and(),
            sortedBy(
                Actor::birthYear.asc(),
                Actor::firstName.desc(),
            )
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with andThen without specifying direction`() {
        val result = actorRepo.findAll(
            and(),
            Actor::birthYear andThen Actor::firstName,
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenBy { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with direction then without direction`() {
        val result = actorRepo.findAll(
            and(),
            Actor::birthYear.desc() andThen Actor::firstName,
        )

        val expected = this.actors.sortedWith(compareByDescending<Actor> { it.birthYear }.thenBy { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with and without specifying direction`() {
        val result = actorRepo.findAll(
            and(),
            Actor::birthYear andThen Actor::firstName.desc(),
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with andThen and different directions`() {
        val result = actorRepo.findAll(
            and(),
            Actor::birthYear.asc() andThen Actor::firstName.desc(),
        )

        val expected = this.actors
            .sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
        assertThat(result, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify no page`() {
        val result = actorRepo.findAll(
            and(),
            unpaged()
        )

        assertThat(result.content, Matchers.containsInAnyOrder(*actors.toTypedArray()))
    }

    @Test
    fun `Should allow to specify a page`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 0, size = 2)
        )

        val expected = this.actors.take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a page greater than 0`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 1, size = 2)
        )

        val expected = this.actors.drop(2).take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a limit`() {
        val result = actorRepo.findAll(
            and(),
            limit(2)
        )

        val expected = this.actors.take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a page and sort on one field`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 0, size = 2).sortedBy(Actor::firstName)
        )

        val expected = this.actors
            .sortedWith(compareBy { it.firstName })
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a page and sort on one field with direction`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 0, size = 2)
                .sortedBy(Actor::firstName.desc())
        )

        val expected = this.actors
            .sortedWith(compareByDescending { it.firstName })
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a page and sort on multiple fields without direction`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 0, size = 2)
                .sortedBy(Actor::birthYear, Actor::firstName)
        )

        val expected = this.actors
            .sortedWith(compareBy<Actor> { it.birthYear }.thenBy { it.firstName })
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a page and sort on multiple fields with direction`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 0, size = 2)
                .sortedBy(Actor::birthYear.asc(), Actor::firstName.desc())
        )

        val expected = this.actors
            .sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a sort then a page`() {
        val result = actorRepo.findAll(
            and(),
            sortedBy(Actor::birthYear.desc())
                .paged(page = 1, size = 2)
        )

        val expected = this.actors
            .sortedWith(compareByDescending { it.birthYear })
            .drop(2)
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a sort then a limit`() {
        val result = actorRepo.findAll(
            and(),
            sortedBy(Actor::firstName.desc())
                .limit(2)
        )

        val expected = this.actors
            .sortedWith(compareByDescending { it.firstName })
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to specify a page and sort on multiple fields with direction using andThen`() {
        val result = actorRepo.findAll(
            and(),
            paged(page = 0, size = 2)
                .sortedBy(Actor::birthYear.asc() andThen Actor::firstName.desc())
        )

        val expected = this.actors
            .sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
            .take(2)
        assertThat(result.content, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to use Sort Order functions`() {
        assertEquals(Actor::birthYear.asc(), Order.by("birthYear"))
        assertEquals(Actor::birthYear.desc(), Order(Sort.Direction.DESC, "birthYear"))
        assertEquals(Actor::birthYear.nullsFirst(), Order.by("birthYear").nullsFirst())
        assertEquals(Actor::birthYear.nullsLast(), Order.by("birthYear").nullsLast())
        assertEquals(Actor::birthYear.nullsNative(), Order.by("birthYear").nullsNative())
        assertEquals(Actor::birthYear.ignoreCase(), Order.by("birthYear").ignoreCase())
        assertEquals(
            Actor::birthYear.desc().nullsFirst().ignoreCase(),
            Order(Sort.Direction.DESC, "birthYear").nullsFirst().ignoreCase(),
        )
    }

    @Test
    fun `Should allow to use sort aliases`() {
        assertEquals(Actor::birthYear.asc(), Actor::birthYear.ascending())
        assertEquals(Actor::birthYear.desc(), Actor::birthYear.descending())
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
            Order.asc("birthYear"),
            Order.desc("firstName"),
            Order.asc("lastName"),
        )
        assertEquals(result1, expected)
        assertEquals(result2, expected)
        assertEquals(result3, expected)
        assertEquals(result4, expected)
    }

}

