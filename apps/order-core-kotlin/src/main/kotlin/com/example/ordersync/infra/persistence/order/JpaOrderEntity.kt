package com.example.ordersync.infra.persistence.order

import com.example.ordersync.domain.order.OrderStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "orders")
class JpaOrderEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	var id: Long? = null,

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	var status: OrderStatus,

	@Column(name = "created_at", nullable = false)
	var createdAt: Instant,
)
{
	@OneToMany(
		mappedBy = "order",
		cascade = [CascadeType.ALL],
		orphanRemoval = true,
		fetch = FetchType.LAZY,
	)
	var items: MutableList<JpaOrderItemEntity> = mutableListOf()
}

