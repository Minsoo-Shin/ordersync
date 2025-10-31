package com.example.ordersync.infra.persistence.order

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataOrderJpaRepository : JpaRepository<JpaOrderEntity, Long>

