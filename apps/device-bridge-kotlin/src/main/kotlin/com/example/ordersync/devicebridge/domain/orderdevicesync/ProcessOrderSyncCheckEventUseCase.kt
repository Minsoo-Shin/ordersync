package com.example.ordersync.devicebridge.domain.orderdevicesync

import com.example.ordersync.event.OrderSyncCheckEvent

interface ProcessOrderSyncCheckEventUseCase {
	fun process(event: OrderSyncCheckEvent)
}

