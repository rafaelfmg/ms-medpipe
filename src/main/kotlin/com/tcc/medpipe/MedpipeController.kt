package com.tcc.medpipe

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate.now
import kotlin.streams.toList

@RestController
@RequestMapping("/v1/medpipe")
class MedpipeController(val medpipeService: MedpipeService) {

    @PostMapping("/run")
    fun runProcess(@RequestBody medpipeDto: MedpipeDto): String {
        val command = " /home/rafael/usr/local/medpipe/medpipe ${medpipeDto.parameter}"
        println("Incio da execução: " + now() + " commad: " + command)
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        val result = BufferedReader(InputStreamReader(process.inputStream)).readText()
        return "Saída do script: $result"
        /*val processo = ProcessBuilder("cmd.exe", "/c", "dir","${medpipeDto.pathname}", medpipeDto.parameter)
            .directory(File("C:/Users/rafael/Documents/UFU/TCC/medpipe/usr/local/medpipe/"))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        processo.waitFor()
        val saida = BufferedReader(InputStreamReader(processo.inputStream)).readText()
        return "Saída do script:\n$saida"*/
    }

    @PostMapping("/upload") fun handleFileUpload(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String? {
        medpipeService.save(file)
        redirectAttributes.addFlashAttribute(
            "message",
            "You successfully uploaded " + file.originalFilename + "!"
        )
        return "redirect:/"
    }

    @PostMapping("/upload2")
    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
        var message = ""
        return try {
            medpipeService.save(file)
            message = "Uploaded the file successfully: " + file.originalFilename
            message
        } catch (e: Exception) {
            message = "Could not upload the file: " + file.originalFilename + ". Error: " + e.message
            message
        }
    }

    @PostMapping("/exec")
    fun executarScript(@RequestBody teste: TesteDto): String {
        return "Saída do script:\n${teste.id}"
    }
}