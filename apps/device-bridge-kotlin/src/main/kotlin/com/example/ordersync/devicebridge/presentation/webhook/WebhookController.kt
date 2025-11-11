package com.example.ordersync.devicebridge.presentation.webhook

import com.example.ordersync.devicebridge.domain.orderdevicesync.OrderDeviceSyncRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhook")
class WebhookController(
	private val orderDeviceSyncRepository: OrderDeviceSyncRepository,
) {
	companion object {
		private val logger = LoggerFactory.getLogger(WebhookController::class.java)
	}

	@PostMapping("/order-sync-status")
	fun updateOrderSyncStatus(@RequestBody request: OrderSyncStatusRequest): ResponseEntity<OrderSyncStatusResponse> {
		return try {
			val orderDeviceSync = orderDeviceSyncRepository.findByOrderId(request.orderId)
			if (orderDeviceSync == null) {
				logger.warn("OrderDeviceSync not found: orderId=${request.orderId}")
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(OrderSyncStatusResponse(success = false, message = "Order not found"))
			}

			// 상태를 CONFIRMED로 업데이트
			val updated = orderDeviceSync.markAsConfirmed()
			orderDeviceSyncRepository.save(updated)

			logger.info("Order sync status confirmed: orderId=${request.orderId}")

			ResponseEntity.ok(OrderSyncStatusResponse(success = true, message = "Status updated successfully"))
		} catch (e: Exception) {
			logger.error("Failed to update order sync status: orderId=${request.orderId}", e)
			ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(OrderSyncStatusResponse(success = false, message = "Internal server error"))
		}
	}

	data class OrderSyncStatusRequest(
		val orderId: Long,
		val status: String? = null, // 옵션: 추가 상태 정보
	)

	data class OrderSyncStatusResponse(
		val success: Boolean,
		val message: String,
	)
}

