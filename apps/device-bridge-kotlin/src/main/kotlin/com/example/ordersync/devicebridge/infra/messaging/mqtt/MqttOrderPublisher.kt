package com.example.ordersync.devicebridge.infra.messaging.mqtt

import com.example.ordersync.devicebridge.domain.orderdevicesync.MqttMessagePublisher
import com.example.ordersync.event.OrderCreated
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MqttOrderPublisher(
	private val mqttClient: MqttClient,

	@Value("\${mqtt.topic.orders:orders/device}")
	private val ordersTopic: String,

	@Value("\${mqtt.qos:1}")
	private val qos: Int,
) : MqttMessagePublisher {
	
	companion object {
		private val logger = LoggerFactory.getLogger(MqttOrderPublisher::class.java)
		private val objectMapper = ObjectMapper()
	}

	override fun publish(orderId: Long, event: OrderCreated): Boolean {
		return try {
			// MQTT 연결 상태 확인
			if (!mqttClient.isConnected) {
				logger.warn("MQTT client not connected. MQTT broker might not be available.")
				throw IllegalStateException("MQTT client is not connected")
			}

			val messageJson = objectMapper.writeValueAsString(
				mapOf(
					"orderId" to event.orderId,
					"items" to event.items,
					"timestamp" to event.timestamp.toString(),
				)
			)

			val mqttMessage = MqttMessage(messageJson.toByteArray()).apply {
				this.qos = this@MqttOrderPublisher.qos
				isRetained = false
			}

			val topic = "$ordersTopic/$orderId"
			mqttClient.publish(topic, mqttMessage)

			logger.info("Published order to MQTT: topic=$topic, orderId=$orderId")
			true
		} catch (e: Exception) {
			logger.error("Failed to publish order to MQTT: orderId=$orderId", e)
			false
		}
	}
}

