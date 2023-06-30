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
fun <R> Expression<R>.equal(value: R): Predicate = builder.equal(this, value)

context(JPACriteriaDsl)
fun <R> Expression<R>.equal(expression: Expression<*>): Predicate = builder.equal(this, expression)

context(JPACriteriaDsl)
fun <R> Expression<R>.notEqual(value: R): Predicate = builder.notEqual(this, value)

context(JPACriteriaDsl)
fun <R> Expression<R>.notEqual(expression: Expression<*>): Predicate = builder.notEqual(this, expression)

// Ignores empty collection otherwise an empty 'in' predicate will be generated which will never match any results
context(JPACriteriaDsl)
fun <R> Expression<R>.isIn(values: Collection<R>): Predicate = if (values.isNotEmpty()) {
    builder.`in`(this).apply { values.forEach { this.value(it) } }
} else builder.and()

context(JPACriteriaDsl)
fun <R> Expression<R>.isNotIn(values: Collection<R>): Predicate = if (values.isNotEmpty()) {
    builder.`in`(this).apply { values.forEach { this.value(it) } }.not()
} else builder.and()

// Comparison operators
context(JPACriteriaDsl)
fun <R : Number> Expression<R>.le(x: Number): Predicate = builder.le(this, x)

context(JPACriteriaDsl)
fun <R : Number> Expression<R>.lt(x: Number): Predicate = builder.lt(this, x)

context(JPACriteriaDsl)
fun <R : Number> Expression<R>.ge(x: Number): Predicate = builder.ge(this, x)

context(JPACriteriaDsl)
fun <R : Number> Expression<R>.gt(x: Number): Predicate = builder.gt(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.lessThan(x: R): Predicate = builder.lessThan(this, x)
context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.lessThan(x: Expression<R>): Predicate = builder.lessThan(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.lessThanOrEqualTo(x: R): Predicate = builder.lessThanOrEqualTo(this, x)
context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.lessThanOrEqualTo(x: Expression<R>): Predicate = builder.lessThanOrEqualTo(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.greaterThan(x: R): Predicate = builder.greaterThan(this, x)
context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.greaterThan(x: Expression<R>): Predicate = builder.greaterThan(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.greaterThanOrEqualTo(x: R): Predicate = builder.greaterThanOrEqualTo(this, x)
context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.greaterThanOrEqualTo(x: Expression<R>): Predicate = builder.greaterThanOrEqualTo(this, x)

context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.between(x: R, y: R): Predicate = builder.between(this, x, y)
context(JPACriteriaDsl)
fun <R : Comparable<R>> Expression<R>.between(x: Expression<R>, y: Expression<R>): Predicate = builder.between(this, x, y)

// Booleans
context(JPACriteriaDsl)
fun Expression<Boolean>.isTrue(): Predicate = builder.isTrue(this)

context(JPACriteriaDsl)
fun Expression<Boolean>.isFalse(): Predicate = builder.isFalse(this)

// Strings
context(JPACriteriaDsl)
fun Expression<String>.like(x: String): Predicate = builder.like(this, x)

context(JPACriteriaDsl)
fun Expression<String>.like(x: String, escapeChar: Char): Predicate = builder.like(this, x, escapeChar)

context(JPACriteriaDsl)
fun Expression<String>.notLike(x: String): Predicate = builder.notLike(this, x)

context(JPACriteriaDsl)
fun Expression<String>.notLike(x: String, escapeChar: Char): Predicate = builder.notLike(this, x, escapeChar)
