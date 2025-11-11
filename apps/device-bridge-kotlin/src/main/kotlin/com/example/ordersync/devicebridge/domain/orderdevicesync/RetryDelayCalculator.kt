package com.example.ordersync.devicebridge.domain.orderdevicesync

/**
 * 재시도 지연 시간을 계산하는 도메인 서비스
 */
class RetryDelayCalculator(
	private val baseDelaySeconds: Int,
	private val maxDelaySeconds: Int = 900, // SQS 최대 지연은 15분
) {
	/**
	 * 초기 체크 이벤트의 지연 시간을 반환
	 */
	fun calculateInitialDelay(): Int {
		return baseDelaySeconds
	}

	/**
	 * 재시도 이벤트의 지연 시간을 계산 (지수 백오프 적용)
	 * @param retryAttempt 재시도 횟수 (0부터 시작)
	 * @return 계산된 지연 시간 (초 단위, 최대 maxDelaySeconds)
	 */
	fun calculateRetryDelay(retryAttempt: Int): Int {
		// 지수 백오프: baseDelaySeconds * 2^retryAttempt
		val calculatedDelay = (baseDelaySeconds * (1 shl retryAttempt)).toLong()
		return minOf(calculatedDelay.toInt(), maxDelaySeconds)
	}
}

