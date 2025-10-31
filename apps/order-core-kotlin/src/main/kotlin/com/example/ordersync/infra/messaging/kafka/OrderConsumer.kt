package com.example.ordersync.infra.messaging.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderConsumer {

	private val logger = LoggerFactory.getLogger(OrderConsumer::class.java)

	@KafkaListener(topics = ["orders"])
	fun consumeOrder(message: String) {
		logger.info("Received order message: $message")
		// TODO: 주문 처리 로직 구현
	}
}

