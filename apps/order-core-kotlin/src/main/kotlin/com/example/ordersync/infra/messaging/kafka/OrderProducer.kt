package com.example.ordersync.infra.messaging.kafka

import com.example.ordersync.event.OrderCreated
import com.example.ordersync.event.OrderItemEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderProducer(
	private val kafkaTemplate: KafkaTemplate<String, String>
) {
	companion object {
		private val objectMapper: ObjectMapper = jacksonObjectMapper()
			.registerModule(JavaTimeModule())
		private val logger = LoggerFactory.getLogger(OrderProducer::class.java)
		private const val TOPIC = "orders"
	}

	fun publishOrderCreated(orderId: Long, items: List<OrderItemEvent>) {
		val event = OrderCreated(
			orderId = orderId,
			items = items
		)
		val message = objectMapper.writeValueAsString(event)
		
		kafkaTemplate.send(TOPIC, message)
			.whenComplete { result, exception ->
				if (exception != null) {
					logger.error("Failed to send order created event for orderId: $orderId", exception)
				} else {
					logger.info("Published OrderCreated event: orderId=$orderId, partition=${result?.recordMetadata?.partition()}, offset=${result?.recordMetadata?.offset()}")
				}
			}
	}
}

