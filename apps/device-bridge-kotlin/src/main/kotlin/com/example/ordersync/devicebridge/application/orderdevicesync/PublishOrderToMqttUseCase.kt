package com.example.ordersync.devicebridge.application.orderdevicesync

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSyncRepository
import com.example.ordersync.devicebridge.infra.messaging.mqtt.MqttMessagePublisher
import com.example.ordersync.event.OrderCreated
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface PublishOrderToMqttUseCase {
	fun publish(orderId: Long, event: OrderCreated): Boolean
}

@Service
class PublishOrderToMqttService(
	private val orderDeviceSyncRepository: OrderDeviceSyncRepository,
	private val mqttMessagePublisher: MqttMessagePublisher,
) : PublishOrderToMqttUseCase {

	companion object {
		private val logger = LoggerFactory.getLogger(PublishOrderToMqttService::class.java)
	}

	@Transactional
	override fun publish(orderId: Long, event: OrderCreated): Boolean {
		return try {
			// MQTT 메시지 발행
			val success = mqttMessagePublisher.publish(orderId, event)

			if (success) {
				// OrderDeviceSync 상태를 SENT로 업데이트
				val orderDeviceSync = orderDeviceSyncRepository.findByOrderId(orderId)
				if (orderDeviceSync != null) {
					val updated = orderDeviceSync.markAsSent(Instant.now())
					orderDeviceSyncRepository.save(updated)
					logger.info("Published order to MQTT: orderId=$orderId, status=SENT")
				}
			}

			success
		} catch (e: Exception) {
			logger.error("Failed to publish order to MQTT: orderId=$orderId", e)

			// OrderDeviceSync 상태를 FAILED로 업데이트
			val orderDeviceSync = orderDeviceSyncRepository.findByOrderId(orderId)
			if (orderDeviceSync != null) {
				val updated = orderDeviceSync.markAsFailed()
				orderDeviceSyncRepository.save(updated)
				logger.info("OrderDeviceSync marked as FAILED: orderId=$orderId")
			}

			false
		}
	}
}

