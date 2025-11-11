package com.example.ordersync.devicebridge.domain.orderdevicesync

import com.example.ordersync.event.OrderCreated

interface MqttMessagePublisher {
	fun publish(orderId: Long, event: OrderCreated): Boolean
}

