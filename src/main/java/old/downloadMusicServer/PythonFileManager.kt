package old.downloadMusicServer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun extractPythonScript(): Path {
    // Use the classloader to load from the classpath root (resources/scripts/DownloadMusic.py)
    val input = object {}.javaClass.classLoader
        .getResourceAsStream("scripts/DownloadMusic.py")
        ?: error("Cannot find script 'DownloadMusic.py'")

    val tempFile = Files.createTempFile("DownloadMusic", ".py")

    input.use {
        Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING)
    }

    tempFile.toFile().deleteOnExit()
    return tempFile
}