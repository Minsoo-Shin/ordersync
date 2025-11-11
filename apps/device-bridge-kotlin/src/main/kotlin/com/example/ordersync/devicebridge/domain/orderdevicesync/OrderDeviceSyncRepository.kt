package com.example.ordersync.devicebridge.domain.orderdevicesync

interface OrderDeviceSyncRepository {
	fun save(orderDeviceSync: OrderDeviceSync): OrderDeviceSync
	fun findByOrderId(orderId: Long): OrderDeviceSync?
	fun findByEventId(eventId: String): OrderDeviceSync?
	fun findPendingOrdersCreatedBefore(before: java.time.Instant): List<OrderDeviceSync>
	fun findFailedOrders(maxRetryCount: Int): List<OrderDeviceSync>
	fun findFinallyFailedOrders(): List<OrderDeviceSync>
}



