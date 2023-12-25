package com.tcc.medpipe

import com.tcc.log
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
@Api(value = "Medpipe")
@RequestMapping("/v1/medpipe")
class MedpipeController(val medpipeService: MedpipeService) {

    @ApiOperation(value = "Running the Medpipe script")
    @PostMapping("/run")
    fun runFileProcess(
        @ApiParam(name = "file", value = "file for Medpipe processing")
        @RequestParam("file") file: MultipartFile,
        @ApiParam(name = "folderName", value = "name of the folder in which the processing results will be stored")
        @RequestParam("folderName") folderName: String,
        @ApiParam(name = "cellWall", value = "cell wall")
        @RequestParam("cellWall") cellWall: String,
        @ApiParam(name = "organismGroup", value = "organism group")
        @RequestParam("organismGroup") organismGroup: String,
        @ApiParam(name = "epitopeLength", value = "epitope length")
        @RequestParam(value = "epitopeLength", required = false, defaultValue = "9") epitopeLength: String,
        @ApiParam(name = "email", value = "email address to which Medpipe results will be sent")
        @RequestParam("email") email: String,
        @ApiParam(name = "membraneCitoplasm", value = "cytoplasmic membrane")
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

    @ApiOperation(value = "Fetches the resulting file from Medpipe")
    @GetMapping(
        value = ["/result-file"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun getResultFile(@ApiParam(name = "dir", value = "name of the directory where the file is located") @RequestParam("dir")  dir: String): ByteArray? {
        return medpipeService.getFile(dir)
    }

    @ApiOperation(value = "Fetch Medpipe script processing status")
    @GetMapping("/status/{id}")
    fun getStatus(@ApiParam(name = "id", value = "status id") @PathVariable id: Long): Long? {
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