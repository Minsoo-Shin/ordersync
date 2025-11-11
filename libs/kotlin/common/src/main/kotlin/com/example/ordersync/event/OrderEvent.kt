package com.example.ordersync.event

import java.time.Instant

sealed interface OrderEvent {
	val eventType: String
	val timestamp: Instant
}

data class OrderCreated(
	val orderId: Long,
	val items: List<OrderItemEvent>,
	override val timestamp: Instant = Instant.now(),
	override val eventType: String = "OrderCreated"
) : OrderEvent

data class OrderItemEvent(
	val productId: Long,
	val quantity: Int,
	val price: Long
)

