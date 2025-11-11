package com.example.ordersync.devicebridge.infra.messaging.kafka

import com.example.ordersync.devicebridge.domain.orderdevicesync.ProcessOrderCreatedEventUseCase
import com.example.ordersync.event.OrderCreated
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderConsumer(
	private val processOrderCreatedEventUseCase: ProcessOrderCreatedEventUseCase,
) {
	companion object {
		private val logger = LoggerFactory.getLogger(OrderConsumer::class.java)
		private val objectMapper = ObjectMapper()
	}

	@KafkaListener(topics = ["orders"], groupId = "device-bridge-group")
	fun consumeOrder(message: String) {
		try {
			val event: OrderCreated = objectMapper.readValue(message, OrderCreated::class.java)
			processOrderCreatedEventUseCase.process(event)
		} catch (e: Exception) {
			logger.error("Failed to process order message: $message", e)
			throw e // 재시도를 위해 예외 재발생
		}
	}
}

