package com.example.ordersync.application.order

import com.example.ordersync.domain.order.Order
import com.example.ordersync.domain.order.OrderItem
import com.example.ordersync.domain.order.OrderRepository
import org.springframework.stereotype.Service

interface CreateOrderUseCase {
    fun create(request: CreateOrderCommand): Long
}

data class CreateOrderCommand(
    val items: List<CreateOrderItemCommand>,
)

data class CreateOrderItemCommand(
    val productId: Long,
    val quantity: Int,
    val price: Long,
)

@Service
class CreateOrderService(
    private val orderRepository: OrderRepository,
) : CreateOrderUseCase {
    override fun create(request: CreateOrderCommand): Long {
        val items: List<OrderItem> = request.items.map {
            OrderItem(
                productId = it.productId,
                quantity = it.quantity,
                price = it.price,
            )
        }
        val order: Order = Order.create(items)
        val saved = orderRepository.save(order)
        return saved.id!!
    }
}


