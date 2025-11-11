package com.example.ordersync.devicebridge.infra.messaging.sqs

import com.example.ordersync.devicebridge.domain.orderdevicesync.ProcessOrderSyncCheckEventUseCase
import com.example.ordersync.event.OrderSyncCheckEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderSyncCheckerConsumer(
	private val processOrderSyncCheckEventUseCase: ProcessOrderSyncCheckEventUseCase,
) {
	companion object {
		private val logger = LoggerFactory.getLogger(OrderSyncCheckerConsumer::class.java)
		private val objectMapper = ObjectMapper()
	}

	@SqsListener("\${aws.sqs.order-sync-check-queue-url}")
	fun receiveMessage(payload: String) {
		logger.info("Order Sync Check Event received: $payload")
		try {
			val event: OrderSyncCheckEvent = objectMapper.readValue(payload, OrderSyncCheckEvent::class.java)
			processOrderSyncCheckEventUseCase.process(event)
		} catch (e: Exception) {
			logger.error("Failed to process OrderSyncCheckEvent: $payload", e)
			throw e // DLQ로 전송되도록 예외 재발생
		}
	}
}
