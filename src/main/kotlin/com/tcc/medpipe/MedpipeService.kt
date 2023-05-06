package com.tcc.medpipe

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

@Service
class MedpipeService {
    private val root: Path = Paths.get("/tmp/uploads1")

    fun init() {
        try {
            Files.createDirectories(root)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    fun save(file: MultipartFile) {
        try {
            Files.copy(file.inputStream, this.root.resolve(file.originalFilename))
        } catch (e: Exception) {
            if (e is FileAlreadyExistsException) {
                throw RuntimeException("A file of that name already exists.")
            }
            throw RuntimeException(e.message)
        }
    }

    fun load(filename: String): Resource? {
        return try {
            val file: Path = root.resolve(filename)
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                resource
            } else {
                throw RuntimeException("Could not read the file!")
            }
        } catch (e: MalformedURLException) {
            throw RuntimeException("Error: " + e.message)
        }
    }

    fun deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile())
    }

    fun loadAll(): Stream<Path?>? {
        return try {
            Files.walk(this.root, 1).filter { path -> !path.equals(this.root) }.map(this.root::relativize)
        } catch (e: IOException) {
            throw RuntimeException("Could not load the files!")
        }
    }
}