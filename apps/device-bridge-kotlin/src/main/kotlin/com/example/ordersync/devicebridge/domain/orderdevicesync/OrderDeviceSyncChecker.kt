package com.example.ordersync.devicebridge.domain.orderdevicesync

import java.time.Duration
import java.time.Instant

/**
 * OrderDeviceSync 상태 검증 및 전이를 담당하는 도메인 서비스
 */
class OrderDeviceSyncChecker(
	private val maxRetryTimeHours: Long,
	private val maxRetryCount: Int,
	private val pendingThresholdSeconds: Long,
) {
	/**
	 * OrderDeviceSync 상태를 검증하고 필요한 상태 전이를 수행
	 * @return CheckResult - 검증 결과 및 필요한 액션
	 */
	fun checkStatus(
		orderDeviceSync: OrderDeviceSync,
		expectedStatus: OrderSyncStatus,
		now: Instant = Instant.now(),
	): CheckResult {
		// 재시도 시작 시간 기준으로 시간 제한 체크
		if (orderDeviceSync.retryStartTime != null) {
			val retryDuration = Duration.between(orderDeviceSync.retryStartTime, now)
			if (retryDuration.toHours() >= maxRetryTimeHours) {
				return CheckResult.Timeout(
					updated = orderDeviceSync.markAsFinallyFailed(),
				)
			}
		}

		// 상태별 처리
		return when (orderDeviceSync.status) {
			// CONFIRMED 상태면 완료
			OrderSyncStatus.CONFIRMED -> CheckResult.AlreadyConfirmed

			// PENDING 상태이고 생성된 지 threshold 이상 지났으면 실패로 간주
			OrderSyncStatus.PENDING -> {
				val elapsed = Duration.between(orderDeviceSync.createdAt, now).seconds
				if (elapsed >= pendingThresholdSeconds) {
					CheckResult.ShouldRetry(
						updated = orderDeviceSync.markAsFailed(),
						expectedStatus = expectedStatus,
					)
				} else {
					CheckResult.Waiting
				}
			}

			// SENT 상태인데 웹훅이 오지 않았으면 재확인
			OrderSyncStatus.SENT -> {
				val mqttSentTime = orderDeviceSync.mqttSentAt
				if (mqttSentTime != null && Duration.between(mqttSentTime, now).seconds >= pendingThresholdSeconds) {
					CheckResult.ShouldRetry(
						updated = orderDeviceSync.markAsFailed(),
						expectedStatus = expectedStatus,
					)
				} else {
					CheckResult.ShouldRecheck(
						expectedStatus = expectedStatus,
					)
				}
			}

			// FAILED 상태면 재시도
			OrderSyncStatus.FAILED -> {
				if (orderDeviceSync.retryCount < maxRetryCount) {
					CheckResult.ShouldRetry(
						updated = orderDeviceSync,
						retryAttempt = orderDeviceSync.retryCount + 1,
						expectedStatus = expectedStatus,
					)
				} else {
					CheckResult.ExceededMaxRetry(
						updated = orderDeviceSync.markAsFinallyFailed(),
					)
				}
			}

			// FINALLY_FAILED 상태면 더 이상 처리하지 않음
			OrderSyncStatus.FINALLY_FAILED -> CheckResult.AlreadyFailed
		}
	}

	/**
	 * 상태 검증 결과
	 */
	sealed class CheckResult {
		/** 이미 확인된 상태 */
		object AlreadyConfirmed : CheckResult()

		/** 이미 최종 실패 상태 */
		object AlreadyFailed : CheckResult()

		/** 아직 대기 중 (추가 액션 없음) */
		object Waiting : CheckResult()

		/** 다시 체크 이벤트 발행 필요 */
		data class ShouldRecheck(
			val expectedStatus: OrderSyncStatus,
		) : CheckResult()

		/** 재시도 필요 */
		data class ShouldRetry(
			val updated: OrderDeviceSync,
			val retryAttempt: Int? = null,
			val expectedStatus: OrderSyncStatus,
		) : CheckResult()

		/** 시간 초과로 최종 실패 */
		data class Timeout(
			val updated: OrderDeviceSync,
		) : CheckResult()

		/** 최대 재시도 횟수 초과 */
		data class ExceededMaxRetry(
			val updated: OrderDeviceSync,
		) : CheckResult()
	}
}

