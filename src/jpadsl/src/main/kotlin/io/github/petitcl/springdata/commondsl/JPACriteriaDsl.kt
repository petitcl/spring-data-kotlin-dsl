package io.github.petitcl.springdata.commondsl

import jakarta.persistence.criteria.*
import kotlin.reflect.KProperty1

// Joining
fun <Z, T, R> From<Z, T>.join(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name)
fun <Z, T, R> From<Z, T>.leftJoin(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name, JoinType.LEFT)
fun <Z, T, R> From<Z, T>.innerJoin(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name, JoinType.INNER)
fun <Z, T, R> From<Z, T>.rightJoin(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name, JoinType.RIGHT)

// Helper to enable get by Property
fun <R> Path<*>.get(prop: KProperty1<*, R?>): Path<R> = this.get(prop.name)

// Combining predicates with null safety
fun CriteriaBuilder.andNotNull(vararg predicates: Predicate?): Predicate = this@CriteriaBuilder.and(*predicates.filterNotNull().toTypedArray())
fun CriteriaBuilder.orNotNull(vararg predicates: Predicate?): Predicate = this@CriteriaBuilder.or(*predicates.filterNotNull().toTypedArray())

// Equality
context(CriteriaBuilder)
fun <R> Path<R>.equal(value: R): Predicate = this@CriteriaBuilder.equal(this, value)

context(CriteriaBuilder)
fun <R> Path<R>.equal(expression: Expression<R?>): Predicate = this@CriteriaBuilder.equal(this, expression)

context(CriteriaBuilder)
fun <R> Path<R>.notEqual(value: R): Predicate = this@CriteriaBuilder.notEqual(this, value)

context(CriteriaBuilder)
fun <R> Path<R>.notEqual(expression: Expression<R?>): Predicate = this@CriteriaBuilder.notEqual(this, expression)

// Ignores empty collection otherwise an empty 'in' predicate will be generated which will never match any results
context(CriteriaBuilder)
fun <R> Path<R>.isIn(values: Collection<R>): Predicate = if (values.isNotEmpty()) {
    this@CriteriaBuilder.`in`(this).apply { values.forEach { this.value(it) } }
} else this@CriteriaBuilder.and()

context(CriteriaBuilder)
fun <R> Path<R>.isNotIn(values: Collection<R>): Predicate = if (values.isNotEmpty()) {
    this@CriteriaBuilder.`in`(this).apply { values.forEach { this.value(it) } }.not()
} else this@CriteriaBuilder.and()

// Comparison operators
context(CriteriaBuilder)
fun <R : Number> Path<R>.le(x: Number): Predicate = this@CriteriaBuilder.le(this, x)
context(CriteriaBuilder)
fun <R : Number> Path<R>.lt(x: Number): Predicate = this@CriteriaBuilder.lt(this, x)
context(CriteriaBuilder)
fun <R : Number> Path<R>.ge(x: Number): Predicate = this@CriteriaBuilder.ge(this, x)
context(CriteriaBuilder)
fun <R : Number> Path<R>.gt(x: Number): Predicate = this@CriteriaBuilder.gt(this, x)

context(CriteriaBuilder)
fun <R : Comparable<R>> Path<R>.lessThan(x: R) = this@CriteriaBuilder.lessThan(this, x)
context(CriteriaBuilder)
fun <R : Comparable<R>> Path<R>.lessThanOrEqualTo(x: R) = this@CriteriaBuilder.lessThanOrEqualTo(this, x)
context(CriteriaBuilder)
fun <R : Comparable<R>> Path<R>.greaterThan(x: R) = this@CriteriaBuilder.greaterThan(this, x)
context(CriteriaBuilder)
fun <R : Comparable<R>> Path<R>.greaterThanOrEqualTo(x: R) = this@CriteriaBuilder.greaterThanOrEqualTo(this, x)
context(CriteriaBuilder)
fun <R : Comparable<R>> Path<R>.between(x: R, y: R) = this@CriteriaBuilder.between(this, x, y)

// Booleans
context(CriteriaBuilder)
fun Path<Boolean>.isTrue() = this@CriteriaBuilder.isTrue(this)

context(CriteriaBuilder)
fun Path<Boolean>.isFalse() = this@CriteriaBuilder.isFalse(this)

// Strings
context(CriteriaBuilder)
fun Path<String>.like(x: String): Predicate = this@CriteriaBuilder.like(this, x)
context(CriteriaBuilder)
fun Path<String>.like(x: String, escapeChar: Char): Predicate = this@CriteriaBuilder.like(this, x, escapeChar)
context(CriteriaBuilder)
fun Path<String>.notLike(x: String): Predicate = this@CriteriaBuilder.notLike(this, x)
context(CriteriaBuilder)
fun Path<String>.notLike(x: String, escapeChar: Char): Predicate = this@CriteriaBuilder.notLike(this, x, escapeChar)
