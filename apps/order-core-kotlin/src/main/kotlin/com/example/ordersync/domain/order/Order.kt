package com.example.ordersync.domain.order

import java.time.Instant

data class Order(
    val id: Long?,
    val status: OrderStatus,
    val createdAt: Instant,
    val items: List<OrderItem>,
) {
    init {
        require(items.isNotEmpty()) { "Order must contain at least one item" }
    }
    companion object {
        fun create(items: List<OrderItem>): Order {
            require(items.isNotEmpty()) { "Order must contain at least one item" }
            return Order(
                id = null,
                status = OrderStatus.CREATED,
                createdAt = Instant.now(),
                items = items.toList(),
            )
        }

        fun restore(
            id: Long?,
            status: OrderStatus,
            createdAt: Instant,
            items: List<OrderItem>,
        ): Order {
            return Order(
                id = id,
                status = status,
                createdAt = createdAt,
                items = items,
            )
        }
    }
}

enum class OrderStatus {
    CREATED,
    CANCELED,
}


