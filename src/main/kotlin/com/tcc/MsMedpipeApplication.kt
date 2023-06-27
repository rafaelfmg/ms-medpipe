package com.tcc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class MsMedpipeApplication
fun main(args: Array<String>) {
	runApplication<MsMedpipeApplication>(*args)
}


