package com.example.ordersync.domain.order

data class OrderItem(
    val productId: Long,
    val quantity: Int,
    val price: Long, // minor unit (e.g., KRW won)
) {
    init {
        require(quantity > 0) { "quantity must be greater than 0" }
        require(price >= 0) { "price must be >= 0" }
    }
}


