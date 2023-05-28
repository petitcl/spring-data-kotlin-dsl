package au.com.console.jpaspecificationdsl

import org.springframework.data.domain.Sort
import kotlin.reflect.KProperty1

fun sortedBy(vararg orders: Sort.Order): Sort = Sort.by(orders.toList())

fun sortedBy(vararg properties: KProperty1<*, *>): Sort {
    val propertyNames = properties.map { it.name }
    return Sort.by(*propertyNames.toTypedArray())
}

fun by(property: KProperty1<*, *>): Sort.Order = Sort.Order.by(property.name)

fun KProperty1<*, *>.asc(): Sort.Order = Sort.Order(Sort.Direction.ASC, this.name)

fun KProperty1<*, *>.desc(): Sort.Order = Sort.Order(Sort.Direction.DESC, this.name)

fun KProperty1<*, *>.ignoreCase(): Sort.Order = Sort.Order.by(this.name).ignoreCase()

fun KProperty1<*, *>.nullsFirst(): Sort.Order = Sort.Order.by(this.name).nullsFirst()

fun KProperty1<*, *>.nullsLast(): Sort.Order = Sort.Order.by(this.name).nullsLast()

fun KProperty1<*, *>.nullsNative(): Sort.Order = Sort.Order.by(this.name).nullsNative()

infix fun KProperty1<*, *>.andThen(other: KProperty1<*, *>): Sort = Sort.by(this.name, other.name)

infix fun KProperty1<*, *>.andThen(other: Sort.Order): Sort = Sort.by(by(this), other)

infix fun Sort.Order.andThen(other: Sort.Order): Sort = Sort.by(this, other)

infix fun Sort.Order.andThen(other: KProperty1<*, *>): Sort = Sort.by(this, by(other))
