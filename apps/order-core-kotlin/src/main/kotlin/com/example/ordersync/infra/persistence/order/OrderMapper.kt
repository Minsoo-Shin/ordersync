package com.example.ordersync.infra.persistence.order

import com.example.ordersync.domain.order.Order
import com.example.ordersync.domain.order.OrderItem

object OrderMapper {
	fun toEntity(order: Order): JpaOrderEntity {
		val entity = JpaOrderEntity(
			id = order.id,
			status = order.status,
			createdAt = order.createdAt,
		)
		val items = order.items.map { toItemEntity(it, entity) }
		entity.items.addAll(items)
		return entity
	}

	private fun toItemEntity(item: OrderItem, parent: JpaOrderEntity): JpaOrderItemEntity {
		val e = JpaOrderItemEntity(
			productId = item.productId,
			quantity = item.quantity,
			price = item.price,
		)
		e.order = parent
		return e
	}

	fun toDomain(entity: JpaOrderEntity): Order {
		return Order.restore(
			id = entity.id,
			status = entity.status,
			createdAt = entity.createdAt,
			items = entity.items.map {
				OrderItem(
					productId = it.productId,
					quantity = it.quantity,
					price = it.price,
				)
			}
		)
	}
}

