package com.example.ordersync.devicebridge.domain.orderdevicesync

import java.time.Instant
import java.util.UUID

data class OrderDeviceSync(
	val id: Long?,
	val orderId: Long,
	val eventId: String,
	val status: OrderSyncStatus,
	val mqttSentAt: Instant?,
	val webhookConfirmedAt: Instant?,
	val lastCheckedAt: Instant?,
	val retryCount: Int,
	val retryStartTime: Instant?,
	val createdAt: Instant,
) {
	companion object {
		fun create(
			orderId: Long,
			eventId: String = UUID.randomUUID().toString(),
		): OrderDeviceSync {
			return OrderDeviceSync(
				id = null,
				orderId = orderId,
				eventId = eventId,
				status = OrderSyncStatus.PENDING,
				mqttSentAt = null,
				webhookConfirmedAt = null,
				lastCheckedAt = null,
				retryCount = 0,
				retryStartTime = null,
				createdAt = Instant.now(),
			)
		}

		fun restore(
			id: Long?,
			orderId: Long,
			eventId: String,
			status: OrderSyncStatus,
			mqttSentAt: Instant?,
			webhookConfirmedAt: Instant?,
			lastCheckedAt: Instant?,
			retryCount: Int,
			retryStartTime: Instant?,
			createdAt: Instant,
		): OrderDeviceSync {
			return OrderDeviceSync(
				id = id,
				orderId = orderId,
				eventId = eventId,
				status = status,
				mqttSentAt = mqttSentAt,
				webhookConfirmedAt = webhookConfirmedAt,
				lastCheckedAt = lastCheckedAt,
				retryCount = retryCount,
				retryStartTime = retryStartTime,
				createdAt = createdAt,
			)
		}
	}

	fun markAsSent(sentAt: Instant = Instant.now()): OrderDeviceSync {
		return this.copy(
			status = OrderSyncStatus.SENT,
			mqttSentAt = sentAt,
		)
	}

	fun markAsConfirmed(confirmedAt: Instant = Instant.now()): OrderDeviceSync {
		return this.copy(
			status = OrderSyncStatus.CONFIRMED,
			webhookConfirmedAt = confirmedAt,
		)
	}

	fun markAsFailed(): OrderDeviceSync {
		val now = Instant.now()
		return this.copy(
			status = OrderSyncStatus.FAILED,
			retryCount = this.retryCount + 1,
			retryStartTime = this.retryStartTime ?: now, // 첫 실패 시에만 설정
			lastCheckedAt = now,
		)
	}

	fun markAsFinallyFailed(): OrderDeviceSync {
		return this.copy(
			status = OrderSyncStatus.FINALLY_FAILED,
			lastCheckedAt = Instant.now(),
		)
	}

	fun incrementRetry(): OrderDeviceSync {
		return this.copy(
			retryCount = this.retryCount + 1,
			lastCheckedAt = Instant.now(),
		)
	}

	fun updateLastChecked(checkedAt: Instant = Instant.now()): OrderDeviceSync {
		return this.copy(
			lastCheckedAt = checkedAt,
		)
	}
}



