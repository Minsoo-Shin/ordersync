package com.example.ordersync.presentation.order

import com.example.ordersync.application.order.CreateOrderCommand
import com.example.ordersync.application.order.CreateOrderItemCommand
import com.example.ordersync.application.order.CreateOrderUseCase
import com.example.ordersync.application.order.GetOrderUseCase
import com.example.ordersync.application.order.GetOrderResult
import com.example.ordersync.presentation.order.dto.CreateOrderRequest
import com.example.ordersync.presentation.order.dto.CreateOrderResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val createOrder: CreateOrderUseCase,
    private val getOrder: GetOrderUseCase,
) {
    @PostMapping
    fun create(@RequestBody request: CreateOrderRequest): ResponseEntity<CreateOrderResponse> {
        val command = CreateOrderCommand(
            items = request.items.map {
                CreateOrderItemCommand(
                    productId = it.productId,
                    quantity = it.quantity,
                    price = it.price,
                )
            }
        )
        val id = createOrder.create(command)
        return ResponseEntity.ok(CreateOrderResponse(id = id))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<GetOrderResult> {
        val result = getOrder.getById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(result)
    }
}


