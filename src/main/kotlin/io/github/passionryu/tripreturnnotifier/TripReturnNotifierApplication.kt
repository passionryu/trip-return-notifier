package io.github.passionryu.tripreturnnotifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TripReturnNotifierApplication

fun main(args: Array<String>) {
	runApplication<TripReturnNotifierApplication>(*args)
}
