package au.com.console.jpaspecificationdsl

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
            bWhite,
            cEastwood,
            sConnery,
            mFreeman,
            hFord,
            rWilliams,
            tHanks,
            sBullock,
            kReeves,
        )
    }

    @AfterEach
    fun tearDown() {
        actorRepo.deleteAll()
    }

    @Test
    fun `Should allow to sort by single field asc`() {
        val actors = actorRepo.findAll(
            and(),
            sortedBy(Actor::birthYear.asc())
        )

        val expected = actors.sortedWith(compareBy { it.birthYear })
        assertThat(actors, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by single field desc`() {
        val actors = actorRepo.findAll(
            and(),
            sortedBy(Actor::birthYear.desc())
        )

        val expected = actors.sortedWith(compareByDescending { it.birthYear })
        assertThat(actors, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with sortedBy without specifying direction`() {
        val actors = actorRepo.findAll(
            and(),
            sortedBy(
                Actor::birthYear,
                Actor::firstName,
            )
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenBy { it.firstName })
        assertThat(actors, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with sortedBy and different directions`() {
        val actors = actorRepo.findAll(
            and(),
            sortedBy(
                Actor::birthYear.asc(),
                Actor::firstName.desc(),
            )
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
        assertThat(actors, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with andThen without specifying direction`() {
        val actors = actorRepo.findAll(
            and(),
            Actor::birthYear andThen Actor::firstName,
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenBy { it.firstName })
        assertThat(actors, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with and without specifying direction`() {
        val actors = actorRepo.findAll(
            and(),
            Actor::birthYear andThen Actor::firstName.desc(),
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
        assertThat(actors, Matchers.equalTo(expected))
    }

    @Test
    fun `Should allow to sort by multiple fields with andThen and different directions`() {
        val actors = actorRepo.findAll(
            and(),
            Actor::birthYear.asc() andThen Actor::firstName.desc(),
        )

        val expected = this.actors.sortedWith(compareBy<Actor> { it.birthYear }.thenByDescending { it.firstName })
        println(actors)
        println(expected)
        assertThat(actors, Matchers.equalTo(expected))
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

}
