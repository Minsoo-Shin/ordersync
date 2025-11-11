package com.example.ordersync.devicebridge.infra.persistence.orderdevicesync

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface SpringDataOrderDeviceSyncJpaRepository : JpaRepository<JpaOrderDeviceSyncEntity, Long> {
	fun findByOrderId(orderId: Long): JpaOrderDeviceSyncEntity?
	fun findByEventId(eventId: String): JpaOrderDeviceSyncEntity?

	@Query("SELECT e FROM JpaOrderDeviceSyncEntity e WHERE e.status = 'PENDING' AND e.createdAt < :before")
	fun findPendingOrdersCreatedBefore(@Param("before") before: Instant): List<JpaOrderDeviceSyncEntity>

	@Query("SELECT e FROM JpaOrderDeviceSyncEntity e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetryCount")
	fun findFailedOrdersWithRetryCountLessThan(@Param("maxRetryCount") maxRetryCount: Int): List<JpaOrderDeviceSyncEntity>

	@Query("SELECT e FROM JpaOrderDeviceSyncEntity e WHERE e.status = 'FINALLY_FAILED'")
	fun findFinallyFailedOrders(): List<JpaOrderDeviceSyncEntity>
}



