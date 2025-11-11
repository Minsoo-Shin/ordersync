package com.example.ordersync.event

import java.time.Instant

data class OrderSyncCheckEvent(
	val orderId: Long,
	val expectedStatus: String,  // OrderSyncStatus를 String으로 전달 (PENDING, SENT, CONFIRMED, FAILED)
	val retryAttempt: Int = 0,
	val timestamp: Instant = Instant.now(),
	val eventType: String = "OrderSyncCheck"
)

