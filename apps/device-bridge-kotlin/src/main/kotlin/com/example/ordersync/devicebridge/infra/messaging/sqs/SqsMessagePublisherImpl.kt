package com.example.ordersync.devicebridge.infra.messaging.sqs

import com.example.ordersync.sqs.SqsMessagePublisher
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

/**
 * SQS 메시지 발행의 범용 구현체
 * Infra 계층에 위치하며, Domain/Application 계층의 SqsMessagePublisher 인터페이스를 구현
 */
@Component
class SqsMessagePublisherImpl(
	private val sqsClient: SqsClient,
) : SqsMessagePublisher {
	companion object {
		private val logger = LoggerFactory.getLogger(SqsMessagePublisherImpl::class.java)
		private val objectMapper = ObjectMapper()
	}

	override fun publish(message: Any, queueUrl: String, delaySeconds: Int) {
		try {
			val messageBody = objectMapper.writeValueAsString(message)

			val request = SendMessageRequest.builder()
				.queueUrl(queueUrl)
				.messageBody(messageBody)
				.delaySeconds(minOf(delaySeconds, 900)) // SQS 최대 지연은 15분
				.build()

			sqsClient.sendMessage(request)

			logger.debug("Published message to SQS: queueUrl=$queueUrl, delaySeconds=$delaySeconds")
		} catch (e: Exception) {
			logger.error("Failed to publish message to SQS: queueUrl=$queueUrl", e)
			throw e
		}
	}
}
