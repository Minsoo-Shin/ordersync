package com.example.ordersync.application.order

import com.example.ordersync.domain.order.Order
import com.example.ordersync.domain.order.OrderItem
import com.example.ordersync.domain.order.OrderRepository
import com.example.ordersync.event.OrderItemEvent
import com.example.ordersync.infra.messaging.kafka.OrderProducer
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
    private val orderProducer: OrderProducer,
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
        
        // 이벤트 발행
        val orderItemEvents = saved.items.map {
            OrderItemEvent(
                productId = it.productId,
                quantity = it.quantity,
                price = it.price
            )
        }
        orderProducer.publishOrderCreated(saved.id!!, orderItemEvents)
        
        return saved.id
    }
}


