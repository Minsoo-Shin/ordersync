package com.example.ordersync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrdersyncApplication

fun main(args: Array<String>) {
	runApplication<OrdersyncApplication>(*args)
}
