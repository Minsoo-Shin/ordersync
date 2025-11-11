package com.example.ordersync.devicebridge.infra.persistence.orderdevicesync

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSync

object OrderDeviceSyncMapper {
	fun toEntity(orderDeviceSync: OrderDeviceSync): JpaOrderDeviceSyncEntity {
		return JpaOrderDeviceSyncEntity(
			id = orderDeviceSync.id,
			orderId = orderDeviceSync.orderId,
			eventId = orderDeviceSync.eventId,
			status = orderDeviceSync.status,
			mqttSentAt = orderDeviceSync.mqttSentAt,
			webhookConfirmedAt = orderDeviceSync.webhookConfirmedAt,
			lastCheckedAt = orderDeviceSync.lastCheckedAt,
			retryCount = orderDeviceSync.retryCount,
			retryStartTime = orderDeviceSync.retryStartTime,
			createdAt = orderDeviceSync.createdAt,
		)
	}

	fun toDomain(entity: JpaOrderDeviceSyncEntity): OrderDeviceSync {
		return OrderDeviceSync.restore(
			id = entity.id,
			orderId = entity.orderId,
			eventId = entity.eventId,
			status = entity.status,
			mqttSentAt = entity.mqttSentAt,
			webhookConfirmedAt = entity.webhookConfirmedAt,
			lastCheckedAt = entity.lastCheckedAt,
			retryCount = entity.retryCount,
			retryStartTime = entity.retryStartTime,
			createdAt = entity.createdAt,
		)
	}
}



