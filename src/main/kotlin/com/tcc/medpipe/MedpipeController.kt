package com.tcc.medpipe

import com.tcc.log
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/medpipe")
class MedpipeController(val medpipeService: MedpipeService) {

    @PostMapping("/run")
    fun runFileProcess(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("folderName") folderName: String,
        @RequestParam("cellWall") cellWall: String,
        @RequestParam("organismGroup") organismGroup: String,
        @RequestParam(value = "epitopeLength", required = false, defaultValue = "9") epitopeLength: String,
        @RequestParam("email") email: String,
        @RequestParam("membraneCitoplasm", required = false, defaultValue = "") membraneCitoplasm: String
    ): String {
        log.info("[runFileProcess] - Init run...")
        val directoryRoot = medpipeService.buildDirectory(folderName)
        log.info("[runFileProcess] - directoryRoot: $directoryRoot")
        val fileResult = medpipeService.saveFile(file, directoryRoot)
        log.info("[runFileProcess] - fileResult: $fileResult")
        val process = medpipeService.saveControl(MedpipeControl(process = folderName, status = 1))
        log.info("[runFileProcess] - process: $process")
        medpipeService.runScript(
            fileResult,
            cellWall,
            organismGroup,
            epitopeLength,
            email,
            membraneCitoplasm,
            process
        )
        log.info("[runFileProcess] - script terminated: $directoryRoot;${process.id}")
        return "$directoryRoot;${process.id}"
    }

    @GetMapping(
        value = ["/result-file"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun getResultFile(@RequestParam("dir") dir: String): ByteArray? {
        return medpipeService.getFile(dir)
    }

    @GetMapping("/status/{id}")
    fun getStatus(@PathVariable id: Long): Long? {
        return medpipeService.findStatusProcess(id)
    }

    @GetMapping("/status")
    fun getAllStatus(): List<MedpipeControl> {
        return medpipeService.findAllStatusProcess()
    }

    @GetMapping("/signal")
    fun getSignalFileResult(
        @RequestParam("fileName") fileName: String,
        @RequestParam("type") type: String
    ): String {
        return medpipeService.readFileInfo(fileName, type)
    }

    @GetMapping("/tmh")
    fun getTmhFileResult(
        @RequestParam("fileName") fileName: String,
        @RequestParam("type") type: String
    ): String {
        return medpipeService.readFileInfo(fileName, type)
    }

    @PostMapping
    fun save(@RequestBody medpipeControl: MedpipeControl): MedpipeControl {
        return medpipeService.saveControl(medpipeControl)
    }

}