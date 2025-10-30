package com.example.ordersync.application.order

import com.example.ordersync.domain.order.OrderRepository
import com.example.ordersync.domain.order.OrderStatus
import org.springframework.stereotype.Service
import java.time.Instant

interface GetOrderUseCase {
    fun getById(id: Long): GetOrderResult?
}

data class GetOrderResult(
    val id: Long,
    val status: OrderStatus,
    val createdAt: Instant,
    val items: List<GetOrderItemResult>,
)

data class GetOrderItemResult(
    val productId: Long,
    val quantity: Int,
    val price: Long,
)

@Service
class GetOrderService(
    private val orderRepository: OrderRepository,
) : GetOrderUseCase {
    override fun getById(id: Long): GetOrderResult? {
        val order = orderRepository.findById(id) ?: return null
        return GetOrderResult(
            id = order.id!!,
            status = order.status,
            createdAt = order.createdAt,
            items = order.items.map {
                GetOrderItemResult(
                    productId = it.productId,
                    quantity = it.quantity,
                    price = it.price,
                )
            },
        )
    }
}


