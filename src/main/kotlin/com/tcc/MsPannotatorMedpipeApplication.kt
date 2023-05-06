package com.tcc

import com.tcc.medpipe.MedpipeService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.annotation.Resource

@SpringBootApplication
class MsPannotatorMedpipeApplication : CommandLineRunner {
	@Resource
	lateinit var storageService: MedpipeService

	override fun run(vararg args: String?) {
		storageService.init()
	}
}
fun main(args: Array<String>) {
	runApplication<MsPannotatorMedpipeApplication>(*args)
}


