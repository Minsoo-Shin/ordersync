package com.example.ordersync.presentation.order.dto

data class CreateOrderRequest(
    val items: List<CreateOrderItemRequest>,
)

data class CreateOrderItemRequest(
    val productId: Long,
    val quantity: Int,
    val price: Long,
)

data class CreateOrderResponse(
    val id: Long,
)