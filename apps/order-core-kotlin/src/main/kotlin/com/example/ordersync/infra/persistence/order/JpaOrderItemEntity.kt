package com.example.ordersync.infra.persistence.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_items")
class JpaOrderItemEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	@Column(name = "product_id", nullable = false)
	var productId: Long,

	@Column(name = "quantity", nullable = false)
	var quantity: Int,

	@Column(name = "price", nullable = false)
	var price: Long,
)
{
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	lateinit var order: JpaOrderEntity
}

