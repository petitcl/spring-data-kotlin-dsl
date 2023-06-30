package io.github.petitcl.springdata.commondsl

import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.*
import kotlin.reflect.KProperty1

interface SpringJPASpecificationDSL<T> : JPACriteriaDsl {
    val root: Root<T>
}

// Version of Specification.where that makes the CriteriaBuilder implicit
fun <T> where(makePredicate: SpringJPASpecificationDSL<T>.() -> Predicate): Specification<T> =
    Specification { root, query, criteriaBuilder ->
        SpringJPASpecificationDSLImpl(root, query, criteriaBuilder).makePredicate()
    }

private data class SpringJPASpecificationDSLImpl<T>(
    override val root: Root<T>,
    override val query: CriteriaQuery<*>,
    override val builder: CriteriaBuilder
) : SpringJPASpecificationDSL<T>

// helper function for defining Specification that take a Path to a property and send it to a CriteriaBuilder
private fun <T, R> KProperty1<T, R?>.spec(makePredicate: SpringJPASpecificationDSL<T>.(path: Path<R>) -> Predicate): Specification<T> =
    let { property -> where { makePredicate(root.get(property)) } }

// Equality
fun <T, R> KProperty1<T, R?>.equal(x: R): Specification<T> = spec { builder.equal(it, x) }

fun <T, R> KProperty1<T, R?>.notEqual(x: R): Specification<T> = spec { builder.notEqual(it, x) }

// Ignores empty collection otherwise an empty 'in' predicate will be generated which will never match any results
fun <T, R> KProperty1<T, R?>.isIn(values: Collection<R>): Specification<T> =
    if (values.isNotEmpty()) spec { path ->
        builder.`in`(path).apply { values.forEach { this.value(it) } }
    } else Specification.where(null)

fun <T, R> KProperty1<T, R?>.isNotIn(values: Collection<R>): Specification<T> =
    if (values.isNotEmpty()) spec { path ->
        builder.`in`(path).apply { values.forEach { this.value(it) } }.not()
    } else Specification.where(null)

// Comparison
fun <T> KProperty1<T, Number?>.le(x: Number): Specification<T> = spec { builder.le(it, x) }
fun <T> KProperty1<T, Number?>.lt(x: Number): Specification<T> = spec { builder.lt(it, x) }
fun <T> KProperty1<T, Number?>.ge(x: Number): Specification<T> = spec { builder.ge(it, x) }
fun <T> KProperty1<T, Number?>.gt(x: Number): Specification<T> = spec { builder.gt(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.lessThan(x: R): Specification<T> = spec { builder.lessThan(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.lessThanOrEqualTo(x: R): Specification<T> =
    spec { builder.lessThanOrEqualTo(it, x) }

fun <T, R : Comparable<R>> KProperty1<T, R?>.greaterThan(x: R): Specification<T> = spec { builder.greaterThan(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.greaterThanOrEqualTo(x: R): Specification<T> =
    spec { builder.greaterThanOrEqualTo(it, x) }

fun <T, R : Comparable<R>> KProperty1<T, R?>.between(x: R, y: R): Specification<T> = spec { builder.between(it, x, y) }

// True/False
fun <T> KProperty1<T, Boolean?>.isTrue(): Specification<T> = spec { builder.isTrue(it) }
fun <T> KProperty1<T, Boolean?>.isFalse(): Specification<T> = spec { builder.isFalse(it) }

// Null / NotNull
fun <T, R> KProperty1<T, R?>.isNull(): Specification<T> = spec { builder.isNull(it) }
fun <T, R> KProperty1<T, R?>.isNotNull(): Specification<T> = spec { builder.isNotNull(it) }

// Collections
fun <T, R : Collection<*>> KProperty1<T, R?>.isEmpty(): Specification<T> = spec { builder.isEmpty(it) }
fun <T, R : Collection<*>> KProperty1<T, R?>.isNotEmpty(): Specification<T> = spec { builder.isNotEmpty(it) }
fun <T, E, R : Collection<E>> KProperty1<T, R?>.hasMember(elem: E): Specification<T> = spec { builder.isMember(elem, it) }
fun <T, E, R : Collection<E>> KProperty1<T, R?>.hasNoMember(elem: E): Specification<T> = spec { builder.isNotMember(elem, it) }


// Strings
fun <T> KProperty1<T, String?>.like(x: String): Specification<T> = spec { builder.like(it, x) }
fun <T> KProperty1<T, String?>.like(x: String, escapeChar: Char): Specification<T> = spec { builder.like(it, x, escapeChar) }
fun <T> KProperty1<T, String?>.notLike(x: String): Specification<T> = spec { builder.notLike(it, x) }
fun <T> KProperty1<T, String?>.notLike(x: String, escapeChar: Char): Specification<T> =
    spec { builder.notLike(it, x, escapeChar) }

// And
infix fun <T> Specification<T>.and(other: Specification<T>): Specification<T> = this.and(other)

inline fun <reified T> and(vararg specs: Specification<T>?): Specification<T> {
    return and(specs.toList())
}

inline fun <reified T> and(specs: Iterable<Specification<T>?>): Specification<T> {
    return combineSpecification(specs, Specification<T>::and)
}

// Or
infix fun <T> Specification<T>.or(other: Specification<T>): Specification<T> = this.or(other)

inline fun <reified T> or(vararg specs: Specification<T>?): Specification<T> {
    return or(specs.toList())
}

inline fun <reified T> or(specs: Iterable<Specification<T>?>): Specification<T> {
    return combineSpecification(specs, Specification<T>::or)
}

// Not
operator fun <T> Specification<T>.not(): Specification<T> = Specification.not(this)

// Combines Specification with an operation
inline fun <reified T> combineSpecification(
    specs: Iterable<Specification<T>?>,
    operation: Specification<T>.(Specification<T>) -> Specification<T>
): Specification<T> {
    return specs.filterNotNull().fold(emptySpecification()) { existing, new -> existing.operation(new) }
}

// Empty Specification
inline fun <reified T> emptySpecification(): Specification<T> = Specification.where(null)
