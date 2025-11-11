package com.example.ordersync.devicebridge.infra.messaging.mqtt

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
	prefix = "mqtt",
	name = ["enabled"],
	havingValue = "true",
	matchIfMissing = true
)
class MqttConfig(
	@Value("\${mqtt.broker.url:tcp://localhost:1883}")
	private val brokerUrl: String,

	@Value("\${mqtt.client.id:device-bridge-mqtt-client}")
	private val clientId: String,

	@Value("\${mqtt.username:}")
	private val username: String?,

	@Value("\${mqtt.password:}")
	private val password: String?,
) {
	companion object {
		private val logger = LoggerFactory.getLogger(MqttConfig::class.java)
	}

	@Bean(destroyMethod = "disconnect")
	fun mqttClient(): MqttClient {
		val client = MqttClient(brokerUrl, clientId)
		val options = MqttConnectOptions().apply {
			isAutomaticReconnect = true
			isCleanSession = false
			connectionTimeout = 30
			keepAliveInterval = 60
			this@MqttConfig.username?.let { userName = it }
			this@MqttConfig.password?.let { password = it.toCharArray() }
		}

		try {
			client.connect(options)
			logger.info("Connected to MQTT broker: $brokerUrl")
		} catch (e: Exception) {
			logger.error("Failed to connect to MQTT broker: $brokerUrl. Application will continue but MQTT features will be unavailable.", e)
			logger.warn("MQTT broker might not be running. Please ensure MQTT broker is started with: docker-compose up -d mqtt")
			// 연결 실패해도 클라이언트 객체는 반환하되, 사용 전에 연결 상태를 확인해야 함
		}

		return client
	}
}


