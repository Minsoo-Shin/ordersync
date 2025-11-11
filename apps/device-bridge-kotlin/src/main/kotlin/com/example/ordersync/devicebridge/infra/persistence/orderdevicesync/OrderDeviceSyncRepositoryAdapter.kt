package com.example.ordersync.devicebridge.infra.persistence.orderdevicesync

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSync
import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSyncRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
class OrderDeviceSyncRepositoryAdapter(
	private val jpaRepository: SpringDataOrderDeviceSyncJpaRepository,
) : OrderDeviceSyncRepository {

	@Transactional
	override fun save(orderDeviceSync: OrderDeviceSync): OrderDeviceSync {
		val entity = OrderDeviceSyncMapper.toEntity(orderDeviceSync)
		val saved = jpaRepository.save(entity)
		return OrderDeviceSyncMapper.toDomain(saved)
	}

	@Transactional(readOnly = true)
	override fun findByOrderId(orderId: Long): OrderDeviceSync? {
		val entity = jpaRepository.findByOrderId(orderId)
		return entity?.let { OrderDeviceSyncMapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findByEventId(eventId: String): OrderDeviceSync? {
		val entity = jpaRepository.findByEventId(eventId)
		return entity?.let { OrderDeviceSyncMapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findPendingOrdersCreatedBefore(before: Instant): List<OrderDeviceSync> {
		val entities = jpaRepository.findPendingOrdersCreatedBefore(before)
		return entities.map { OrderDeviceSyncMapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findFailedOrders(maxRetryCount: Int): List<OrderDeviceSync> {
		val entities = jpaRepository.findFailedOrdersWithRetryCountLessThan(maxRetryCount)
		return entities.map { OrderDeviceSyncMapper.toDomain(it) }
	}

	@Transactional(readOnly = true)
	override fun findFinallyFailedOrders(): List<OrderDeviceSync> {
		val entities = jpaRepository.findFinallyFailedOrders()
		return entities.map { OrderDeviceSyncMapper.toDomain(it) }
	}
}



