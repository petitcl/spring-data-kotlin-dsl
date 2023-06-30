# Spring Data Kotlin DSL

[![Build Status](https://github.com/petitcl/spring-data-kotlin-dsl/actions/workflows/build.yaml/badge.svg?branch=main)](https://github.com/petitcl/spring-data-kotlin-dsl/actions/workflows/build.yaml/badge.svg?branch=main)
[![License](http://img.shields.io/:license-apache-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

This library provides fluent and type safe DSLs for working with Spring Data repositories, without boilerplate code or generated metamodel / compiler plugins.
This library currently offers the following DSLs:
- Spring Data Common Paging and Sorting DSL
- JPA Criteria API DSL
- Spring Data JPA specification DSL

Originally, this library started as a fork of [consoleau/kotlin-jpa-specification-dsl](https://github.com/consoleau/kotlin-jpa-specification-dsl),
in order to add support for Spring Boot 3.x, but it evolved into a set of DSLs for Spring Data Common and Spring Data JPA.

## Quick Start

```
implementation("io.github.petitcl:spring-data-jpa-kotlin-dsl:$springDataJpaKotlinDslVersion")
```

Or, if you just want to use the Spring Data Common DSL:
```
implementation("io.github.petitcl:spring-data-common-kotlin-dsl:$springDataCommonKotlinDslVersion")
``` 


## Spring Data JPA specification DSL

```kotlin
import io.github.petitcl.springdata.jpadsl.*   // 1. Import extension functions

// 2. Declare JPA Entities
@Entity
data class TvShow(
    @Id
    @GeneratedValue
    val id: Int = 0,
    val name: String = "",
    val synopsis: String = "",
    val availableOnNetflix: Boolean = false,
    val releaseDate: String? = null,
    @OneToMany(cascade = [CascadeType.ALL])
    val starRatings: Set<StarRating> = emptySet()
)

@Entity
data class StarRating(
    @Id
    @GeneratedValue
    val id: Int = 0,
    val stars: Int = 0
)


// 3. Declare JPA Repository with JpaSpecificationExecutor
@Repository
interface TvShowRepository : CrudRepository<TvShow, Int>, JpaSpecificationExecutor<TvShow>


// 4. Kotlin Properties are now usable to create fluent specifications
@Service
class MyService @Inject constructor(val tvShowRepo: TvShowRepository) {
   fun findShowsReleasedIn2010NotOnNetflix(): List<TvShow> {
     return tvShowRepo.findAll(TvShow::availableOnNetflix.isFalse() and TvShow::releaseDate.equal("2010"))
   }

   /* Fall back to spring API with some extra helpers for more complex join queries */
   fun findShowsWithComplexQuery(): List<TvShow> {
       return tvShowRepo.findAll(where { equal(it.join(TvShow::starRatings).get(StarRating::stars), 2) })
   }

   /* Using paging and sorting DSL alongside the specification DSL */ 
   fun findFirst10ShowsOnNetflixOrderedByNameDesc(): List<TvShow> {
       return tvShowRepo.findAll(
           TvShow::availableOnNetflix.isTrue(),
           paged(page = 0, size = 10).sortedBy(TvShow::name.desc()),
       )
   }

}
```

### Advanced Usage

For more complex and dynamic queries it's good practice to create functions that use the DSL to make queries more readable,
and to allow for their composition in complex dynamic queries.

```kotlin
fun hasName(name: String?): Specification<TvShow>? = name?.let {
    TvShow::name.equal(it)
}

fun availableOnNetflix(available: Boolean?): Specification<TvShow>? = available?.let {
    TvShow::availableOnNetflix.equal(it)
}

fun hasReleaseDateIn(releaseDates: List<String>?): Specification<TvShow>? = releaseDates?.let {
    TvShow::releaseDate.`in`(releaseDates)
}

fun hasKeywordIn(keywords: List<String>?): Specification<TvShow>? = keywords?.let {
    or(keywords.map(::hasKeyword))
}

fun hasKeyword(keyword: String?): Specification<TvShow>? = keyword?.let {
    TvShow::synopsis.like("%$keyword%")
}
```

These functions can be combined with and() and or() for complex nested queries:

```kotlin
    val shows = tvShowRepo.findAll(
            or(
                    and(
                            availableOnNetflix(false),
                            hasKeywordIn(listOf("Jimmy"))
                    ),
                    and(
                            availableOnNetflix(true),
                            or(
                                    hasKeyword("killer"),
                                    hasKeyword("monster")
                            )
                    )
            )
    )
```

Or they can be combined with a service-layer query DTO and mapping extension function

```kotlin
    /**
     * A TV show query DTO - typically used at the service layer.
     */
    data class TvShowQuery(
            val name: String? = null,
            val availableOnNetflix: Boolean? = null,
            val keywords: List<String> = listOf(),
            val releaseDates: List<String> = listOf()
    )

    /**
     * A single TvShowQuery is equivalent to an AND of all supplied criteria.
     * Note: any criteria that is null will be ignored (not included in the query).
     */
    fun TvShowQuery.toSpecification(): Specification<TvShow> = and(
            hasName(name),
            availableOnNetflix(availableOnNetflix),
            hasKeywordIn(keywords),
            hasReleaseDateIn(releaseDates)
    )
```

For powerful dynamic queries:

```kotlin
    val query = TvShowQuery(availableOnNetflix = false, keywords = listOf("Rick", "Jimmy"))
    val shows = tvShowRepo.findAll(query.toSpecification())
```

For more details and examples, refer to `JPASpecificationDSLIntTest.kt` in the unit tests.

### Spring Data Common DSL

```kotlin
import io.github.petitcl.springdata.commondsl.*   // 1. Import extension functions
import org.springframework.data.domain.Page
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.*

// 2. Declare JPA Entities
@Entity
data class Actor(
    @Id
    @GeneratedValue
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val birthYear: String? = "",
)

// 3. Declare Repository (with PagingAndSortingRepository or JpaSpecificationExecutor)
@Repository
interface ActorRepository : CrudRepository<Actor, Int>, PagingAndSortingRepository<Actor, Int>

// 4. Kotlin Properties are now usable to create fluent specifications
@Service
class MyService constructor(val actorRepo: ActorRepository) {

    /* Using sort only */
    fun findAllActorsSortedByLastName(): List<Actor> {
        return actorRepo.findAll(sortedBy(Actor::lastName.asc())).toList()
    }

    /* Using pagination only */
    fun findFirst10Actors(): Page<Actor> {
        return actorRepo.findAll(paged(page = 0, size = 10))
    }

    /* or, if you prefer */
    fun findFirst10Actors2(): Page<Actor> {
        return actorRepo.findAll(limit(10))
    }

    /* Using pagination + sort */
    fun findFirst10ActorsByBirthDate(): Page<Actor> {
        return actorRepo.findAll(sortedBy(Actor::birthYear.asc()).paged(page = 0, size = 10))
    }

    /* or, if you prefer */
    fun findFirst10ActorsByBirthDate2(): Page<Actor> {
        return actorRepo.findAll(paged(page = 0, size = 10).sortedBy(Actor::birthYear.asc()))
    }

}
```


## How it works

The basic idea of all the DSLs in this repository is to use Kotlin property references in order to build type safe queries, pagination and sorting.
Spring Data only supports referencing entity properties by name, which is not type safe and can lead to runtime errors.
For example, if you rename a property in your entity, the query will still compile, but will fail at runtime, since the old field does exist anymore.
On top of that, Spring Data queries can be quite verbose in some cases (eg: JPA specifications), especially when used in a Kotlin project.

The DSLs are based on two simple mechanisms:
- Kotlin Property references, more specifically the `KProperty` class. Property references are compile type references that can be read at runtime. 
For example, if we have an entity class named `TvShow`, we can create a compile time reference, `TvShow::releaseDate`, which will be of type `KProperty1<T,R?>`.
We can then read the name of the field using `KProperty1<T,R?>.name`, which will return the name of the field as a String.
If the field name changes, the code will not compile anymore, until the property is correctly referenced.
- Extension functions on the `KProperty` class.
We can add extension functions on the `KProperty1` that allow to create Spring Data query objects that reference the name of the field. 
Given this is a reference checked by the compiler, we can guarantee that the field name is correct at runtime.


### Spring JPA specification DSL

This Spring JPA DSL builds on [Spring Data's Specifications abstraction](http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications),
combined with the `KProperty` extension methods mentioned above, to remove the boilerplate and the need to generate a metamodel.

For example, the `equal` extension function is defined as follows:
```kotlin
fun <T, R> KProperty1<T, R?>.equal(x: R): Specification<T> = spec { builder.equal(it, x) }
```

This is implemented using a private helper function `spec` that captures the common use case of taking an Entity property,
and using a JPA `CriteriaBuilder` to create a JPA `Predicate`. 

### JPA Criteria API DSL

For more advance JPA query use cases, for example in case of joins or aggregations, this module also provides a DSL for the [JPA Criteria API](https://www.baeldung.com/hibernate-criteria-queries) directly. 

In order to use this DSL, it is required to call the `where` function, and provide it a lambda that returns the desired complex query. 
The catch here is that this operation is at a different level of abstraction than the JPA DSL. More specifically, the Spring JPA DSL works with `Specifications`, while the JPA Criteria API works with `Predicates`.
Therefore, this library redefines the same functions as for Spring JPA (`Specification` based), but this time with `Predicate` instead.

For example, here is the JPA Criteria DSL equivalent of the `equal` function presented earlier:
```kotlin
context(JPACriteriaDsl)
fun <R> Expression<R>.equal(value: R): Predicate = builder.equal(this, value)
```


The advanced query usage looks like this:
```kotlin
// Fetch all TvSHows, joining on StarRatings, and keeping only rows that have a StarRating > 2 or < 4
val shows = tvShowRepo.findAll(where {
    val join = root.innerJoin(TvShow::starRatings)
    or(
        join.get(StarRating::stars).greaterThan(2),
        join.get(StarRating::stars).lessThan(4)
    )
})
```


:warning: Warning: this particular feature set is based on context receivers and that requires context receivers to be enabled in your project.


### Creating your own operators / extensions
It is possible that you will want to create you own custom operators or extensions, or that the library does not (yet) cover some existing operator (contributions are welcome!). In that case, you can very simply define your own function.

For example, let's imagine you want to create a new operation `betweenExclusive`, that does the same as `between`, except that it does not match if the values are equal to the range bounds. That operation can simply be implemented in terms of `lessThanOrEqualTo` and `greaterThanOrEqualTo`.

You would simply need to create the following extension in order to make this new operation available in the Spring Data JPA DSL: 

```kotlin
fun <T, R : Comparable<R>> KProperty1<T, R?>.betweenExclusive(x: R, y: R): Specification<T> = spec {
    and(
        builder.greaterThanOrEqualTo(it, x),
        builder.lessThanOrEqualTo(it, y)
    )
}
```

And you would need to create this extension in order to make this new operation available in the Jpa Criteria API: 
```kotlin
context(JPACriteriaDsl)
fun <R> Expression<R>.betweenExclusive(x: R, y: R): Predicate = and(
        builder.greaterThanOrEqualTo(this, x),
        builder.lessThanOrEqualTo(this, y)
)
```


## Contributing to the Project

If you'd like to contribute code to this project you can do so through GitHub by forking the repository and generating a pull request.

By contributing your code, you agree to license your contribution under the terms of the Apache License v2.0. 

## License

```text
    Copyright 2023 Clément Petit 

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```