package com.example.ordersync.infra.order

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataOrderJpaRepository : JpaRepository<JpaOrderEntity, Long>


