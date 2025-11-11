package com.example.ordersync.devicebridge.domain.orderdevicesync

import com.example.ordersync.event.OrderCreated

interface ProcessOrderCreatedEventUseCase {
	fun process(event: OrderCreated)
}

