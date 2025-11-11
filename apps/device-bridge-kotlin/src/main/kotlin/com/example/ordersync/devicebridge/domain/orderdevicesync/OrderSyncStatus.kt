package com.example.ordersync.devicebridge.domain.orderdevicesync

enum class OrderSyncStatus {
	PENDING,        // Kafka 이벤트 수신 후, MQTT 전송 전
	SENT,           // MQTT 전송 완료, 웹훅 확인 대기
	CONFIRMED,      // 웹훅으로 확인 완료
	FAILED,         // 전송 실패 또는 검증 실패 (재시도 가능)
	FINALLY_FAILED  // 최대 재시도 횟수 초과로 더 이상 재시도 불가능한 최종 실패 상태
}



