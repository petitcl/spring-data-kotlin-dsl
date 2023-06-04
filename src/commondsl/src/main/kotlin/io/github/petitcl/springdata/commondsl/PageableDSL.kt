package io.github.petitcl.springdata.commondsl

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.reflect.KProperty1

// Entry points of the pageable DSL
fun paged(page: Int, size: Int): PageRequest = PageRequest.of(page, size)

fun limit(size: Int): PageRequest = PageRequest.of(0, size)

//  for convenience
fun unpaged(): Pageable = Pageable.unpaged()


// Entry points of the sort DSL
fun sortedBy(vararg orders: Sort.Order): Sort = Sort.by(orders.toList())

fun sortedBy(vararg properties: KProperty1<*, *>): Sort {
    val propertyNames = properties.map { it.name }
    return Sort.by(*propertyNames.toTypedArray())
}

// For convenience
fun unsorted(): Sort = Sort.unsorted()

// These extension functions allow to page then sort
fun PageRequest.sortedBy(sort: Sort): PageRequest = this.withSort(sort)

fun PageRequest.sortedBy(vararg orders: Sort.Order): PageRequest
    = this.withSort(io.github.petitcl.springdata.commondsl.sortedBy(*orders))

fun PageRequest.sortedBy(vararg properties: KProperty1<*, *>): PageRequest
    = this.withSort(io.github.petitcl.springdata.commondsl.sortedBy(*properties))


// These extension functions allow to sort then page
fun Sort.paged(page: Int, size: Int): PageRequest = PageRequest.of(page, size, this)
fun Sort.limit(size: Int): PageRequest = PageRequest.of(0, size, this)


// The following extension functions allow to use a property like a Sort.Order
fun KProperty1<*, *>.asc(): Sort.Order = Sort.Order(Sort.Direction.ASC, this.name)
fun KProperty1<*, *>.ascending(): Sort.Order = this.asc()

fun KProperty1<*, *>.desc(): Sort.Order = Sort.Order(Sort.Direction.DESC, this.name)
fun KProperty1<*, *>.descending(): Sort.Order = this.desc()

fun KProperty1<*, *>.ignoreCase(): Sort.Order = Sort.Order.by(this.name).ignoreCase()

fun KProperty1<*, *>.nullsFirst(): Sort.Order = Sort.Order.by(this.name).nullsFirst()

fun KProperty1<*, *>.nullsLast(): Sort.Order = Sort.Order.by(this.name).nullsLast()

fun KProperty1<*, *>.nullsNative(): Sort.Order = Sort.Order.by(this.name).nullsNative()


// the following infix functions allow to combine fields and orders to create sorts
internal fun by(property: KProperty1<*, *>): Sort.Order = Sort.Order.by(property.name)

infix fun KProperty1<*, *>.andThen(other: KProperty1<*, *>): Sort = Sort.by(this.name, other.name)

infix fun KProperty1<*, *>.andThen(other: Sort.Order): Sort = Sort.by(by(this), other)

infix fun Sort.Order.andThen(other: Sort.Order): Sort = Sort.by(this, other)

infix fun Sort.Order.andThen(other: KProperty1<*, *>): Sort = Sort.by(this, by(other))

infix fun Sort.andThen(other: KProperty1<*, *>): Sort = this andThen by(other)

infix fun Sort.andThen(sort: Sort): Sort = this.and(sort)

infix fun Sort.andThen(order: Sort.Order): Sort = this.and(Sort.by(order))
