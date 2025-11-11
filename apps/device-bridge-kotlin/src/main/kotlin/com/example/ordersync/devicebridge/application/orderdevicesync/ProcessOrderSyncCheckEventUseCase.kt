package com.example.ordersync.devicebridge.application.orderdevicesync

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSyncChecker
import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSyncRepository
import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderSyncStatus
import com.example.ordersync.devicebridge.domain.orderdevicesync.ProcessOrderSyncCheckEventUseCase
import com.example.ordersync.devicebridge.domain.orderdevicesync.RetryDelayCalculator
import com.example.ordersync.event.OrderSyncCheckEvent
import com.example.ordersync.sqs.SqsMessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessOrderSyncCheckEventService(
	private val orderDeviceSyncRepository: OrderDeviceSyncRepository,
	private val sqsMessagePublisher: SqsMessagePublisher,

	@Value("\${sqs.checker.max-retry-time-hours:6}")
	private val maxRetryTimeHours: Long,

	@Value("\${sqs.checker.max-retry-count:3}")
	private val maxRetryCount: Int,

	@Value("\${sqs.checker.pending-threshold-seconds:30}")
	private val pendingThresholdSeconds: Long,

	@Value("\${sqs.checker.delay.seconds:30}")
	private val baseDelaySeconds: Int,

	@Value("\${aws.sqs.order-sync-check-queue-url}")
	private val orderSyncCheckQueueUrl: String,
) : ProcessOrderSyncCheckEventUseCase {

	companion object {
		private val logger = LoggerFactory.getLogger(ProcessOrderSyncCheckEventService::class.java)
	}

	private val retryDelayCalculator = RetryDelayCalculator(baseDelaySeconds)
	private val orderDeviceSyncChecker = OrderDeviceSyncChecker(
		maxRetryTimeHours = maxRetryTimeHours,
		maxRetryCount = maxRetryCount,
		pendingThresholdSeconds = pendingThresholdSeconds,
	)

	@Transactional
	override fun process(event: OrderSyncCheckEvent) {
		val orderDeviceSync = orderDeviceSyncRepository.findByOrderId(event.orderId)
		if (orderDeviceSync == null) {
			logger.warn("OrderDeviceSync not found: orderId=${event.orderId}")
			return
		}

		val expectedStatus = try {
			OrderSyncStatus.valueOf(event.expectedStatus)
		} catch (e: IllegalArgumentException) {
			logger.error("Invalid expectedStatus in event: ${event.expectedStatus}")
			return
		}

		// 도메인 서비스를 통한 상태 검증
		val result = orderDeviceSyncChecker.checkStatus(orderDeviceSync, expectedStatus)

		// 결과에 따른 처리
		when (result) {
			is OrderDeviceSyncChecker.CheckResult.AlreadyConfirmed -> {
				logger.info("Order already confirmed: orderId=${event.orderId}")
			}

			is OrderDeviceSyncChecker.CheckResult.AlreadyFailed -> {
				logger.info("Order already in FINALLY_FAILED state: orderId=${event.orderId}")
			}

			is OrderDeviceSyncChecker.CheckResult.Waiting -> {
				// 추가 액션 없음
			}

			is OrderDeviceSyncChecker.CheckResult.ShouldRecheck -> {
				// 아직 대기 중이면 다시 체크 이벤트 발행
				val checkEvent = OrderSyncCheckEvent(
					orderId = event.orderId,
					expectedStatus = result.expectedStatus.name,
					retryAttempt = 0,
				)
				val delaySeconds = retryDelayCalculator.calculateInitialDelay()
				sqsMessagePublisher.publish(checkEvent, orderSyncCheckQueueUrl, delaySeconds)
				logger.info("Published recheck OrderSyncCheckEvent to SQS: orderId=${event.orderId}, delaySeconds=$delaySeconds")
			}

			is OrderDeviceSyncChecker.CheckResult.ShouldRetry -> {
				// 상태 업데이트 저장
				orderDeviceSyncRepository.save(result.updated)

				// 재시도 이벤트 발행
				val retryAttempt = result.retryAttempt ?: result.updated.retryCount
				logger.info("Retrying failed order: orderId=${event.orderId}, retryAttempt=$retryAttempt")

				val retryEvent = OrderSyncCheckEvent(
					orderId = event.orderId,
					expectedStatus = result.expectedStatus.name,
					retryAttempt = retryAttempt,
				)
				val delaySeconds = retryDelayCalculator.calculateRetryDelay(retryAttempt)
				sqsMessagePublisher.publish(retryEvent, orderSyncCheckQueueUrl, delaySeconds)
				logger.info("Published retry OrderSyncCheckEvent to SQS: orderId=${event.orderId}, retryAttempt=$retryAttempt, delaySeconds=$delaySeconds")
			}

			is OrderDeviceSyncChecker.CheckResult.Timeout -> {
				logger.warn("재시도 시간이 ${maxRetryTimeHours}시간을 초과하여 더 이상 처리하지 않습니다. orderId=${event.orderId}")
				orderDeviceSyncRepository.save(result.updated)
			}

			is OrderDeviceSyncChecker.CheckResult.ExceededMaxRetry -> {
				logger.error("Order exceeded max retry count: orderId=${event.orderId}, retryCount=${result.updated.retryCount}, marking as FINALLY_FAILED")
				orderDeviceSyncRepository.save(result.updated)
			}
		}
	}
}

