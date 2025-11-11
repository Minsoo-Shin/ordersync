package com.example.ordersync.devicebridge.application.orderdevicesync

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSync
import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSyncRepository
import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderSyncStatus
import com.example.ordersync.devicebridge.domain.orderdevicesync.ProcessOrderCreatedEventUseCase
import com.example.ordersync.devicebridge.domain.orderdevicesync.PublishOrderToMqttUseCase
import com.example.ordersync.devicebridge.domain.orderdevicesync.RetryDelayCalculator
import com.example.ordersync.event.OrderCreated
import com.example.ordersync.event.OrderSyncCheckEvent
import com.example.ordersync.sqs.SqsMessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessOrderCreatedEventService(
	private val orderDeviceSyncRepository: OrderDeviceSyncRepository,
	private val publishOrderToMqttUseCase: PublishOrderToMqttUseCase,
	private val sqsMessagePublisher: SqsMessagePublisher,

	@Value("\${sqs.checker.delay.seconds:30}")
	private val baseDelaySeconds: Int,

	@Value("\${aws.sqs.order-sync-check-queue-url}")
	private val orderSyncCheckQueueUrl: String,
) : ProcessOrderCreatedEventUseCase {

	companion object {
		private val logger = LoggerFactory.getLogger(ProcessOrderCreatedEventService::class.java)
	}

	private val retryDelayCalculator = RetryDelayCalculator(baseDelaySeconds)

	@Transactional
	override fun process(event: OrderCreated) {
		// 중복 방지: 이미 존재하는 orderId 체크
		val existingOrder = orderDeviceSyncRepository.findByOrderId(event.orderId)
		if (existingOrder != null) {
			logger.warn("Order already exists: orderId=${event.orderId}, skipping duplicate event")
			return
		}

		// OrderDeviceSync 생성 및 저장 (PENDING 상태)
		val orderDeviceSync = OrderDeviceSync.create(orderId = event.orderId)
		val saved = orderDeviceSyncRepository.save(orderDeviceSync)

		logger.info("Processed OrderCreated event: orderId=${event.orderId}, itemsCount=${event.items.size}, orderDeviceSyncId=${saved.id}")

		// MQTT 전송
		val mqttSuccess = publishOrderToMqttUseCase.publish(event.orderId, event)
		if (!mqttSuccess) {
			logger.warn("MQTT publish failed for orderId=${event.orderId}, will be retried by checker")
		}

		// SQS Checker Event 발행 (30초 후 검증)
		val expectedStatus = if (mqttSuccess) OrderSyncStatus.SENT else OrderSyncStatus.PENDING
		val checkEvent = OrderSyncCheckEvent(
			orderId = event.orderId,
			expectedStatus = expectedStatus.name,
			retryAttempt = 0,
		)
		val delaySeconds = retryDelayCalculator.calculateInitialDelay()
		sqsMessagePublisher.publish(checkEvent, orderSyncCheckQueueUrl, delaySeconds)
		
		logger.info("Published OrderSyncCheckEvent to SQS: orderId=${event.orderId}, retryAttempt=0, delaySeconds=$delaySeconds")
	}
}

