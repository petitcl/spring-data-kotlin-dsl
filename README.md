### ⚠️⚠️⚠️ **This project is a work in progress.** ⚠️⚠️⚠️

# Spring Data Kotlin DSL

[![Build Status](https://github.com/petitcl/spring-data-kotlin-dsl/actions/workflows/build.yaml/badge.svg?branch=main)](https://github.com/petitcl/spring-data-kotlin-dsl/actions/workflows/build.yaml/badge.svg?branch=main)
[![License](http://img.shields.io/:license-apache-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

This library provides a fluent DSL for querying spring data JPA repositories using spring data Specifications (i.e. the JPA Criteria API),
without boilerplate code or a generated metamodel / compiler plugins.

This library is a fork of https://github.com/consoleau/kotlin-jpa-specification-dsl, with the added following features:
- Support for Spring Boot 3.x / Jarkarta EE 10 / Jakarta Persistence API 3.x
- DSL for Spring data common paging and sorting 

# Quick Start

```kotlin
dependencies {
    implementation("io.github.petitcl:spring-data-kotlin-dsl:$kotlinJpaSpecificationDslVersion")
}
```

# Example #

```kotlin
import io.github.petitcl.jpaspecificationdsl.*   // 1. Import extension functions

////
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


////
// 3. Declare JPA Repository with JpaSpecificationExecutor
@Repository
interface TvShowRepository : CrudRepository<TvShow, Int>, JpaSpecificationExecutor<TvShow>


////
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

   fun findFirst10ShowsOnNetflixOrderedByNameDesc(): List<TvShow> {
       return tvShowRepo.findAll(
           TvShow::availableOnNetflix.isTrue(),
           paged(page = 0, size = 10).sortedBy(TvShow::name.desc()),
       )
   }

}
```

# Advanced Usage #

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

For more details, refer to `JPASpecificationDSLTest.kt` in the unit tests.

# How it works #

This DSL builds on [Spring Data's Specifications abstraction](http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications),
sprinkling some Kotlin sugar over it to remove the boilerplate and the need to generate a metamodel.

The basic idea is to add extension functions to Kotlin's `KProperty` class, which is the reflection API representation of a Kotlin property.
The extension functions each represent a database operator (eg: `equal`, `like`, `greaterThan`, etc...) and are then used to build a `Specification` object,
which is then passed to the Spring Data repository.

For example, The code `TvShow::releaseDate.equal("2010")` is a call to the Kotlin extension function:

```kotlin
fun <T, R> KProperty1<T, R?>.equal(x: R): Specification<T> = spec { equal(it, x) }
```

This is a bit dense, but makes sense when it's broken down:

- `T`: The type of the object that the property is declared on, in this case TvShow
- `R`: The property type, for TvShow::releaseDate it is String
- `KProperty1<T,R?>`: Kotlin reflection API representation of the property `TvShow::releaseDate`. 
The 1 refers to a property with 1 receiver, and `R?` is declared as nullable for the method to work on nullable properties as well as non-null properties.
- `x`: The value to test against
- `Specification<T>`: The Spring data specifications result

This is implemented using a private helper function `spec` that captures the common use case of taking an Entity property, and using a `CriteriaBuilder` to create a `Predicate`:

```kotlin
private fun <T, R> KProperty1<T, R?>.spec(makePredicate: CriteriaBuilder.(path: Path<R>) -> Predicate): Specification<T> =
    this.let { property -> where { root -> makePredicate(root.get(property)) } }
```

This uses the `where` factory method, which expects a callback with the signature: `CriteriaBuilder.(Root<T>) -> Predicate`

The code converts a `KProperty1<T,R>` to a `Path<T>` using `root.get<R>(property)`.

Once it has a `Path<R>` to work with, it delegates to the `makePredicate` function to configure the `CriteriaBuilder` given the `Path`.

The `makePredicate` function passed to `spec` is an extension function on `CriteraiBuilder`. So when `equal(it, x)` is called from inside the `spec` block, it is invoking `CriteriaBuilder::equal`.


# TODO
- DSL for doing query + sort + paging
- Different modules for JPA / Spring Data common
- Publish to Maven / Github

# Contributing to the Project #

If you'd like to contribute code to this project you can do so through GitHub by forking the repository and generating a pull request.

By contributing your code, you agree to license your contribution under the terms of the Apache License v2.0. 

# License #

Copyright 2016 RES INFORMATION SERVICES PTY LTD

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.