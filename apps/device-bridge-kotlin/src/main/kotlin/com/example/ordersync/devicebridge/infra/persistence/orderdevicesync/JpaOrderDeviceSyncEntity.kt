package com.example.ordersync.devicebridge.infra.persistence.orderdevicesync

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderSyncStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "order_device_syncs")
class JpaOrderDeviceSyncEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	var id: Long? = null,

	@Column(name = "order_id", nullable = false)
	var orderId: Long,

	@Column(name = "event_id", unique = true, nullable = false)
	var eventId: String,

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	var status: OrderSyncStatus,

	@Column(name = "mqtt_sent_at")
	var mqttSentAt: Instant? = null,

	@Column(name = "webhook_confirmed_at")
	var webhookConfirmedAt: Instant? = null,

	@Column(name = "last_checked_at")
	var lastCheckedAt: Instant? = null,

	@Column(name = "retry_count", nullable = false)
	var retryCount: Int = 0,

	@Column(name = "retry_start_time")
	var retryStartTime: Instant? = null,

	@Column(name = "created_at", nullable = false)
	var createdAt: Instant,
)



