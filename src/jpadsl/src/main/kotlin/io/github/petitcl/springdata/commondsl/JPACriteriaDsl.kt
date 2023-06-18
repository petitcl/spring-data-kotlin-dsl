package io.github.petitcl.springdata.commondsl

import jakarta.persistence.criteria.*
import kotlin.reflect.KProperty1

interface JPACriteriaDsl {
    val builder: CriteriaBuilder
    val query: CriteriaQuery<*>
}

// Joining
fun <Z, T, R> From<Z, T>.join(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name)
fun <Z, T, R> From<Z, T>.leftJoin(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name, JoinType.LEFT)
fun <Z, T, R> From<Z, T>.innerJoin(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name, JoinType.INNER)
fun <Z, T, R> From<Z, T>.rightJoin(prop: KProperty1<T, R?>): Join<T, R> = this.join(prop.name, JoinType.RIGHT)

// Helper to enable get by Property
fun <R> Path<*>.get(prop: KProperty1<*, R?>): Path<R> = this.get(prop.name)

// Combining predicates with null safety
context(JPACriteriaDsl)
fun and(vararg predicates: Predicate?): Predicate = builder.and(*predicates.filterNotNull().toTypedArray())

context(JPACriteriaDsl)
fun or(vararg predicates: Predicate?): Predicate = builder.or(*predicates.filterNotNull().toTypedArray())

// Equality
context(JPACriteriaDsl)
fun <R> Path<R>.equal(value: R): Predicate = builder.equal(this, value)

context(JPACriteriaDsl)
fun <R> Path<R>.equal(expression: Expression<*>): Predicate = builder.equal(this, expression)

context(JPACriteriaDsl)
fun <R> Path<R>.notEqual(value: R): Predicate = builder.notEqual(this, value)

context(JPACriteriaDsl)
fun <R> Path<R>.notEqual(expression: Expression<R?>): Predicate = builder.notEqual(this, expression)

// Ignores empty collection otherwise an empty 'in' predicate will be generated which will never match any results
context(JPACriteriaDsl)
fun <R> Path<R>.isIn(values: Collection<R>): Predicate = if (values.isNotEmpty()) {
    builder.`in`(this).apply { values.forEach { this.value(it) } }
} else builder.and()

context(JPACriteriaDsl)
fun <R> Path<R>.isNotIn(values: Collection<R>): Predicate = if (values.isNotEmpty()) {
    builder.`in`(this).apply { values.forEach { this.value(it) } }.not()
} else builder.and()

// Comparison operators
context(JPACriteriaDsl)
fun <R : Number> Path<R>.le(x: Number): Predicate = builder.le(this, x)

context(JPACriteriaDsl)
fun <R : Number> Path<R>.lt(x: Number): Predicate = builder.lt(this, x)

context(JPACriteriaDsl)
fun <R : Number> Path<R>.ge(x: Number): Predicate = builder.ge(this, x)

context(JPACriteriaDsl)
fun <R : Number> Path<R>.gt(x: Number): Predicate = builder.gt(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Path<R>.lessThan(x: R): Predicate = builder.lessThan(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Path<R>.lessThanOrEqualTo(x: R): Predicate = builder.lessThanOrEqualTo(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Path<R>.greaterThan(x: R): Predicate = builder.greaterThan(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Path<R>.greaterThanOrEqualTo(x: R): Predicate = builder.greaterThanOrEqualTo(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Path<R>.between(x: R, y: R): Predicate = builder.between(this, x, y)

// Booleans
context(JPACriteriaDsl)
fun Path<Boolean>.isTrue(): Predicate = builder.isTrue(this)

context(JPACriteriaDsl)
fun Path<Boolean>.isFalse(): Predicate = builder.isFalse(this)

// Strings
context(JPACriteriaDsl)
fun Path<String>.like(x: String): Predicate = builder.like(this, x)

context(JPACriteriaDsl)
fun Path<String>.like(x: String, escapeChar: Char): Predicate = builder.like(this, x, escapeChar)

context(JPACriteriaDsl)
fun Path<String>.notLike(x: String): Predicate = builder.notLike(this, x)

context(JPACriteriaDsl)
fun Path<String>.notLike(x: String, escapeChar: Char): Predicate = builder.notLike(this, x, escapeChar)
