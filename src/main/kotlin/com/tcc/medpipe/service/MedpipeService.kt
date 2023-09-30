package com.tcc.medpipe.service

import com.tcc.medpipe.domain.model.MedpipeControl
import com.tcc.medpipe.domain.repository.MedpipeControlRepository
import com.tcc.file.DirectoryRoot.MEDPIPE_FILES
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.io.path.extension

@Service
class MedpipeService {
    private lateinit var root: Path
    @Autowired
    private lateinit var medpipeControlRepository: MedpipeControlRepository
    fun init(directoryRoot: String): Path? {
        try {
            root = Paths.get(directoryRoot)
            return Files.createDirectories(root)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    fun buildDirectory(folderName: String): String {
        return "/"+MEDPIPE_FILES.description+"/$folderName"
    }

    @Async
    fun runScript(
        fileResult: String,
        cellWall: String,
        organismGroup: String,
        epitopeLength: String,
        email: String,
        membraneCitoplasm: String,
        medpipeControl: MedpipeControl
    ) {
        try {
            val command = "sh medpipe $fileResult $cellWall $organismGroup $epitopeLength $email $membraneCitoplasm"
            println("Incio da execução: " + LocalDateTime.now() + " commad: " + command)
            val process = Runtime.getRuntime().exec(command)
            val processEnd = process.waitFor()
            val result = BufferedReader(InputStreamReader(process.inputStream)).readText()
            println("Time: " + LocalDateTime.now() + " Result: " + result)
            println("End process: $processEnd")
            updateStatus(0, medpipeControl)
        } catch (e: Exception) {
            updateStatus(-1, medpipeControl)
            throw RuntimeException(e.message)
        }
    }

    fun saveFile(file: MultipartFile, directoryRoot: String): String {
        try {
            val path = init(directoryRoot)
            Files.copy(file.inputStream, path?.resolve(file.originalFilename))
            return (path?.toAbsolutePath() as Any).toString() + path.root.toString() +file.originalFilename
        } catch (e: Exception) {
            if (e is FileAlreadyExistsException) {
                throw RuntimeException("A file of that name already exists.")
            }
            throw RuntimeException(e.message)
        }
    }

        fun updateStatus(status: Long, medpipeControl: MedpipeControl){
            medpipeControl.status=status
            saveControl(medpipeControl)
        }

       fun saveControl(medpipeControl: MedpipeControl): MedpipeControl {
           return  medpipeControlRepository.save(medpipeControl)
        }

        fun findStatusProcess(id: Long): Long? {
            val result =  medpipeControlRepository.findById(id)

            return if(result.isPresent) result.get().status else -5
        }

    fun deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    fun getFile(dir: String): ByteArray? {
        val fileSet: MutableSet<String> = HashSet()
        var byteResource: ByteArray? = ByteArray(1)
        Files.newDirectoryStream(Paths.get(dir)).use { stream ->
            for (path in stream) {
                if (!Files.isDirectory(path) && path.extension == "zip") {
                    byteResource =Files.readAllBytes(path)
                    fileSet.add(
                        path.fileName
                            .toString()
                    )
                }
            }
        }
        return byteResource
    }

    fun loadAll(): Stream<Path?>? {
        return try {
            Files.walk(this.root, 1).filter { path -> !path.equals(this.root) }.map(this.root::relativize)
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }
}