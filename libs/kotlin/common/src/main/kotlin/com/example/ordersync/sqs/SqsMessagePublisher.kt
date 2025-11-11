package com.example.ordersync.sqs

/**
 * SQS로 메시지를 발행하는 범용 인터페이스
 * Domain/Application 계층에서 사용하며, 구체적인 구현은 Infra 계층에서 제공
 */
interface SqsMessagePublisher {
	/**
	 * 객체를 JSON으로 직렬화하여 SQS에 메시지 발행
	 * @param message 발행할 메시지 객체 (Jackson으로 직렬화됨)
	 * @param queueUrl SQS 큐 URL
	 * @param delaySeconds 지연 시간 (초 단위, 최대 900초, 기본값 0)
	 */
	fun publish(message: Any, queueUrl: String, delaySeconds: Int = 0)
}
