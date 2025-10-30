package com.example.ordersync.infra.order

import com.example.ordersync.domain.order.Order
import com.example.ordersync.domain.order.OrderRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class OrderRepositoryAdapter(
    private val jpaRepository: SpringDataOrderJpaRepository,
) : OrderRepository {

    @Transactional
    override fun save(order: Order): Order {
        val entity = OrderMapper.toEntity(order)
        val saved = jpaRepository.save(entity)
        return OrderMapper.toDomain(saved)
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): Order? {
        val entity = jpaRepository.findById(id)
        return if (entity.isPresent) OrderMapper.toDomain(entity.get()) else null
    }
}


